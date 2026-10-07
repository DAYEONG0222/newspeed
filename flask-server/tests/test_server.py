import threading
import time

import pytest

from newspeed_server import create_app
from newspeed_server.batch import analyze_many
from newspeed_server.cache import TTLCache
from newspeed_server.difficulty import japanese, spanish

EASY_JA = "ねこが います。ねこは かわいいです。わたしは ねこが すきです。"
HARD_JA = (
    "政府は経済安全保障推進法に基づく特定重要物資の安定供給確保に向けた基本指針を閣議決定し、"
    "半導体や蓄電池などの国内生産基盤強化に関する財政支援措置の拡充方針を明示した。"
)
EASY_ES = "El gato come. La casa es azul. Mi papá lee un libro."
HARD_ES = (
    "La implementación de mecanismos institucionales de coordinación intergubernamental "
    "constituye una condición indispensable para la consolidación de políticas públicas "
    "sostenibles, especialmente en contextos caracterizados por profundas desigualdades estructurales."
)


def test_japanese_orders_easy_above_hard():
    easy, hard = japanese.analyze(EASY_JA), japanese.analyze(HARD_JA)
    assert easy["score"] > hard["score"]
    assert easy["level"] == "초급"
    assert hard["level"] == "고급"


def test_japanese_uses_percentages_and_word_counts():
    result = japanese.analyze(HARD_JA)
    assert 0 <= result["kango_pct"] <= 100
    assert result["kango_pct"] > 30  # 소수(0~1)로 넣으면 이 값이 1 이하가 된다


def test_spanish_orders_easy_above_hard():
    easy, hard = spanish.analyze(EASY_ES), spanish.analyze(HARD_ES)
    assert easy["score"] > hard["score"]
    assert easy["level"] == "초급"
    assert hard["level"] == "고급"


def test_cache_expires_and_evicts():
    now = [0.0]
    cache = TTLCache(ttl_seconds=10, max_items=2, clock=lambda: now[0])
    cache.set("a", 1)
    assert cache.get("a") == 1
    now[0] = 11
    assert cache.get("a") is None
    cache.set("b", 2)
    cache.set("c", 3)
    cache.set("d", 4)
    assert cache.get("b") is None and cache.get("d") == 4


def test_batch_keeps_order_runs_concurrently_and_caches():
    calls, active, peak = [], [0], [0]
    lock = threading.Lock()

    def fake_fetch(url):
        with lock:
            active[0] += 1
            peak[0] = max(peak[0], active[0])
        time.sleep(0.05)
        with lock:
            active[0] -= 1
        calls.append(url)
        return EASY_JA * 5

    urls = [f"https://example.com/{i}" for i in range(6)]
    cache = TTLCache(60)
    results = analyze_many(urls, "ja", cache=cache, max_workers=6, fetch=fake_fetch)
    assert [r["url"] for r in results] == urls
    assert peak[0] > 1  # 동시에 여러 개를 가져왔다
    again = analyze_many(urls, "ja", cache=cache, max_workers=6, fetch=fake_fetch)
    assert all(r["cached"] for r in again)
    assert len(calls) == 6


def test_batch_reports_failures_per_url():
    def fake_fetch(url):
        if "bad" in url:
            raise OSError("timeout")
        return EASY_ES * 5

    results = analyze_many(["https://ok", "https://bad"], "es", max_workers=2, fetch=fake_fetch)
    assert results[0]["level"] == "초급"
    assert "error" in results[1]


@pytest.fixture
def client():
    return create_app().test_client()


def test_routes_keep_old_paths(client):
    assert client.post("/analyze-japanese-news", json={"text": EASY_JA}).get_json()["level"] == "초급"
    assert client.post("/analyze-es", json={"text": EASY_ES}).get_json()["level"] == "초급"
    assert client.post("/analyze", json={"text": "The cat sat on the mat."}).get_json()["difficulty"]
    assert client.post("/tokenize-ja", json={"text": "猫が好き"}).get_json()["tokens"] == ["猫", "が", "好き"]


def test_input_validation(client):
    assert client.post("/analyze-es", json={"text": "  "}).status_code == 400
    assert client.post("/articles/analyze", json={"lang": "zh", "urls": ["x"]}).status_code == 400
    assert client.post("/articles/analyze", json={"lang": "ja", "urls": ["x"] * 21}).status_code == 400
    assert client.get("/nope").status_code == 404


def test_missing_keys_return_503_not_crash(client, monkeypatch):
    monkeypatch.delenv("GOOGLE_APPLICATION_CREDENTIALS", raising=False)
    monkeypatch.delenv("BABELNET_KEY", raising=False)
    assert client.post("/translate", json={"text": "hola"}).status_code == 503
    assert client.post("/synonyms", json={"word": "casa", "lang": "es"}).status_code == 503
