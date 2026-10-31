#!/usr/bin/env python3
"""Zbiera treści z katalogu content/ i zapisuje je do web/js/content.js.

Struktura:
  content/<NN-dzial>/_section.md   - opis działu (front matter)
  content/<NN-dzial>/<NN-plik>.md   - lekcje (kilka w pliku, rozdzielone linią "%%%");
                                      każda: front matter, treść Markdown,
                                      potem opcjonalnie bloki "@@@ fiszki" i "@@@ quiz"
  content/cheatsheet.md            - ściąga ze wzorami

Format fiszek:
  Q: pytanie (może mieć kilka linii)
  A: odpowiedź (może mieć kilka linii)

Format quizu:
  ? treść pytania
  - błędna odpowiedź
  + poprawna odpowiedź
  ! wyjaśnienie
"""
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CONTENT = ROOT / "content"
OUT = ROOT / "web" / "js" / "content.js"


class ContentError(Exception):
    pass


def parse_front_matter(text, path):
    if not text.startswith("---\n"):
        raise ContentError(f"{path}: brak front matter")
    end = text.find("\n---\n", 4)
    if end < 0:
        raise ContentError(f"{path}: niezamknięty front matter")
    meta = {}
    for line in text[4:end].splitlines():
        if not line.strip():
            continue
        key, _, value = line.partition(":")
        meta[key.strip()] = value.strip()
    return meta, text[end + 5:]


def parse_cards(block, lesson_id, path):
    cards = []
    cur = None
    field = None
    for raw in block.splitlines():
        line = raw.rstrip()
        if line.startswith("Q:"):
            if cur:
                cards.append(cur)
            cur = {"q": line[2:].strip(), "a": ""}
            field = "q"
        elif line.startswith("A:"):
            if cur is None:
                raise ContentError(f"{path}: odpowiedź bez pytania: {line}")
            cur["a"] = line[2:].strip()
            field = "a"
        elif cur is not None and field:
            cur[field] += "\n" + line
    if cur:
        cards.append(cur)
    out = []
    for i, c in enumerate(cards):
        q, a = c["q"].strip(), c["a"].strip()
        if not q or not a:
            raise ContentError(f"{path}: niepełna fiszka nr {i + 1}")
        out.append({"id": f"{lesson_id}.c{i + 1}", "lesson": lesson_id, "q": q, "a": a})
    return out


def parse_quiz(block, lesson_id, path):
    items = []
    cur = None
    last = None
    for raw in block.splitlines():
        line = raw.rstrip()
        if not line.strip():
            continue
        tag, rest = line[:1], line[1:].strip()
        if tag == "?":
            if cur:
                items.append(cur)
            cur = {"q": rest, "options": [], "answer": -1, "explain": ""}
            last = "q"
        elif tag in "-+" and cur is not None and line[1:2] == " ":
            if tag == "+":
                if cur["answer"] >= 0:
                    raise ContentError(f"{path}: dwie poprawne odpowiedzi w: {cur['q']}")
                cur["answer"] = len(cur["options"])
            cur["options"].append(rest)
            last = "o"
        elif tag == "!" and cur is not None:
            cur["explain"] = rest
            last = "e"
        elif cur is not None and last == "q":
            cur["q"] += "\n" + line.strip()
        elif cur is not None and last == "e":
            cur["explain"] += " " + line.strip()
        else:
            raise ContentError(f"{path}: nieznana linia quizu: {line}")
    if cur:
        items.append(cur)
    out = []
    for i, it in enumerate(items):
        if it["answer"] < 0 or len(it["options"]) < 2:
            raise ContentError(f"{path}: pytanie bez poprawnej odpowiedzi: {it['q']}")
        it["id"] = f"{lesson_id}.q{i + 1}"
        it["lesson"] = lesson_id
        out.append(it)
    return out


def strip_md(text):
    text = re.sub(r"```.*?```", " ", text, flags=re.S)
    text = re.sub(r"\$\$.*?\$\$", " ", text, flags=re.S)
    text = re.sub(r"[#>*`|_]", " ", text)
    return re.sub(r"\s+", " ", text).strip()


def build():
    sections, lessons, cards, quiz = [], [], [], []
    ids = set()
    for sdir in sorted(p for p in CONTENT.iterdir() if p.is_dir()):
        smeta, sbody = parse_front_matter((sdir / "_section.md").read_text("utf-8"), sdir)
        section = {
            "id": smeta["id"],
            "title": smeta["title"],
            "short": smeta.get("short", smeta["title"]),
            "icon": smeta.get("icon", ""),
            "color": smeta.get("color", "#7c5cff"),
            "desc": sbody.strip(),
            "lessons": [],
        }
        chunks = []
        for lfile in sorted(sdir.glob("[0-9]*.md")):
            # jeden plik może zawierać kilka lekcji rozdzielonych linią "%%%"
            for chunk in re.split(r"^%%%\s*$", lfile.read_text("utf-8"), flags=re.M):
                if chunk.strip():
                    chunks.append((lfile, chunk.strip() + "\n"))
        for lfile, text in chunks:
            meta, body = parse_front_matter(text, lfile)
            lid = meta["id"]
            if lid in ids:
                raise ContentError(f"{lfile}: powtórzone id {lid}")
            ids.add(lid)
            parts = re.split(r"^@@@\s*(\w+)\s*$", body, flags=re.M)
            main = parts[0].strip()
            lcards, lquiz = [], []
            for name, block in zip(parts[1::2], parts[2::2]):
                if name == "fiszki":
                    lcards = parse_cards(block, lid, lfile)
                elif name == "quiz":
                    lquiz = parse_quiz(block, lid, lfile)
                else:
                    raise ContentError(f"{lfile}: nieznany blok @@@ {name}")
            words = len(strip_md(main).split())
            lessons.append({
                "id": lid,
                "section": section["id"],
                "title": meta["title"],
                "summary": meta.get("summary", ""),
                "level": meta.get("level", "podstawowy"),
                "lab": meta.get("lab", ""),
                # tekst techniczny czyta się wolniej; wzory blokowe liczymy osobno
                "minutes": max(2, round(words / 110 + main.count("$$") / 2 * 0.4)),
                "body": main,
                "nCards": len(lcards),
                "nQuiz": len(lquiz),
            })
            section["lessons"].append(lid)
            cards.extend(lcards)
            quiz.extend(lquiz)
        sections.append(section)

    cheat = ""
    cheat_file = CONTENT / "cheatsheet.md"
    if cheat_file.exists():
        cheat = cheat_file.read_text("utf-8")

    data = {"sections": sections, "lessons": lessons, "cards": cards, "quiz": quiz, "cheatsheet": cheat}
    js = "/* Plik generowany przez tools/build_content.py - nie edytuj ręcznie. */\n"
    js += "window.QA_CONTENT = " + json.dumps(data, ensure_ascii=False, separators=(",", ":")) + ";\n"
    OUT.write_text(js, "utf-8")
    print(f"Treści: {len(sections)} działy, {len(lessons)} lekcji, {len(cards)} fiszek, "
          f"{len(quiz)} pytań quizowych -> {OUT.relative_to(ROOT)}")


if __name__ == "__main__":
    try:
        build()
    except ContentError as e:
        print("BŁĄD:", e, file=sys.stderr)
        sys.exit(1)
