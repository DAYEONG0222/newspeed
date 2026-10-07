"""번역·유의어처럼 외부 유료/키 API 가 필요한 기능.

키가 없으면 서버 전체가 죽지 않고 해당 엔드포인트만 503 을 돌려준다.
(기존에는 키가 없으면 서버 시작 단계에서 예외가 나서 프로세스가 뜨지 않았다.)
"""

import os

import requests


class FeatureUnavailable(Exception):
    pass


_translator = None


def translate(text: str, target_language: str = "ko") -> str:
    global _translator
    if not os.getenv("GOOGLE_APPLICATION_CREDENTIALS"):
        raise FeatureUnavailable("GOOGLE_APPLICATION_CREDENTIALS 가 설정되지 않아 번역을 쓸 수 없습니다.")
    if _translator is None:
        from google.cloud import translate_v2  # 키가 있을 때만 의존성 필요

        _translator = translate_v2.Client()
    return _translator.translate(text, target_language=target_language)["translatedText"]


def synonyms(word: str, lang: str) -> list[str]:
    key = os.getenv("BABELNET_KEY")
    if not key:
        raise FeatureUnavailable("BABELNET_KEY 가 설정되지 않아 유의어를 쓸 수 없습니다.")
    lang_code = {"ja": "JA", "es": "ES"}[lang]
    response = requests.get(
        "https://babelnet.io/v6/getSenses",
        params={"lemma": word, "searchLang": lang_code, "langs": lang_code, "key": key},
        timeout=10,
    )
    response.raise_for_status()
    found = set()
    for sense in response.json():
        lemma = sense.get("properties", {}).get("fullLemma")
        if lemma and lemma.lower() != word.lower():
            found.add(lemma)
    return sorted(found)
