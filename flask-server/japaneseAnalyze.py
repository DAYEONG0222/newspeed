import os
import re
from flask import Flask, request, jsonify
from dotenv import load_dotenv
from google.cloud import language_v1

# ✅ 환경 변수 로딩
load_dotenv(dotenv_path=".env.dev")
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")

# ✅ Flask 앱 및 Google NLP 클라이언트 초기화
app = Flask(__name__)
client = language_v1.LanguageServiceClient()

def classify_level(score):
    if score >= 8.0:
        return "초급"
    elif score >= 7.2:
        return "중급"
    else:
        return "고급"


# ✅ NLP 기반 난이도 분석
def analyze_text(text):
    sentences = re.split(r'(?<=[。！？])', text)
    total_chars = sum(len(s) for s in sentences)
    total_sentences = len(sentences) or 1
    avg_sentence_length = total_chars / total_sentences

    document = language_v1.Document(content=text, type_=language_v1.Document.Type.PLAIN_TEXT, language="ja")
    response = client.analyze_syntax(request={"document": document})

    counts = {
        "words": 0,
        "kanji_words": 0,
        "wago_words": 0,
        "verbs": 0,
        "aux_verbs": 0
    }

    for token in response.tokens:
        surface = token.text.content
        tag = language_v1.PartOfSpeech.Tag(token.part_of_speech.tag).name
        counts["words"] += 1

        if re.search(r"[一-龥]", surface):
            counts["kanji_words"] += 1
        elif re.fullmatch(r"[ぁ-んー]+", surface):
            counts["wago_words"] += 1
        if tag == "VERB":
            counts["verbs"] += 1
        elif tag == "AUXILIARY":
            counts["aux_verbs"] += 1

    total_words = counts["words"] or 1
    p_kanji = counts["kanji_words"] / total_words
    p_wago = counts["wago_words"] / total_words
    p_verb = counts["verbs"] / total_words
    p_aux = counts["aux_verbs"] / total_words

    score = (
        -0.056 * avg_sentence_length
        -0.126 * p_kanji
        -0.042 * p_wago
        -0.145 * p_verb
        -0.044 * p_aux
        + 11.724
    )

    return {
        "score": round(score, 3),
        "level": classify_level(score),
        "sentence_len": round(avg_sentence_length, 2),
        "kanji_ratio": round(p_kanji, 2),
        "wago_ratio": round(p_wago, 2),
        "verb_ratio": round(p_verb, 2),
        "particle_ratio": round(p_aux, 2),
        "sentences_analyzed": total_sentences
    }

# ✅ POST /analyze 엔드포인트
@app.route("/analyze-japanese-news", methods=["POST"])
def analyze_route():
    data = request.get_json()
    text = data.get("text", "")
    if not text.strip():
        return jsonify({"error": "텍스트가 비어 있습니다."}), 400

    print(f"📩 분석 요청 수신됨 | 길이: {len(text)}자")
    result = analyze_text(text)
    print(f"✅ 분석 완료 → 점수: {result['score']}, 레벨: {result['level']}")
    return jsonify(result)

# ✅ 서버 실행
if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=6100)
