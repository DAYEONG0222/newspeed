import requests
from bs4 import BeautifulSoup

def fetch_articles_from_gnews(api_key, category, language="es", max_results=5):
    url = "https://gnews.io/api/v4/top-headlines"
    params = {
        "token": api_key,
        "lang": language,
        "topic": category,
        "max": max_results
    }

    response = requests.get(url, params=params)
    if response.status_code != 200:
        print("API 요청 실패:", response.status_code, response.text)
        return []

    data = response.json()
    return data.get("articles", [])

def extract_article_body(url):
    try:
        headers = {"User-Agent": "Mozilla/5.0"}
        response = requests.get(url, headers=headers, timeout=10)
        soup = BeautifulSoup(response.text, "html.parser")

        candidates = soup.find_all(["article", "main", "section", "div"], recursive=True)
        texts = [tag.get_text(separator=" ", strip=True) for tag in candidates if tag.get_text()]
        if not texts:
            return ""

        return max(texts, key=len).strip()

    except Exception as e:
        print(f"본문 추출 실패: {e}")
        return ""
