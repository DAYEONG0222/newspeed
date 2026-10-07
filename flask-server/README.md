# NewsPeed Flask Server

뉴스 기사 본문을 추출하고 언어별 난이도(초급·중급·고급)를 분석하는 서버입니다.
기능별로 나뉘어 있던 Flask 서버 6개를 하나의 앱(`newspeed_server`)으로 합쳤고, 이전 코드는 `legacy/`에 그대로 보존했습니다.

## 실행

```bash
python -m venv .venv
.venv\Scripts\activate            # macOS/Linux: source .venv/bin/activate
pip install -r requirements.txt
python -m spacy download en_core_web_sm
python -m spacy download es_core_news_sm
python run.py                     # http://localhost:5000
```

유료 API 키 없이 실행됩니다. 번역(`/translate`)과 유의어(`/synonyms`)만 `.env`에 키가 있을 때 동작하고, 키가 없으면 해당 요청만 503을 돌려줍니다.

```env
# .env (선택)
GOOGLE_APPLICATION_CREDENTIALS=path/to/service-account.json
BABELNET_KEY=your-babelnet-key
```

## 엔드포인트

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| POST | `/articles/analyze` | **URL 목록을 한 번에 본문 추출·난이도 분석** (`{"lang": "ja", "urls": [...]}`, 최대 20개) |
| POST | `/extract` | URL → 본문 |
| POST | `/tokenize-ja` | 일본어 문장 → 단어 목록 |
| POST | `/analyze` | 영어 난이도 |
| POST | `/analyze-japanese-news` | 일본어 난이도 |
| POST | `/analyze-es` | 스페인어 난이도 |
| POST | `/translate` | 번역 (키 필요) |
| POST | `/synonyms` | 일본어·스페인어 유의어 (키 필요) |
| GET | `/health` | 상태와 캐시 적중 통계 |

기존 경로는 그대로 유지해서 앱은 서버 주소만 바꾸면 됩니다.

## 난이도 분석 방법

| 언어 | 방법 | 3단계 기준 |
| --- | --- | --- |
| 일본어 | jReadability (Lee & Hasebe). MeCab(fugashi) + UniDic 단단위로 평균 문장 길이(단어 수), 한어·화어·동사·조동사 비율(%) 계산 | 4.5 이상 초급 / 2.5 이상 중급 / 미만 고급 (논문 6단계 구간을 묶음) |
| 스페인어 | Szigriszt-Pazos 가독성 + INFLESZ 척도 | 65 이상 초급 / 55 이상 중급 / 미만 고급 |
| 영어 | 팀원 구현 공식 유지 (구문 트리 깊이, 의존 거리 등 6개 지표 가중합). 엔터티만 spaCy NER로 계산 | 63 초과 중급 / 70 초과 고급 |

## 구조

```text
run.py                    # 실행 진입점
newspeed_server/
  app.py                  # 라우트, 입력 검증, 에러 처리
  batch.py                # URL 목록 동시 추출(스레드 풀) + 분석
  cache.py                # 스레드 안전 TTL 캐시 (10분)
  extract.py              # 본문 추출 (trafilatura)
  external.py             # 번역·유의어 (키가 있을 때만)
  difficulty/             # 언어별 난이도
tests/                    # pytest
evaluation/               # 난이도 판별력 평가 (공개 말뭉치)
benchmarks/               # 순차 vs 배치 처리 시간 측정
legacy/                   # 통합 전 서버 6개 (비교용)
```

## 테스트 · 평가

```bash
pytest -q                               # 단위 테스트
python evaluation/build_datasets.py     # 평가 말뭉치 내려받기 (원문은 저장소에 포함하지 않음)
python evaluation/evaluate.py           # 기존 공식 vs 개선 공식
python benchmarks/bench_batch.py        # 기사 10개 처리 시간
```
