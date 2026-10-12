"""영어 레벨 테스트용 단어 목록(app/src/main/assets/vocabulary/english.json) 생성.

출처: NGSL 1.2 (New General Service List), Browne, C., Culligan, B., & Phillips, J.
      https://www.newgeneralservicelist.com  — CC BY-SA 4.0
      일상 영어의 약 92% 를 덮는 핵심 어휘 2,809개와 빈도 순위(SFI Rank).
가공: 기능어(spaCy 불용어)와 빈도 1~100위를 빼고, Google Cloud Translation 으로
      한국어 뜻을 붙인 뒤 번역이 비었거나 원문과 같은 단어를 제외했다.
      결과 파일도 CC BY-SA 4.0 을 따른다.

사용: python tools/build_english_vocab.py   (flask-server/.env 의 번역 키 필요)
"""

import csv
import io
import json
import re
from pathlib import Path

import requests
from dotenv import load_dotenv
from spacy.lang.en.stop_words import STOP_WORDS

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT.parent / "app/src/main/assets/vocabulary/english.json"
NGSL_URL = "https://www.newgeneralservicelist.com/s/NGSL_12_stats.csv"
MIN_RANK = 101          # 1~100위는 대부분 기능어라 퀴즈에 부적합
BATCH = 100             # translate_v2 는 한 번에 여러 문자열을 받는다

# 단어만 따로 번역하면 문맥이 없어 뜻이 엇나가는 경우가 있다. 검토 중 발견한 오역은 직접 보정.
# (무작위 100개 검토: 명백한 오역 1개, 애매 2개)
OVERRIDES = {
    "secondary": "이차적인",
    "freeze": "얼다",
    "salary": "급여",
    "boost": "끌어올리다",
}


def load_ngsl() -> list[tuple[str, int]]:
    text = requests.get(NGSL_URL, timeout=30).content.decode("utf-8-sig")
    rows = csv.DictReader(io.StringIO(text))
    return [(r["Lemma"].strip(), int(r["SFI Rank"])) for r in rows]


def is_bad_translation(word: str, meaning: str) -> bool:
    meaning = meaning.strip()
    return (not meaning
            or meaning.lower() == word.lower()
            or not re.search(r"[가-힣]", meaning))   # 한글이 하나도 없으면 번역 실패로 봄


def main():
    load_dotenv(ROOT / ".env")
    from google.cloud import translate_v2

    words = [(w, r) for w, r in load_ngsl()
             if r >= MIN_RANK and w.isalpha() and w.lower() not in STOP_WORDS]
    client = translate_v2.Client()

    items, dropped = [], []
    for i in range(0, len(words), BATCH):
        chunk = words[i:i + BATCH]
        results = client.translate([w for w, _ in chunk], source_language="en", target_language="ko")
        for (word, rank), res in zip(chunk, results):
            meaning = OVERRIDES.get(word) or res["translatedText"].replace("&#39;", "'").strip()
            if is_bad_translation(word, meaning):
                dropped.append((word, meaning))
            else:
                # band: 레벨 테스트 출제 구간 (1: ~1000위, 2: ~2000위, 3: 그 이후)
                band = 1 if rank <= 1000 else 2 if rank <= 2000 else 3
                items.append({"english": word, "korean": meaning, "rank": rank, "band": band})

    OUT.write_text(json.dumps({
        "source": "NGSL 1.2 (Browne, Culligan & Phillips), https://www.newgeneralservicelist.com",
        "license": "CC BY-SA 4.0 — 한국어 뜻은 Google Cloud Translation 으로 생성",
        "english_vocabulary": items,
    }, ensure_ascii=False, indent=1), encoding="utf-8")
    print(f"saved {len(items)} words, dropped {len(dropped)} → {OUT}")
    print("dropped sample:", dropped[:20])


if __name__ == "__main__":
    main()
