"""스페인어 기사 난이도 (Szigriszt-Pazos 가독성 + INFLESZ 척도).

  P = 206.835 - 62.3 * (음절 수 / 단어 수) - (단어 수 / 문장 수)
INFLESZ 척도(Barrio-Cantalejo et al., 2008):
  80 초과 매우 쉬움 / 65-80 꽤 쉬움 / 55-65 보통 / 40-55 약간 어려움 / 40 미만 매우 어려움
→ 앱의 3단계로 묶어 65 이상 초급, 55 이상 중급, 그 미만 고급.

기존 코드(legacy/spanishAnalyze.py)의 점수는 품사 비율 가중치가 1점 안팎밖에
기여하지 못해 사실상 평균 문장 글자 수로 정해졌다. legacy_score() 는 비교용 재현이며,
Google NL 대신 spaCy 품사 태거를 쓴다.
"""

import re
from functools import lru_cache

import spacy
import textstat

ELEMENTARY_MIN = 65.0
INTERMEDIATE_MIN = 55.0

_SENTENCE_END = re.compile(r"[.!?\n]+")


@lru_cache(maxsize=1)
def _nlp():
    return spacy.load("es_core_news_sm", disable=["ner", "lemmatizer"])


def classify(score: float) -> str:
    if score >= ELEMENTARY_MIN:
        return "초급"
    if score >= INTERMEDIATE_MIN:
        return "중급"
    return "고급"


def analyze(text: str) -> dict:
    textstat.set_lang("es")
    words = textstat.lexicon_count(text, removepunct=True) or 1
    sentences = textstat.sentence_count(text) or 1
    syllables = textstat.syllable_count(text)
    score = 206.835 - 62.3 * (syllables / words) - (words / sentences)
    return {
        "score": round(score, 2),
        "level": classify(score),
        "method": "szigriszt_pazos",
        "words_per_sentence": round(words / sentences, 2),
        "syllables_per_word": round(syllables / words, 3),
        "sentences_analyzed": sentences,
    }


def legacy_score(text: str) -> dict:
    """기존 공식 재현: (0.18*평균 문장 글자 수 + 품사 비율 가중합) * 10, 임계값 180 / 360."""
    sentences = [s.strip() for s in _SENTENCE_END.split(text) if s.strip()]
    n_sent = len(sentences) or 1
    avg_len = sum(len(s) for s in sentences) / n_sent

    counts = {"words": 0, "NOUN": 0, "VERB": 0, "ADJ": 0, "CONJ": 0}
    for token in _nlp()(text):
        counts["words"] += 1
        if token.pos_ in ("CCONJ", "SCONJ"):
            counts["CONJ"] += 1
        elif token.pos_ in counts:
            counts[token.pos_] += 1
    words = counts["words"] or 1
    score = (
        0.18 * avg_len
        + 0.3 * counts["CONJ"] / words
        + 0.25 * counts["ADJ"] / words
        + 0.2 * counts["VERB"] / words
        + 0.1 * counts["NOUN"] / words
    ) * 10
    level = "초급" if score < 180 else "중급" if score < 360 else "고급"
    return {"score": round(score, 3), "level": level, "method": "legacy"}
