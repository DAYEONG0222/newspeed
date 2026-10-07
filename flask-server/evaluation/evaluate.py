"""기존 공식(legacy) vs 개선 공식 난이도 판별력 비교.

지표
- 쌍 정확도: 같은 내용의 쉬운 글이 어려운 글보다 '더 쉽다'는 점수를 받은 쌍의 비율 (동점 0.5)
- AUC: 쌍을 무시하고 쉬운 글 전체와 어려운 글 전체를 점수로 얼마나 잘 가르는지
- 등급 분포: 쉬운 글 / 어려운 글이 초급·중급·고급으로 몇 개씩 분류됐는지

사용: python evaluation/evaluate.py   (먼저 build_datasets.py 실행)
결과: evaluation/results/readability_eval.json  (원문 없이 수치만)
"""

import json
import sys
from collections import Counter
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from newspeed_server.difficulty import japanese, spanish  # noqa: E402

DATA = Path(__file__).parent / "data"
RESULTS = Path(__file__).parent / "results"

# (함수, 점수가 클수록 쉬운가)
METHODS = {
    "ja": {"legacy": (japanese.legacy_score, True), "improved": (japanese.analyze, True)},
    "es": {"legacy": (spanish.legacy_score, False), "improved": (spanish.analyze, True)},
}


def _easiness(score: float, higher_is_easier: bool) -> float:
    return score if higher_is_easier else -score


def _auc(easy: list[float], hard: list[float]) -> float:
    wins = sum((e > h) + 0.5 * (e == h) for e in easy for h in hard)
    return wins / (len(easy) * len(hard))


def evaluate(lang: str) -> dict:
    pairs = json.loads((DATA / f"{lang}_pairs.json").read_text(encoding="utf-8"))["pairs"]
    report = {"pairs": len(pairs)}
    for name, (fn, higher_is_easier) in METHODS[lang].items():
        easy_res = [fn(p["easy"]) for p in pairs]
        hard_res = [fn(p["hard"]) for p in pairs]
        easy_s = [_easiness(r["score"], higher_is_easier) for r in easy_res]
        hard_s = [_easiness(r["score"], higher_is_easier) for r in hard_res]
        pair_acc = sum((e > h) + 0.5 * (e == h) for e, h in zip(easy_s, hard_s)) / len(pairs)
        report[name] = {
            "pair_accuracy": round(pair_acc, 3),
            "auc": round(_auc(easy_s, hard_s), 3),
            "easy_levels": dict(Counter(r["level"] for r in easy_res)),
            "hard_levels": dict(Counter(r["level"] for r in hard_res)),
        }
    return report


if __name__ == "__main__":
    RESULTS.mkdir(exist_ok=True)
    out = {lang: evaluate(lang) for lang in ("ja", "es")}
    (RESULTS / "readability_eval.json").write_text(json.dumps(out, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(out, ensure_ascii=False, indent=2))
