"""일본어 기사 난이도 (jReadability).

Lee & Hasebe 의 jReadability 공식을 원 논문 조건 그대로 적용한다.
  X = 평균 문장 길이(단어 수) * -0.056
      + 한어(漢語) 비율(%) * -0.126
      + 화어(和語) 비율(%) * -0.042
      + 동사 비율(%) * -0.145
      + 조동사 비율(%) * -0.044
      + 11.724
형태소 분석: MeCab 계열(fugashi) + UniDic, 단단위(SUW). 어종(goshu)은 UniDic 이 제공한다.

등급 구간(논문 Table 4):
  5.5-6.4 하위 초급 / 4.5-5.4 상위 초급 / 3.5-4.4 하위 중급 /
  2.5-3.4 상위 중급 / 1.5-2.4 하위 고급 / 0.5-1.4 상위 고급
→ 앱의 3단계로 묶어 4.5 이상 초급, 2.5 이상 중급, 그 미만 고급.

기존 코드(legacy/japaneseAnalyze.py)는 같은 계수에 비율을 0~1 로, 문장 길이를
글자 수로 넣어서 점수가 사실상 문장 길이 하나로 정해졌다. legacy_score() 는
비교 평가용으로 그 계산을 로컬 형태소 분석기로 재현한 것이다.
"""

import re
from functools import lru_cache

import fugashi

ELEMENTARY_MIN = 4.5
INTERMEDIATE_MIN = 2.5

# 단어 수에서 제외할 품사 (문장부호·공백)
_SKIP_POS = {"補助記号", "空白", "記号"}
_SENTENCE_END = re.compile(r"(?<=[。！？!?])|\n+")


@lru_cache(maxsize=1)
def _tagger() -> fugashi.Tagger:
    return fugashi.Tagger()


def split_sentences(text: str) -> list[str]:
    return [s.strip() for s in _SENTENCE_END.split(text) if s and s.strip()]


def tokenize(text: str) -> list[str]:
    return [w.surface for w in _tagger()(text)]


def _counts(text: str) -> dict:
    sentences = split_sentences(text)
    counts = {"sentences": len(sentences), "chars": 0, "words": 0,
              "kango": 0, "wago": 0, "verbs": 0, "aux_verbs": 0,
              "kanji_words": 0, "hiragana_words": 0}
    for sentence in sentences:
        counts["chars"] += len(sentence)
        for word in _tagger()(sentence):
            pos = word.feature.pos1
            if pos in _SKIP_POS:
                continue
            counts["words"] += 1
            goshu = word.feature.goshu
            if goshu == "漢":
                counts["kango"] += 1
            elif goshu == "和":
                counts["wago"] += 1
            if pos == "動詞":
                counts["verbs"] += 1
            elif pos == "助動詞":
                counts["aux_verbs"] += 1
            if re.search(r"[一-龥]", word.surface):
                counts["kanji_words"] += 1
            elif re.fullmatch(r"[ぁ-んー]+", word.surface):
                counts["hiragana_words"] += 1
    return counts


def classify(score: float) -> str:
    if score >= ELEMENTARY_MIN:
        return "초급"
    if score >= INTERMEDIATE_MIN:
        return "중급"
    return "고급"


def analyze(text: str) -> dict:
    c = _counts(text)
    words = c["words"] or 1
    sentences = c["sentences"] or 1
    mean_len = c["words"] / sentences
    p_kango = 100.0 * c["kango"] / words
    p_wago = 100.0 * c["wago"] / words
    p_verb = 100.0 * c["verbs"] / words
    p_aux = 100.0 * c["aux_verbs"] / words

    score = (
        -0.056 * mean_len
        - 0.126 * p_kango
        - 0.042 * p_wago
        - 0.145 * p_verb
        - 0.044 * p_aux
        + 11.724
    )
    return {
        "score": round(score, 3),
        "level": classify(score),
        "method": "jreadability",
        "sentence_len": round(mean_len, 2),
        "kango_pct": round(p_kango, 2),
        "wago_pct": round(p_wago, 2),
        "verb_pct": round(p_verb, 2),
        "aux_verb_pct": round(p_aux, 2),
        "sentences_analyzed": c["sentences"],
    }


def legacy_score(text: str) -> dict:
    """기존 공식 재현: 비율 0~1, 문장 길이는 글자 수, 임계값 8.0 / 7.2."""
    c = _counts(text)
    words = c["words"] or 1
    sentences = c["sentences"] or 1
    score = (
        -0.056 * (c["chars"] / sentences)
        - 0.126 * (c["kanji_words"] / words)
        - 0.042 * (c["hiragana_words"] / words)
        - 0.145 * (c["verbs"] / words)
        - 0.044 * (c["aux_verbs"] / words)
        + 11.724
    )
    level = "초급" if score >= 8.0 else "중급" if score >= 7.2 else "고급"
    return {"score": round(score, 3), "level": level, "method": "legacy"}
