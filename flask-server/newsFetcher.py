from flask import Flask, request, jsonify
from fetchJapaneseNews import fetch_news
from dotenv import load_dotenv
import os

load_dotenv(dotenv_path=".env.dev")

app = Flask(__name__)

@app.route("/japan-news", methods=["GET"])
def get_japan_news():
    category = request.args.get("category", "general")  # 기본값: 일반
    articles = fetch_news(category)
    return jsonify({"articles": articles})

if __name__ == "__main__":
    app.run(debug=True, port=6200)
