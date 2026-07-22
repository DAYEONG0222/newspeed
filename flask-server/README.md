# NewsPeed Flask Server

GNews API로 가져온 뉴스 기사의 본문을 추출하고, Google Cloud API와 언어별 분석 로직을 활용해 기사 난이도 분석, 번역, 유의어 조회를 제공하는 Flask 서버입니다.

## 설치 방법

### 1. 가상환경 생성 및 활성화

```bash
python -m venv venv
```

Windows:

```bash
venv\Scripts\activate
```

Mac/Linux:

```bash
source venv/bin/activate
```

### 2. 필수 라이브러리 설치

```bash
pip install -r requirements.txt
```

영어 난이도 분석 서버는 spaCy 영어 모델이 필요합니다.

```bash
python -m spacy download en_core_web_sm
```

## 환경 변수 설정

각 서버에서 사용하는 API 키와 Google Cloud 서비스 계정 키는 Git에 커밋하지 않는 환경변수 파일에 저장합니다. 아래 예시는 `flask-server` 디렉터리에서 서버를 실행하는 기준입니다.

`.env.nlp` (`nlpServerNew.py`):

```env
GOOGLE_APPLICATION_CREDENTIALS=your-google-service-account.json
```

`.env.translate` (`translate.py`):

```env
GOOGLE_APPLICATION_CREDENTIALS=your-google-service-account.json
```

`.env.dev` (`japaneseAnalyze.py`, `spanishAnalyze.py`, `spanishNewsFetcher.py`, `newsFetcher.py`, `fetchJapaneseNews.py`):

```env
GOOGLE_APPLICATION_CREDENTIALS=your-google-service-account.json
GNEWS_API_KEY=your-gnews-api-key
NEWSDATA_API_KEY=your-newsdata-api-key
```

`.env.babel` (`synonyms_server.py`):

```env
BABELNET_KEY=your-babelnet-api-key
```

## 서버 실행 방법

각 서버는 별도 터미널에서 실행합니다.

| 기능 | 파일 | 포트 | 엔드포인트 |
| --- | --- | --- | --- |
| 기사 본문 추출, 일본어 토큰화 | `extract_server.py` | `5000` | `/extract`, `/tokenize-ja` |
| 영어 기사 난이도 분석 | `nlpServerNew.py` | `6000` | `/analyze` |
| 일본어 기사 난이도 분석 | `japaneseAnalyze.py` | `6100` | `/analyze-japanese-news` |
| 스페인어 기사 난이도 분석 | `spanishAnalyze.py` | `6201` | `/analyze-es` |
| 기사/단어 번역 | `translate.py` | `7000` | `/translate` |
| 일본어/스페인어 유의어 조회 | `synonyms_server.py` | `7001` | `/synonyms` |

예시:

```bash
python extract_server.py
python nlpServerNew.py
python japaneseAnalyze.py
python spanishAnalyze.py
python translate.py
python synonyms_server.py
```

## 참고

- Android 에뮬레이터에서는 Flask 서버를 `10.0.2.2`로 접근합니다.
- 실제 기기에서는 `RetrofitClient`와 `RetrofitTranslateClient`의 로컬 서버 IP가 개발 PC의 IP와 일치해야 합니다.
- API 키, Google Cloud 서비스 계정 JSON, `.env` 파일, `local.properties`는 공개 저장소에 올리지 않습니다.
