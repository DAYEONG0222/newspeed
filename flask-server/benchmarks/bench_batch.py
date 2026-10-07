"""기사 10개 처리 시간: 기존 순차 방식 vs 배치(동시 추출) vs 서버 캐시 적중.

기존 일본어·스페인어 ViewModel 은 기사마다 '본문 추출 → 분석' 을 하나씩 차례로 했다.
여기서는 같은 URL 목록으로
  sequential : max_workers=1  (기존 흐름과 같은 순서)
  batch      : max_workers=8  (/articles/analyze 가 쓰는 방식)
  cached     : 같은 요청을 한 번 더 (서버 캐시 적중)
을 번갈아 여러 번 돌려 중앙값을 비교한다. 언론사 서버 응답 편차가 커서 라운드를 반복한다.

사용: python benchmarks/bench_batch.py   (일본어 NHK RSS, 스페인어 BBC Mundo RSS 기사 10개)
결과: benchmarks/results/batch_timing.json  (기사 수와 시간만 저장, URL·원문은 저장하지 않음)
"""

import html
import json
import re
import statistics
import sys
import time
from pathlib import Path

import requests

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from newspeed_server.batch import analyze_many  # noqa: E402
from newspeed_server.cache import TTLCache  # noqa: E402

ROUNDS = 3


RSS = {
    # 공개 RSS 로 기사 URL 10개를 모은다 (GNews 키 없이 재현 가능하도록)
    "ja": ["https://www.nhk.or.jp/rss/news/cat%d.xml" % i for i in range(8)],
    "es": ["https://feeds.bbci.co.uk/mundo/rss.xml"],
}
URL_PATTERN = {"ja": r"/newsweb/na/nd-", "es": r"/mundo/articles/"}


def _urls(lang: str, n: int = 10) -> list[str]:
    found = []
    for feed in RSS[lang]:
        text = requests.get(feed, headers={"User-Agent": "Mozilla/5.0"}, timeout=20).text
        for link in re.findall(r"<link>(https?://[^<]+)</link>", text):
            link = html.unescape(link)
            if re.search(URL_PATTERN[lang], link) and link not in found:
                found.append(link)
        if len(found) >= n:
            break
    return found[:n]


def _timed(urls, lang, workers, cache):
    started = time.perf_counter()
    results = analyze_many(urls, lang, cache=cache, max_workers=workers)
    return time.perf_counter() - started, sum("level" in r for r in results)


def bench(lang: str, urls: list[str]) -> dict:
    seq, par, hit, ok = [], [], [], []
    for _ in range(ROUNDS):
        for mode in ("sequential", "batch"):  # 순서 편향을 줄이려고 매 라운드 둘 다
            cache = TTLCache(600)
            elapsed, n_ok = _timed(urls, lang, 1 if mode == "sequential" else 8, cache)
            (seq if mode == "sequential" else par).append(elapsed)
            ok.append(n_ok)
            if mode == "batch":
                hit.append(_timed(urls, lang, 8, cache)[0])
    return {
        "articles": len(urls),
        "analyzed_ok_min": min(ok),
        "sequential_s": round(statistics.median(seq), 2),
        "batch_s": round(statistics.median(par), 2),
        "cached_s": round(statistics.median(hit), 3),
        "speedup": round(statistics.median(seq) / statistics.median(par), 1),
        "rounds": ROUNDS,
    }


if __name__ == "__main__":
    report = {}
    for lang in ("ja", "es"):
        report[lang] = bench(lang, _urls(lang))
        print(lang, report[lang])
    out = Path(__file__).parent / "results"
    out.mkdir(exist_ok=True)
    report["measured_at"] = time.strftime("%Y-%m-%d %H:%M")
    (out / "batch_timing.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
