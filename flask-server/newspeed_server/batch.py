"""기사 여러 개를 한 번에 추출·분석.

기존 앱(일본어·스페인어 ViewModel)은 기사 10개마다 '본문 추출 → 난이도 분석'
요청 2번을 순서대로 보냈다(최대 20번 왕복). 여기서는 앱이 URL 목록을 한 번에 보내고,
서버가 네트워크 대기가 대부분인 본문 추출을 스레드 풀로 동시에 처리한다.
형태소 분석기(MeCab)는 스레드 안전하지 않고 CPU 작업이라 분석은 락 아래에서 한다.
"""

import threading
import time
from concurrent.futures import ThreadPoolExecutor

from . import difficulty
from .cache import TTLCache
from .extract import ExtractError, fetch_article_text

NLP_LOCK = threading.Lock()


def analyze_text_locked(text: str, lang: str, category: str = "default") -> dict:
    with NLP_LOCK:
        return difficulty.analyze(text, lang, category)


def analyze_one(url: str, lang: str, category: str, cache: TTLCache | None, fetch=fetch_article_text) -> dict:
    key = f"{lang}|{url}"
    if cache is not None:
        cached = cache.get(key)
        if cached is not None:
            return {**cached, "cached": True}

    started = time.perf_counter()
    try:
        text = fetch(url)
    except (ExtractError, OSError) as e:
        return {"url": url, "error": str(e)}
    except Exception as e:  # requests 예외 등
        return {"url": url, "error": f"{type(e).__name__}: {e}"}

    analysis = analyze_text_locked(text, lang, category)
    result = {
        "url": url,
        "text": text,
        "level": analysis["level"],
        "analysis": analysis,
        "elapsed_ms": round((time.perf_counter() - started) * 1000, 1),
    }
    if cache is not None:
        cache.set(key, result)
    return {**result, "cached": False}


def analyze_many(urls: list[str], lang: str, category: str = "default",
                 cache: TTLCache | None = None, max_workers: int = 8,
                 fetch=fetch_article_text) -> list[dict]:
    """입력 순서를 유지해서 결과를 돌려준다. max_workers=1 이면 기존과 같은 순차 처리."""
    if max_workers <= 1:
        return [analyze_one(u, lang, category, cache, fetch) for u in urls]
    with ThreadPoolExecutor(max_workers=max_workers) as pool:
        return list(pool.map(lambda u: analyze_one(u, lang, category, cache, fetch), urls))
