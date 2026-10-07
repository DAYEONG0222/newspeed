import os
from dotenv import load_dotenv
from flask import Flask, request, jsonify
from fetchSpanishNews import fetch_articles_from_gnews, extract_article_body
from spanishAnalyze import analyze_spanish_text

# ✅ 환경 변수 로드
load_dotenv(dotenv_path=".env.dev")
GNEWS_API_KEY = os.getenv("GNEWS_API_KEY")

# ✅ Flask 앱 초기화
app = Flask(__name__)

# ✅ 뉴스 수집 + 난이도 분석 함수
def fetch_and_analyze_news(category):
    results = []
    articles = fetch_articles_from_gnews(GNEWS_API_KEY, category)

    for article in articles:
        url = article.get("url")
        if not url:
            continue

        body = extract_article_body(url)
        if len(body) < 300:
            continue  # 너무 짧은 본문은 분석 제외

        result = analyze_spanish_text(body)
        results.append({
            "url": url,
            "analysis": result
        })

    return results

# ✅ 테스트용 실행 함수 (직접 실행 시 작동)
def fetch_and_analyze():
    categories = ["world", "business", "science", "technology", "nation"]
    for category in categories:
        print(f"카테고리: {category}")
        results = fetch_and_analyze_news(category)
        for article in results:
            print("기사 URL:", article["url"])
            print("분석 결과:", article["analysis"])
            print("-" * 60)

# ✅ Android 앱에서 호출할 API 엔드포인트
@app.route("/spanish-news-analyze", methods=["GET"])
def get_spanish_news():
    category = request.args.get("category", "world")
    results = fetch_and_analyze_news(category)
    return jsonify({
        "category": category,
        "articles": results
    })

# ✅ 직접 실행 시: 테스트 실행 또는 서버 실행
if __name__ == "__main__":
    mode = os.getenv("MODE", "server")  # .env.dev에 MODE=test 설정하면 테스트 실행
    if mode == "test":
        fetch_and_analyze()
    else:
        app.run(debug=True, port=6300)
