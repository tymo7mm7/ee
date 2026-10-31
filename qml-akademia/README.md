# QML Akademia ⚛️🧠

Aplikacja do nauki i powtórek: **informatyka kwantowa**, **algorytmy kwantowe**, **kwantowe uczenie maszynowe (QML)** i **klasyczne uczenie maszynowe**. Działa w pełni offline — na Androidzie (plik APK) i w przeglądarce.

📦 **Plik instalacyjny:** [`release/QML-Akademia-1.0.apk`](../release/QML-Akademia-1.0.apk) (ok. 0,7 MB, Android 7.0+)

## Co jest w środku

| Dział | Lekcje | Fiszki | Pytania |
|---|---|---|---|
| Informatyka kwantowa — podstawy | 8 | 45 | 32 |
| Algorytmy kwantowe | 8 | 42 | 27 |
| **Kwantowe uczenie maszynowe (QML)** | 15 | 86 | 58 |
| Uczenie maszynowe (klasyczne) | 13 | 72 | 42 |
| **Razem** | **44** | **245** | **159** |

- **Lekcje** z wzorami (KaTeX), ramkami „najważniejsze / uwaga / na egzaminie”, przykładami kodu (PennyLane, Qiskit ML, scikit‑learn).
- **Fiszki z powtórkami rozłożonymi w czasie** (algorytm SM‑2): oceniasz „Nie pamiętam / Trudne / Dobrze / Łatwe”, a aplikacja planuje kolejną powtórkę. Dzienna „Powtórka dnia” i licznik dni nauki z rzędu.
- **Quizy** wielokrotnego wyboru z wyjaśnieniami: losowe, „niewidziane” albo „moje błędy”; mini‑quiz po każdej lekcji.
- **Laboratorium** (symulacje liczone na telefonie):
  - 🌐 **Sfera Blocha** — bramki X, Y, Z, H, S, T, rotacje, pomiar z animacją obrotów,
  - 🔌 **Symulator obwodów** (1–4 kubity) — m.in. stan Bella, GHZ, Deutsch–Jozsa, Grover, teleportacja, QFT; amplitudy, prawdopodobieństwa, sfery Blocha kubitów (widać splątanie),
  - 🤖 **VQC vs klasyczne ML** — trening wariacyjnego klasyfikatora kwantowego (data re‑uploading, gradient z reguły parameter‑shift) na żywo, w porównaniu z regresją logistyczną i MLP,
  - 🧩 **Kernele kwantowe** — macierz Grama, szerokość pasma, klasyfikator jądrowy (mapa kątowa, ZZ feature map, RBF),
  - 📈 **Reguła parameter‑shift** — dokładny gradient vs różnice skończone przy szumie pomiarowym.
- **Ściąga ze wzorami**, **wyszukiwarka**, **statystyki** (kalendarz aktywności, najtrudniejsze fiszki), **jasny/ciemny motyw**, **eksport/import postępów**.

## Instalacja na Androidzie

1. Pobierz `release/QML-Akademia-1.0.apk` na telefon (np. z GitHuba: plik → „Download raw file”).
2. Otwórz plik i zezwól przeglądarce / menedżerowi plików na **instalowanie nieznanych aplikacji**.
3. Zainstaluj. Aplikacja nie potrzebuje internetu ani żadnych uprawnień poza standardowym dostępem do sieci WebView.

> APK jest podpisany kluczem deweloperskim (nie pochodzi ze Sklepu Play), więc Android może wyświetlić ostrzeżenie Play Protect — to normalne dla aplikacji instalowanych ręcznie.

## Uruchomienie w przeglądarce

Otwórz `web/index.html` bezpośrednio albo przez lokalny serwer:

```bash
python3 -m http.server 8000 -d qml-akademia/web
# potem: http://localhost:8000
```

## Struktura

```
qml-akademia/
├── content/              # treści (Markdown): lekcje, fiszki, quizy, ściąga
│   ├── 1-qc/ 2-alg/ 3-qml/ 4-ml/
│   └── cheatsheet.md
├── web/                  # aplikacja (HTML/CSS/JS, bez frameworków)
│   ├── js/app.js             # router, ekran startowy, wyszukiwarka, ustawienia
│   ├── js/views-learn.js     # działy, lekcje, ściąga
│   ├── js/views-practice.js  # fiszki (SM-2) i quizy
│   ├── js/store.js           # zapis postępów (localStorage)
│   ├── js/qsim.js            # symulator wektora stanu
│   ├── js/lab-*.js           # laboratorium
│   ├── js/content.js         # generowany z content/
│   └── vendor/katex/         # KaTeX (MIT)
├── android/              # opakowanie WebView (Java) + zasoby i ikony
└── tools/
    ├── build_content.py  # content/*.md → web/js/content.js
    ├── build_apk.sh      # buduje i podpisuje APK
    └── make_icons.js     # generuje ikony z SVG (Playwright)
```

## Edycja treści

Każdy plik w `content/<dział>/` może zawierać kilka lekcji rozdzielonych linią `%%%`. Lekcja:

```
---
id: qml-przyklad
title: Tytuł lekcji
summary: Jedno zdanie opisu.
level: podstawowy | średni | zaawansowany
lab: bloch | circuit | train | kernel | shift   (opcjonalnie)
---
Treść w Markdown, wzory $...$ i $$...$$, ramki > [!key], > [!warn], > [!tip] ...

@@@ fiszki
Q: Pytanie?
A: Odpowiedź.

@@@ quiz
? Treść pytania
- zła odpowiedź
+ poprawna odpowiedź
- zła odpowiedź
! Wyjaśnienie.
```

Po zmianach: `python3 tools/build_content.py`.

## Budowanie APK

Bez Android Studio — skrypt używa narzędzi z pakietów Ubuntu/Debian i kompilatora D8:

```bash
sudo apt install aapt apksigner zipalign android-sdk-platform-23 openjdk-17-jdk zip
curl -o /opt/r8/r8lib-8.7.18.jar https://storage.googleapis.com/r8-releases/raw/8.7.18/r8lib.jar
./qml-akademia/tools/build_apk.sh      # wynik: release/QML-Akademia-<wersja>.apk
```

Przy pierwszym uruchomieniu skrypt tworzy klucz podpisu w `qml-akademia/signing/` (jest w `.gitignore`). Aktualizacja zainstalowanej aplikacji wymaga podpisu tym samym kluczem — przy innym kluczu trzeba odinstalować starą wersję (wcześniej warto wyeksportować postępy w „Więcej → Kopia postępów”).

Szczegóły techniczne: minSdk 24, targetSdk 34, aplikacja webowa serwowana z `assets/www` pod adresem `https://appassets.androidplatform.net/` (przechwytywanym lokalnie), podpis APK v2 + v3.

## Licencje

Kod i treści aplikacji: na potrzeby nauki. [KaTeX](https://katex.org) — licencja MIT (`web/vendor/katex/LICENSE`).
