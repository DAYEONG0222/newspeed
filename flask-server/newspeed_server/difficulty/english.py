"""영어 기사 난이도 (팀원 구현 공식 유지).

legacy/nlpServerNew.py 의 6개 지표 가중합(합계 100)과 임계값(70 / 63)을 그대로 쓴다.
바뀐 점은 하나: 엔터티 지표를 Google Cloud NL 대신 spaCy NER 로 계산한다.
spaCy 는 salience(중요도)를 주지 않으므로 엔터티 점수는 개수 항만 남기고
원래 가중치 15.66 은 유지한다.
"""

from functools import lru_cache

import spacy

CATEGORY_NORMS = {
    "politics": (180, 18, 10),
    "economy": (170, 17, 9),
    "society": (160, 16, 8),
    "technology": (165, 17, 9),
    "science": (190, 19, 11),
}
DEFAULT_NORMS = (170, 17, 9)


@lru_cache(maxsize=1)
def _nlp():
    return spacy.load("en_core_web_sm")


def _normalize(val, max_val, min_val=0):
    return max(min((val - min_val) / (max_val - min_val), 1), 0)


def _tree_depth(token) -> int:
    depth, stack = 0, [(token, 1)]
    while stack:
        node, d = stack.pop()
        depth = max(depth, d)
        stack.extend((child, d + 1) for child in node.children)
    return depth


def analyze(text: str, category: str = "default") -> dict:
    doc = _nlp()(text)
    alpha = [t for t in doc if t.is_alpha]
    total_words = len(alpha)
    avg_word_len = sum(len(t.text) for t in alpha) / total_words if total_words else 0

    max_depth, dep_lengths = 0, []
    for sent in doc.sents:
        max_depth = max(max_depth, _tree_depth(sent.root))
        dep_lengths += [abs(t.i - t.head.i) for t in sent if t.head != t]
    avg_dep_len = sum(dep_lengths) / len(dep_lengths) if dep_lengths else 0
    clause_count = sum(1 for t in doc if t.dep_ == "mark" and t.head.pos_ == "VERB")
    entity_count = len(doc.ents)
    entity_score = _normalize(entity_count, 30)

    length_max, depth_max, dep_max = CATEGORY_NORMS.get(category.lower(), DEFAULT_NORMS)
    score = (
        24.39 * _normalize(total_words, length_max)
        + 18.61 * _normalize(max_depth, depth_max)
        + 17.14 * _normalize(avg_dep_len, dep_max)
        + 14.26 * _normalize(avg_word_len, 10)
        + 15.66 * entity_score
        + 10.00 * _normalize(clause_count, 8)
    )
    difficulty = "고급" if score > 70 else "중급" if score > 63 else "초급"
    return {
        "difficulty": difficulty,
        "level": difficulty,
        "score": round(score, 2),
        "features": {
            "total_words": total_words,
            "avg_word_length": round(avg_word_len, 2),
            "tree_depth": max_depth,
            "avg_dep_len": round(avg_dep_len, 2),
            "entity_count": entity_count,
            "clause_count": clause_count,
            "category": category,
        },
    }
