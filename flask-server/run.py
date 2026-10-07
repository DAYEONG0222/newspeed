"""통합 서버 실행: python run.py  (기본 포트 5000, PORT 환경변수로 변경)"""

import os

from dotenv import load_dotenv

from newspeed_server import create_app

load_dotenv()  # .env 의 GOOGLE_APPLICATION_CREDENTIALS, BABELNET_KEY (선택)

if __name__ == "__main__":
    port = int(os.getenv("PORT", "5000"))
    create_app().run(host="0.0.0.0", port=port, debug=os.getenv("FLASK_DEBUG") == "1", threaded=True)
