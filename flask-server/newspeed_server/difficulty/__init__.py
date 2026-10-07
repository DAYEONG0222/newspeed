from . import english, japanese, spanish

SUPPORTED_LANGS = ("en", "ja", "es")


def analyze(text: str, lang: str, category: str = "default") -> dict:
    if lang == "ja":
        return japanese.analyze(text)
    if lang == "es":
        return spanish.analyze(text)
    if lang == "en":
        return english.analyze(text, category)
    raise ValueError(f"지원하지 않는 언어: {lang}")
