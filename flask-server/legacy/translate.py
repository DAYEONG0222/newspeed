import os
os.environ["FLASK_ENV"] = "development"

from flask import Flask, request, jsonify
from google.cloud import translate_v2 as translate
from dotenv import load_dotenv

# 환경 변수 로딩 (.env.translate 파일 사용)
load_dotenv(dotenv_path=".env.translate")

# 서비스 계정 키 설정
google_credential_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
if google_credential_path:
    os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = google_credential_path
else:
    raise Exception("❌ GOOGLE_APPLICATION_CREDENTIALS가 설정되지 않았습니다.")

# Flask 앱 생성
app = Flask(__name__)

# Google Cloud Translate 클라이언트 생성
translator = translate.Client()

# 번역 API 엔드포인트
@app.route("/translate", methods=["POST"])
def translate_text():
    data = request.get_json()

    text = data.get("text", "")                     # 번역할 원문 텍스트
    target_language = data.get("target_language", "ko")  # 대상 언어 (기본값: 한국어)

    try:
        # Google Translate API 호출
        result = translator.translate(text, target_language=target_language)
        translated_text = result["translatedText"]

        # 번역 결과 반환
        return jsonify({"translated_text": translated_text})

    except Exception as e:
        # 오류 발생 시 로그 출력 및 에러 응답
        print("❌ 번역 에러:", str(e))
        return jsonify({"error": str(e)}), 500

# 서버 실행
if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=7000)
