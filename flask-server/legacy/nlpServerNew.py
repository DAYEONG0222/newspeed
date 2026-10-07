from flask import Flask, request, jsonify
from google.cloud import language_v1
import os
import spacy
from dotenv import load_dotenv

# ✅ 환경변수 로드
load_dotenv(dotenv_path=".env.nlp")
google_credential_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
if google_credential_path:
    os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = google_credential_path
else:
    raise Exception("❌ GOOGLE_APPLICATION_CREDENTIALS가 설정되지 않았습니다.")

# ✅ 앱 생성
app = Flask(__name__)
nlp = spacy.load("en_core_web_sm")

def get_tree_depth(token):
    if not list(token.children):
        return 1
    return 1 + max(get_tree_depth(child) for child in token.children)

def count_subordinate_clauses(doc):
    return sum(1 for token in doc if token.dep_ == "mark" and token.head.pos_ == "VERB")

def compute_syntactic_features(text):
    doc = nlp(text)
    total_words = len([token for token in doc if token.is_alpha])
    total_chars = sum(len(token.text) for token in doc if token.is_alpha)
    avg_word_length = total_chars / total_words if total_words else 0

    max_depth = 0
    dep_lengths = []
    for sent in doc.sents:
        max_depth = max(max_depth, get_tree_depth(sent.root))
        dep_lengths += [abs(token.i - token.head.i) for token in sent if token.head != token]

    avg_dep_len = sum(dep_lengths) / len(dep_lengths) if dep_lengths else 0
    clause_count = count_subordinate_clauses(doc)
    return total_words, avg_word_length, max_depth, avg_dep_len, clause_count

def normalize(val, max_val, min_val=0):
    return max(min((val - min_val) / (max_val - min_val), 1), 0)

def get_category_norms(category):
    return {
        "politics":  (180, 18, 10),
        "economy":   (170, 17, 9),
        "society":   (160, 16, 8),
        "technology":(165, 17, 9),
        "science":   (190, 19, 11)
    }.get(category.lower(), (170, 17, 9))

@app.route("/analyze", methods=["POST"])
def analyze_text():
    data = request.get_json()
    article_text = data.get("text", "")
    category = data.get("category", "default")

    try:
        if not article_text.strip():
            return jsonify({"error": "본문을 추출할 수 없습니다."}), 400

        total_words, avg_word_len, tree_depth, avg_dep_len, clause_count = compute_syntactic_features(article_text)

        client = language_v1.LanguageServiceClient()
        document = language_v1.Document(content=article_text, type_=language_v1.Document.Type.PLAIN_TEXT)
        entity_response = client.analyze_entities(request={"document": document})
        entities = entity_response.entities
        avg_salience = sum(e.salience for e in entities) / len(entities) if entities else 0
        entity_count = len(entities)

        entity_score = (
            normalize(avg_salience, 1.0) * 0.6 +
            normalize(entity_count, 30) * 0.4
        )

        length_max, depth_max, dep_max = get_category_norms(category)

        score = (
            24.39 * normalize(total_words, length_max) +
            18.61 * normalize(tree_depth, depth_max) +
            17.14 * normalize(avg_dep_len, dep_max) +
            14.26 * normalize(avg_word_len, 10) +
            15.66 * entity_score +
            10.00 * normalize(clause_count, 8)
        )

        if score > 70:
            difficulty = "고급"
        elif score > 63:
            difficulty = "중급"
        else:
            difficulty = "초급"

        print("\U0001F50D 세부 난이도 점수 구성:")
        print(f" - 문장 길이 점수         : {24.39 * normalize(total_words, length_max):.2f} (정규화={normalize(total_words, length_max):.3f})")
        print(f" - 구문 트리 깊이 점수     : {18.61 * normalize(tree_depth, depth_max):.2f} (정규화={normalize(tree_depth, depth_max):.3f})")
        print(f" - 의존 거리 평균 점수     : {17.14 * normalize(avg_dep_len, dep_max):.2f} (정규화={normalize(avg_dep_len, dep_max):.3f})")
        print(f" - 평균 단어 길이 점수     : {14.26 * normalize(avg_word_len, 10):.2f} (정규화={normalize(avg_word_len, 10):.3f})")
        print(f" - 엔터티 점수              : {15.66 * entity_score:.2f} (정규화={entity_score:.3f})")
        print(f" - 종속절 수 점수          : {10.00 * normalize(clause_count, 8):.2f} (정규화={normalize(clause_count, 8):.3f})")
        print(f"✅ 최종 점수               : {score:.2f} → 난이도: {difficulty}")

        return jsonify({
            "difficulty": difficulty,
            "score": round(score, 2),
            "features": {
                "total_words": total_words,
                "avg_word_length": round(avg_word_len, 2),
                "tree_depth": tree_depth,
                "avg_dep_len": round(avg_dep_len, 2),
                "entity_count": entity_count,
                "avg_salience": round(avg_salience, 4),
                "entity_score": round(entity_score, 4),
                "clause_count": clause_count,
                "category": category
            }
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=6000) 