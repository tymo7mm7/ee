# Zadania – aplikacja na Androida

Lista zadań: priorytety, terminy z kalendarza, wyszukiwanie, filtry, pasek postępu,
cofanie usunięcia, ciemny motyw i kolory z tapety (Android 12+). Dane zapisują się na telefonie.

## Jak pobrać APK
1. Wejdź w zakładkę **Releases** repozytorium na GitHubie.
2. Pobierz `Zadania.apk` z najnowszego wydania.
3. Otwórz plik na telefonie i zezwól na instalację z nieznanych źródeł.

APK buduje się automatycznie (GitHub Actions) po każdej zmianie w folderze `android/`.

## Technologie
Kotlin, Jetpack Compose, Material 3. Min. Android 8.0 (API 26).

> `app/demo-release.jks` to klucz demonstracyjny. Przed publikacją w Google Play
> zastąp go prywatnym kluczem trzymanym w GitHub Secrets.
