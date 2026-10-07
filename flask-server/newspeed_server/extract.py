import requests
import trafilatura

HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/112.0.0.0 Safari/537.36"
    ),
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Accept-Language": "en-US,en;q=0.9",
    "Referer": "https://www.google.com",
}
MIN_TEXT_LENGTH = 100


class ExtractError(Exception):
    """본문을 가져오지 못했거나 너무 짧을 때."""


def fetch_article_text(url: str, timeout: float = 10.0) -> str:
    response = requests.get(url, headers=HEADERS, timeout=timeout)
    if response.status_code != 200:
        raise ExtractError(f"HTML 요청 실패 ({response.status_code})")

    # 헤더에 charset 이 없으면 requests 가 ISO-8859-1 로 가정해 일본어가 깨진다.
    if not response.encoding or response.encoding.lower() == "iso-8859-1":
        response.encoding = response.apparent_encoding or "utf-8"
    text = trafilatura.extract(response.text, include_comments=False, include_tables=False)
    if not text or len(text.strip()) < MIN_TEXT_LENGTH:
        raise ExtractError("본문 파싱 실패 또는 너무 짧음")
    return text
