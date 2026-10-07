"""난이도 분류 평가용 '쉬운 글 / 어려운 글' 쌍 만들기.

원문은 저장소에 넣지 않는다. 이 스크립트가 evaluation/data/(gitignore)에 내려받아 만든다.

일본어: SNOW T15 (Yada lab, CC BY 4.0) — 같은 문장을 사람이 '쉬운 일본어'로 고쳐 쓴
        5만 쌍. 실제로 바뀐 쌍만 골라 20문장씩 묶어 문서 단위 쌍을 만든다.
        https://www.jnlp.org/SNOW/T15
스페인어: 같은 제목의 Vikidia(8~13세용 백과) 문서와 Wikipedia 문서 쌍 (CC BY-SA).
        본문 문단만 뽑아 앞 4,000자까지 쓴다.

사용: python evaluation/build_datasets.py
"""

import json
import random
import time
from pathlib import Path

import openpyxl
import requests
from lxml import html

DATA = Path(__file__).parent / "data"
SNOW_URL = "https://filedn.com/lit4DCIlHwxfS1gj9zcYuDJ/SNOW/T15-2020.1.7.xlsx"
HEADERS = {"User-Agent": "NewsPeed-readability-eval/0.1 (portfolio project; contact via GitHub DAYEONG0222)"}
SEED = 42


def build_japanese(n_docs: int = 150, sentences_per_doc: int = 20) -> Path:
    xlsx = DATA / "T15-2020.1.7.xlsx"
    if not xlsx.exists():
        xlsx.write_bytes(requests.get(SNOW_URL, timeout=60).content)
    rows = openpyxl.load_workbook(xlsx, read_only=True).active.iter_rows(values_only=True)
    next(rows)  # 헤더
    changed = [(orig, easy) for _, orig, easy, _ in rows if orig and easy and orig != easy]

    rng = random.Random(SEED)
    rng.shuffle(changed)
    pairs = []
    for i in range(n_docs):
        chunk = changed[i * sentences_per_doc:(i + 1) * sentences_per_doc]
        pairs.append({"id": f"snow-{i}", "hard": "".join(o for o, _ in chunk), "easy": "".join(e for _, e in chunk)})
    out = DATA / "ja_pairs.json"
    out.write_text(json.dumps({"source": "SNOW T15 (CC BY 4.0)", "changed_sentences": len(changed), "pairs": pairs},
                              ensure_ascii=False), encoding="utf-8")
    return out


def _get_json(api: str, params: dict, retries: int = 4) -> dict | None:
    """요청 제한(429)이나 일시 오류면 점점 길게 기다렸다 다시 시도."""
    for attempt in range(retries):
        try:
            r = requests.get(api, params=params, headers=HEADERS, timeout=20)
            if r.status_code == 200:
                return r.json()
        except (requests.RequestException, ValueError):
            pass
        time.sleep(2 ** attempt)
    return None


def _wiki_text(api: str, title: str, max_chars: int = 4000) -> tuple[str, str] | None:
    body = _get_json(api, {"action": "parse", "page": title, "prop": "text", "format": "json",
                           "redirects": 1, "formatversion": 2})
    data = (body or {}).get("parse")
    if not data:
        return None
    tree = html.fromstring(data["text"])
    for bad in tree.xpath("//table|//sup|//*[contains(@class,'reference')]|//style|//*[contains(@class,'mw-empty-elt')]"):
        bad.drop_tree()
    paragraphs = [p.text_content().strip() for p in tree.xpath("//p")]
    text = "\n".join(p for p in paragraphs if len(p) > 40)
    return data["title"], text[:max_chars]


def build_spanish(n_pairs: int = 100, min_chars: int = 800) -> Path:
    """수집한 쌍을 매번 파일에 저장해서, 중간에 끊겨도 다시 실행하면 이어서 모은다."""
    vik, wiki = "https://es.vikidia.org/w/api.php", "https://es.wikipedia.org/w/api.php"
    out = DATA / "es_pairs.json"
    state = json.loads(out.read_text(encoding="utf-8")) if out.exists() else {}
    pairs = state.get("pairs", [])
    seen = set(state.get("seen", [])) | {p["id"] for p in pairs}

    def save():
        out.write_text(json.dumps({"source": "Vikidia vs Wikipedia (CC BY-SA)", "pairs": pairs,
                                   "seen": sorted(seen)}, ensure_ascii=False), encoding="utf-8")

    while len(pairs) < n_pairs:
        body = _get_json(vik, {"action": "query", "list": "random", "rnnamespace": 0, "rnlimit": 20, "format": "json"})
        if not body:
            continue
        for item in body["query"]["random"]:
            title = item["title"]
            if title in seen or len(pairs) >= n_pairs:
                continue
            seen.add(title)
            easy = _wiki_text(vik, title)
            if not easy or len(easy[1]) < min_chars:   # 짧은 Vikidia 문서는 Wikipedia 를 부르지 않음
                continue
            hard = _wiki_text(wiki, title)
            time.sleep(0.3)  # 위키 서버 부담을 줄이려고 천천히
            if not hard or len(hard[1]) < min_chars:
                continue
            pairs.append({"id": title, "easy": easy[1], "hard": hard[1]})
            save()
            print(f"[es] {len(pairs)}/{n_pairs} {title}", flush=True)
        save()
    return out


if __name__ == "__main__":
    DATA.mkdir(exist_ok=True)
    if not (DATA / "ja_pairs.json").exists():
        print(build_japanese())
    print(build_spanish())
