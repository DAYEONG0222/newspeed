from flask import Flask, request, jsonify
import trafilatura
import requests
import fugashi  # ✅ 일본어 형태소 분석기 추가

# Flask 앱 초기화
app = Flask(__name__)
tagger = fugashi.Tagger()  # ✅ 형태소 분석기 인스턴스

# 요청 로깅
@app.before_request
def log_request():
    print(f"🌐 요청 수신됨: {request.method} {request.path}", flush=True)

# ✅ 본문 추출 라우트
@app.route("/extract", methods=["POST"])
def extract_article():
    data = request.json
    url = data.get("url")

    try:
        print("⏳ URL 받음:", url, flush=True)

        headers = {
            "User-Agent": (
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                "AppleWebKit/537.36 (KHTML, like Gecko) "
                "Chrome/112.0.0.0 Safari/537.36"
            ),
            "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Accept-Language": "en-US,en;q=0.9",
            "Referer": "https://www.google.com"
        }

        response = requests.get(url, headers=headers, timeout=10)

        if response.status_code != 200:
            print(f"❌ 요청 실패: {response.status_code}", flush=True)
            return jsonify({"error": f"HTML 요청 실패 ({response.status_code})"}), 400

        response.encoding = 'utf-8'
        downloaded = response.text
        text = trafilatura.extract(downloaded, include_comments=False, include_tables=False)

        if not text or len(text.strip()) < 100:
            print("❌ 본문 추출 실패 또는 너무 짧음", flush=True)
            return jsonify({"error": "본문 파싱 실패 또는 너무 짧음"}), 400

        print(f"✅ 본문 추출 완료 | 길이: {len(text)}", flush=True)
        print("📝 본문 앞부분:", text[:300], flush=True)

        return jsonify({
            "url": url,
            "text": text
        })

    except Exception as e:
        print("❌ 예외 발생:", str(e), flush=True)
        return jsonify({"error": str(e)}), 500

# ✅ 일본어 토큰화 라우트
@app.route("/tokenize-ja", methods=["POST"])
def tokenize_ja():
    data = request.get_json()
    text = data.get("text", "")

    if not isinstance(text, str) or not text.strip():
        return jsonify({"tokens": []})

    tokens = [word.surface for word in tagger(text)]
    print(f"🧪 토큰화 완료: {tokens}", flush=True)
    return jsonify({"tokens": tokens})

if __name__ == "__main__":
    print("✅ extract_server.py 실행됨")  # 이거 추가!
    app.run(debug=True, host="0.0.0.0", port=5000)

