"""통합 서버 실행: python run.py  (기본 포트 5000, PORT 환경변수로 변경)

waitress(운영용 WSGI 서버)로 실행한다. Flask 개발 서버는 keep-alive 연결을 앱(OkHttp)이
재사용할 때 'unexpected end of stream' 오류가 났다. FLASK_DEBUG=1 이면 개발 서버로 실행.
"""

import os

from dotenv import load_dotenv

from newspeed_server import create_app

load_dotenv()  # .env 의 GOOGLE_APPLICATION_CREDENTIALS, BABELNET_KEY (선택)

if __name__ == "__main__":
    port = int(os.getenv("PORT", "5000"))
    app = create_app()
    if os.getenv("FLASK_DEBUG") == "1":
        app.run(host="0.0.0.0", port=port, debug=True, threaded=True)
    else:
        from waitress import serve

        print(f"NewsPeed server on http://0.0.0.0:{port}")
        serve(app, host="0.0.0.0", port=port, threads=8)
