import os
import re
from flask import Flask, request, jsonify
from dotenv import load_dotenv
from google.cloud import language_v1

# ✅ 환경변수 로드
load_dotenv(dotenv_path=".env.dev")
gcp_key = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
if not gcp_key:
    raise EnvironmentError("GOOGLE_APPLICATION_CREDENTIALS 환경변수가 설정되지 않았습니다.")
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = gcp_key

# ✅ GCP NLP 클라이언트 초기화
client = language_v1.LanguageServiceClient()

# ✅ 난이도 분류 함수
def classify_level(score):
    if score < 180:
        return "초급"
    elif score < 360:
        return "중급"
    else:
        return "고급"

# ✅ 분석 함수
def analyze_spanish_text(text):
    sentences = re.split(r'[.!?\n]+', text)
    sentences = [s.strip() for s in sentences if s.strip()]
    total_sentences = len(sentences) or 1
    total_chars = sum(len(s) for s in sentences)
    avg_sentence_length = total_chars / total_sentences

    document = language_v1.Document(content=text, type_=language_v1.Document.Type.PLAIN_TEXT, language="es")
    response = client.analyze_syntax(request={"document": document})

    counts = {"words": 0, "nouns": 0, "verbs": 0, "adjectives": 0, "conjunctions": 0}
    for token in response.tokens:
        tag = language_v1.PartOfSpeech.Tag(token.part_of_speech.tag).name
        counts["words"] += 1
        if tag == "NOUN":
            counts["nouns"] += 1
        elif tag == "VERB":
            counts["verbs"] += 1
        elif tag == "ADJ":
            counts["adjectives"] += 1
        elif tag == "CONJ":
            counts["conjunctions"] += 1

    total_words = counts["words"] or 1
    noun_ratio = counts["nouns"] / total_words
    verb_ratio = counts["verbs"] / total_words
    adj_ratio = counts["adjectives"] / total_words
    conj_ratio = counts["conjunctions"] / total_words

    score = (
        0.18 * avg_sentence_length +
        0.3 * conj_ratio +
        0.25 * adj_ratio +
        0.2 * verb_ratio +
        0.1 * noun_ratio
    ) * 10

    return {
        "score": round(score, 3),
        "level": classify_level(score),
        "sentence_len": round(avg_sentence_length, 2),
        "noun_ratio": round(noun_ratio, 2),
        "verb_ratio": round(verb_ratio, 2),
        "adj_ratio": round(adj_ratio, 2),
        "conj_ratio": round(conj_ratio, 2),
        "sentences_analyzed": total_sentences
    }

# ✅ Flask 앱 구성
app = Flask(__name__)

@app.route("/analyze-es", methods=["POST"])
def analyze_route():
    data = request.get_json()
    text = data.get("text", "")

    if not isinstance(text, str) or not text.strip():
        return jsonify({"error": "빈 텍스트입니다."}), 400

    try:
        result = analyze_spanish_text(text)
        print(f"✅ 분석 결과: score = {result['score']}, level = {result['level']}", flush=True)
        return jsonify(result)
    except Exception as e:
        print("❌ 분석 실패:", str(e), flush=True)
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    print("✅ spanishAnalyze.py Flask 서버 실행 중")
    app.run(debug=True, host="0.0.0.0", port=6201)
