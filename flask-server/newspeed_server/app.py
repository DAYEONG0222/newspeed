import logging

from flask import Flask, jsonify, request
from werkzeug.exceptions import HTTPException

from . import external
from .batch import NLP_LOCK, analyze_many, analyze_text_locked
from .cache import TTLCache
from .difficulty import SUPPORTED_LANGS, japanese
from .extract import ExtractError, fetch_article_text

MAX_TEXT_CHARS = 50_000
MAX_BATCH_URLS = 20

log = logging.getLogger("newspeed")


def _bad_request(message: str):
    return jsonify({"error": message}), 400


def _text_from_body(body: dict):
    text = body.get("text", "")
    if not isinstance(text, str) or not text.strip():
        return None, _bad_request("텍스트가 비어 있습니다.")
    if len(text) > MAX_TEXT_CHARS:
        return None, _bad_request(f"텍스트가 너무 깁니다 (최대 {MAX_TEXT_CHARS}자).")
    return text, None


def create_app(cache_ttl_seconds: float = 600.0) -> Flask:
    app = Flask(__name__)
    app.json.ensure_ascii = False
    cache = TTLCache(cache_ttl_seconds)
    app.config["ARTICLE_CACHE"] = cache

    @app.get("/health")
    def health():
        return jsonify({"status": "ok", "cache": cache.stats()})

    # ---- 본문 추출 / 토큰화 (기존 extract_server.py) ----
    @app.post("/extract")
    def extract():
        url = (request.get_json(silent=True) or {}).get("url")
        if not url:
            return _bad_request("url 이 필요합니다.")
        try:
            return jsonify({"url": url, "text": fetch_article_text(url)})
        except ExtractError as e:
            return _bad_request(str(e))

    @app.post("/tokenize-ja")
    def tokenize_ja():
        text = (request.get_json(silent=True) or {}).get("text", "")
        if not isinstance(text, str) or not text.strip():
            return jsonify({"tokens": []})
        with NLP_LOCK:  # MeCab 인스턴스는 스레드 안전하지 않음
            tokens = japanese.tokenize(text)
        return jsonify({"tokens": tokens})

    # ---- 언어별 난이도 (기존 경로 유지 → 앱 수정 최소화) ----
    def _analyze_route(lang: str):
        body = request.get_json(silent=True) or {}
        text, error = _text_from_body(body)
        if error:
            return error
        return jsonify(analyze_text_locked(text, lang, body.get("category", "default")))

    app.add_url_rule("/analyze", "analyze_en", lambda: _analyze_route("en"), methods=["POST"])
    app.add_url_rule("/analyze-japanese-news", "analyze_ja", lambda: _analyze_route("ja"), methods=["POST"])
    app.add_url_rule("/analyze-es", "analyze_es", lambda: _analyze_route("es"), methods=["POST"])

    # ---- 새 엔드포인트: URL 목록을 한 번에 추출·분석 ----
    @app.post("/articles/analyze")
    def articles_analyze():
        body = request.get_json(silent=True) or {}
        lang = body.get("lang")
        urls = body.get("urls")
        if lang not in SUPPORTED_LANGS:
            return _bad_request(f"lang 은 {SUPPORTED_LANGS} 중 하나여야 합니다.")
        if not isinstance(urls, list) or not urls or not all(isinstance(u, str) for u in urls):
            return _bad_request("urls 는 문자열 목록이어야 합니다.")
        if len(urls) > MAX_BATCH_URLS:
            return _bad_request(f"한 번에 최대 {MAX_BATCH_URLS}개까지 요청할 수 있습니다.")
        results = analyze_many(urls, lang, body.get("category", "default"), cache=cache)
        return jsonify({"lang": lang, "results": results})

    # ---- 키가 필요한 외부 기능 ----
    @app.post("/translate")
    def translate():
        body = request.get_json(silent=True) or {}
        text, error = _text_from_body(body)
        if error:
            return error
        try:
            translated = external.translate(text, body.get("target_language", "ko"))
        except external.FeatureUnavailable as e:
            return jsonify({"error": str(e)}), 503
        return jsonify({"translated_text": translated})

    @app.post("/synonyms")
    def synonyms():
        body = request.get_json(silent=True) or {}
        word, lang = body.get("word"), body.get("lang")
        if not word or lang not in ("ja", "es"):
            return _bad_request("word 와 lang(ja 또는 es)이 필요합니다.")
        try:
            return jsonify({"synonyms": external.synonyms(word, lang), "antonyms": []})
        except external.FeatureUnavailable as e:
            return jsonify({"error": str(e)}), 503

    @app.errorhandler(Exception)
    def handle_unexpected(e):
        if isinstance(e, HTTPException):  # 404·405 등은 그대로
            return e
        log.exception("처리 중 오류")
        return jsonify({"error": f"{type(e).__name__}: {e}"}), 500

    return app
