from flask import Flask, request, jsonify
import requests
import os
from dotenv import load_dotenv

# ✅ .env.babel 파일에서 환경변수 불러오기
load_dotenv(dotenv_path=".env.babel")

app = Flask(__name__)

# ✅ BabelNet API 키 (환경변수에서 불러옴)
BABELNET_KEY = os.getenv("BABELNET_KEY")

def query_babelnet(word, lang_code):
    """
    BabelNet API를 사용하여 동의어 추출
    """
    url = "https://babelnet.io/v6/getSenses"
    params = {
        "lemma": word,
        "searchLang": lang_code,   # "JA" or "ES"
        "langs": lang_code,
        "key": BABELNET_KEY
    }

    try:
        response = requests.get(url, params=params)
        if response.status_code != 200:
            print("❌ BabelNet 요청 실패:", response.text)
            return [], []

        senses = response.json()
        synonyms = set()

        for sense in senses:
            props = sense.get("properties", {})
            lemma = props.get("fullLemma")
            if lemma and lemma.lower() != word.lower():
                synonyms.add(lemma)

        return list(synonyms), []  # BabelNet은 현재 반의어 API 미지원

    except Exception as e:
        print("❌ 예외 발생:", e)
        return [], []

@app.route("/synonyms", methods=["POST"])
def get_synonyms():
    """
    POST 요청으로 받은 단어와 언어에 대해 동의어/반의어 반환
    """
    data = request.get_json()
    word = data.get("word")
    lang = data.get("lang")  # "ja" 또는 "es"

    if not word or not lang:
        return jsonify({"error": "word 또는 lang 누락"}), 400

    if lang == "ja":
        lang_code = "JA"
    elif lang == "es":
        lang_code = "ES"
    else:
        return jsonify({"error": "지원되지 않는 언어입니다."}), 400

    synonyms, antonyms = query_babelnet(word, lang_code)

    return jsonify({
        "synonyms": synonyms,
        "antonyms": antonyms  # 현재는 항상 빈 리스트
    })

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=7001, debug=True)
