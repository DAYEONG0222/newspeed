"""일본어·스페인어 레벨 테스트 단어 목록 생성 (app/src/main/assets/vocabulary/{japanese,spanish}.json).

제출본에 있던 두 목록은 출처가 기록되어 있지 않아, 출처와 라이선스가 분명한 공개 목록으로 교체했다.

일본어: JLPT 급수별 어휘 — Jonathan Waller (tanos.co.uk), CC BY.
        정리본: https://github.com/Bluskyo/JLPT_Vocabulary (data/vocab/parsedData/n*_vocab_cleaned.csv)
        급수마다 400개를 뽑아 band 를 매긴다: N5·N4 = 1(쉬움), N3 = 2(중간), N2·N1 = 3(어려움).
스페인어: OpenSubtitles 2018 빈도 목록 — Hermit Dave, FrequencyWords, CC BY-SA 4.0.
        https://github.com/hermitdave/FrequencyWords (content/2018/es/es_50k.txt)
        빈도순으로 기본형(spaCy 표제어와 같은 형태)인 명사·동사·형용사·부사만 골라 2,400개,
        순위 1~800 = 1, 801~1600 = 2, 1601~2400 = 3.
        자막 말뭉치라 섞여 있는 것을 거른다: 영어로 감지되는 단어(이름·영어 단어, Google 언어 감지
        신뢰도 0.7 이상), 욕설, 대명사가 붙은 동사형(darte, mírate 등).
한국어 뜻: Google Cloud Translation. 단어만 번역하면 뜻이 엇나가는 경우가 많아(스페인어 표본 100개 중
          약 15%) '역번역 검증'을 둔다: 한국어 뜻을 다시 원래 언어로 번역해 원래 단어(표제어)로
          돌아오는 것만 남긴다. 맞는 단어도 일부 빠지지만 틀린 뜻이 퀴즈에 섞이는 것을 막는 쪽을 택했다.
          또 한국어 뜻을 영어로 번역했을 때 대문자로 시작하는 한 단어(Rachel, Makoto 등)는
          사람·지명 이름으로 보고 뺀다. 스페인어 동사는 원형(-ar/-er/-ir)만 남긴다(활용형 제외).
          결과 파일은 원본 라이선스를 따른다
          (japanese.json: CC BY, spanish.json: CC BY-SA 4.0).

사용: python tools/build_ja_es_vocab.py   (flask-server/.env 의 번역 키 필요)
"""

import csv
import io
import json
import random
import re
import unicodedata
from pathlib import Path

import requests
from dotenv import load_dotenv

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT.parent / "app/src/main/assets/vocabulary"
JLPT_URL = "https://raw.githubusercontent.com/Bluskyo/JLPT_Vocabulary/main/data/vocab/parsedData/n{level}_vocab_cleaned.csv"
ES_URL = "https://raw.githubusercontent.com/hermitdave/FrequencyWords/master/content/2018/es/es_50k.txt"
PER_LEVEL = 400
ES_WORDS = 2400
ES_BAND_SIZE = 800
JLPT_BAND = {5: 1, 4: 1, 3: 2, 2: 3, 1: 3}
SEED = 42
BATCH = 100

ES_PROFANITY = {"joder", "jodido", "jodida", "mierda", "puta", "puto", "zorra", "coño", "carajo", "cabrón",
                "cabrona", "culo", "pene", "polla", "pendejo", "pendeja", "maricón", "imbécil", "idiota",
                "estúpido", "estúpida", "perra", "bastardo", "follar", "putas", "chingar", "verga"}
# 동사 원형/명령형 뒤에 대명사가 붙은 형태 (darte, mírate, pregúntale, decirle ...)
ES_CLITIC = re.compile(r"(?:(?:ar|er|ir|ár|ér|ír)(?:me|te|se|lo|la|le|nos|os|los|las|les)"
                       r"|[áéíóú]\w*(?:me|te|se|lo|la|le|nos|os|los|las|les))$")

# 단어만 번역해서 생긴 오역 중 표본 검토에서 발견한 것 (최종 표본 100개: 일본어 약 3개, 스페인어 약 6개)
JA_OVERRIDES = {"研ぐ": "갈다", "生身": "산 몸", "塵取り": "쓰레받기", "耳鼻科": "이비인후과", "先先週": "지지난주",
                "八百屋": "채소 가게", "何時までも": "언제까지나", "無論": "물론"}
ES_OVERRIDES = {"payaso": "광대", "practicar": "연습하다", "distribuidor": "유통업자", "impresionar": "감명을 주다", "soja": "콩", "aplastado": "짓눌린"}


def translate_all(words: list[str], source: str) -> list[str]:
    from google.cloud import translate_v2

    client = translate_v2.Client()
    out = []
    for i in range(0, len(words), BATCH):
        res = client.translate(words[i:i + BATCH], source_language=source, target_language="ko")
        out += [r["translatedText"].replace("&#39;", "'").strip() for r in res]
    return out


def has_korean(text: str) -> bool:
    return bool(re.search(r"[가-힣]", text))


def _plain(text: str) -> str:
    """소문자·악센트 제거·앞뒤 문장부호 제거 (비교용)"""
    text = unicodedata.normalize("NFD", text.lower())
    return "".join(ch for ch in text if unicodedata.category(ch) != "Mn").strip(" .!?¿¡。、")


def looks_like_name(meanings: list[str]) -> list[bool]:
    """한국어 뜻 → 영어가 대문자로 시작하는 한 단어면 고유명사(이름·지명)로 본다."""
    english = back_translate(meanings, "en")
    return [bool(e) and " " not in e and e[0].isupper() for e in english]


def back_translate(meanings: list[str], target: str) -> list[str]:
    from google.cloud import translate_v2

    client = translate_v2.Client()
    out = []
    for i in range(0, len(meanings), BATCH):
        res = client.translate(meanings[i:i + BATCH], source_language="ko", target_language=target)
        out += [r["translatedText"].replace("&#39;", "'").strip() for r in res]
    return out


def build_japanese() -> int:
    import fugashi

    tagger = fugashi.Tagger()

    def lemmas(text: str) -> set[str]:
        return {(w.feature.lemma or w.surface) for w in tagger(text) if w.feature.pos1 not in ("補助記号", "空白")} | {text}

    rng = random.Random(SEED)
    items = []
    for level in (5, 4, 3, 2, 1):
        text = requests.get(JLPT_URL.format(level=level), timeout=30).content.decode("utf-8-sig")
        entries = [(r["Kanji"].strip(), r["Reading"].strip()) for r in csv.DictReader(io.StringIO(text))]
        # 괄호·기호가 섞인 항목(예: "あたたか(い)")은 퀴즈에 부적합해 제외
        entries = [(w, rd) for w, rd in entries if w and re.fullmatch(r"[぀-ヿ一-鿿ー々]+", w)]
        rng.shuffle(entries)
        kept = 0
        for i in range(0, len(entries), BATCH):
            chunk = entries[i:i + BATCH]
            meanings = translate_all([w for w, _ in chunk], "ja")
            backs = back_translate(meanings, "ja")
            names = looks_like_name(meanings)
            for (w, rd), m, b, is_name in zip(chunk, meanings, backs, names):
                m = JA_OVERRIDES.get(w, m)
                ok = w in JA_OVERRIDES or ((bool(lemmas(w) & lemmas(b)) or b in (w, rd)) and not is_name)
                if has_korean(m) and ok and kept < PER_LEVEL:
                    items.append({"japanese": w, "reading": rd, "korean": m, "jlpt": f"N{level}",
                                  "band": JLPT_BAND[level]})
                    kept += 1
            if kept >= PER_LEVEL:
                break
        print(f"N{level}: {kept}")

    (ASSETS / "japanese.json").write_text(json.dumps({
        "source": "JLPT vocabulary lists by Jonathan Waller (tanos.co.uk), via github.com/Bluskyo/JLPT_Vocabulary",
        "license": "CC BY — 한국어 뜻은 Google Cloud Translation 으로 생성, 역번역 검증 통과분만",
        "japanese_vocabulary": items,
    }, ensure_ascii=False, indent=1), encoding="utf-8")
    return len(items)


def build_spanish() -> int:
    import spacy
    from google.cloud import translate_v2

    client = translate_v2.Client()
    nlp = spacy.load("es_core_news_sm", disable=["ner", "parser"])
    lines = requests.get(ES_URL, timeout=60).text.splitlines()
    candidates = [ln.split()[0] for ln in lines[:20000] if ln.split()]

    # 1) 형태 필터: 기본형 명사·동사·형용사·부사, 욕설·대명사 결합형 제외
    lemmas, seen = [], set()
    for doc in nlp.pipe(candidates, batch_size=500):
        token = doc[0]
        word = token.text
        if (len(doc) == 1 and word.isalpha() and len(word) >= 3 and not token.is_stop
                and token.pos_ in {"NOUN", "VERB", "ADJ", "ADV"}
                and token.lemma_.lower() == word and word not in seen
                and word not in ES_PROFANITY
                # 동사는 원형만 (comiste, leeré, tómalo 같은 활용·대명사 결합형 제외)
                and not (token.pos_ == "VERB" and not re.search(r"(ar|er|ir)$", word))
                and not (token.pos_ == "VERB" and ES_CLITIC.search(word))):  # parte(명사) 같은 오탐 방지
            seen.add(word)
            lemmas.append(word)

    # 2) 영어로 감지되는 단어(이름·영어 단어) 제외 후 번역, 목표 개수가 찰 때까지
    items, dropped_en = [], []
    for i in range(0, len(lemmas), BATCH):
        chunk = lemmas[i:i + BATCH]
        detected = client.detect_language(chunk)
        keep = [w for w, d in zip(chunk, detected)
                if not (d["language"] == "en" and d["confidence"] >= 0.7) or dropped_en.append(w)]
        if not keep:
            continue
        results = client.translate(keep, source_language="es", target_language="ko")
        meanings = [ES_OVERRIDES.get(w) or r["translatedText"].replace("&#39;", "'").strip()
                    for w, r in zip(keep, results)]
        backs = back_translate(meanings, "es")
        names = looks_like_name(meanings)
        for word, meaning, back, is_name in zip(keep, meanings, backs, names):
            back_forms = {_plain(t.lemma_) for t in nlp(back)} | {_plain(back)}
            round_trip = word in ES_OVERRIDES or (_plain(word) in back_forms and not is_name)
            if has_korean(meaning) and meaning.lower() != word and round_trip:
                rank = len(items) + 1
                items.append({"spanish": word, "korean": meaning, "rank": rank,
                              "band": min(3, (rank - 1) // ES_BAND_SIZE + 1)})
            if len(items) >= ES_WORDS:
                break
        if len(items) >= ES_WORDS:
            break
    print("es dropped as English:", len(dropped_en), dropped_en[:25])

    (ASSETS / "spanish.json").write_text(json.dumps({
        "source": "FrequencyWords by Hermit Dave (OpenSubtitles 2018), github.com/hermitdave/FrequencyWords",
        "license": "CC BY-SA 4.0 — 한국어 뜻은 Google Cloud Translation 으로 생성, 역번역 검증 통과분만",
        "spanish_vocabulary": items,
    }, ensure_ascii=False, indent=1), encoding="utf-8")
    return len(items)


if __name__ == "__main__":
    load_dotenv(ROOT / ".env")
    print("japanese:", build_japanese())
    print("spanish:", build_spanish())
