[English](README.md) | **Polski**

<div align="center">

<img src="assets/kanarek.svg" alt="Kanarek" width="96">

# Kanarek

**Czytnik i widżet wiadomości oraz odtwarzacz radia/IPTV dla Androida.**

[![android CI](https://img.shields.io/github/actions/workflow/status/trvny/kanarek/android-ci.yml?label=android%20CI&logo=android&logoColor=111&color=FFC107&style=flat-square)](https://github.com/trvny/kanarek/actions/workflows/android-ci.yml)
[![worker CI](https://img.shields.io/github/actions/workflow/status/trvny/kanarek/worker-ci.yml?label=worker%20CI&logo=cloudflare&logoColor=111&color=FFC107&style=flat-square)](https://github.com/trvny/kanarek/actions/workflows/worker-ci.yml)
[![last commit](https://img.shields.io/github/last-commit/trvny/kanarek?color=FFC107&logo=git&logoColor=111&style=flat-square)](https://github.com/trvny/kanarek/commits/main)
[![license](https://img.shields.io/github/license/trvny/kanarek?color=FFC107&style=flat-square)](LICENSE)  
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat-square&logo=typescript&logoColor=white)
![Cloudflare Worker](https://img.shields.io/badge/Worker-F38020?style=flat-square&logo=cloudflareworkers&logoColor=white)  
<a href="https://deepwiki.com/trvny/kanarek"><img src="https://deepwiki.com/badge.svg" alt="DeepWiki"></a>

</div>

Kanarek łączy dwa narzędzia w jednej natywnej aplikacji:

- czytnik RSS/Atom z automatycznym pokazem wiadomości i widżetem ekranu głównego,
- odtwarzacz radia internetowego i IPTV działający w tle, również z własnym widżetem.

Opcjonalny Cloudflare Worker przyspiesza pobieranie i dodaje funkcje sieciowe. Nie jest wymagany: zwykłe kanały RSS/Atom mogą być przetwarzane bezpośrednio na urządzeniu.

## Najważniejsze funkcje

### Wiadomości

- skalowalny widżet z pokazem slajdów, ręczną nawigacją i osobnymi ustawieniami każdego egzemplarza,
- własne źródła RSS 2.0 i Atom, import oraz eksport OPML,
- wyszukiwanie wiadomości, filtrowanie źródeł i tryb najważniejszych nagłówków,
- oznaczanie jako przeczytane, zapisywanie artykułów oraz opcjonalny tekst offline,
- podgląd czystej treści artykułu przy skonfigurowanym Workerze,
- opcjonalne powiadomienia o nowych wiadomościach z godzinami ciszy,
- zachowanie ostatnich poprawnych wiadomości, gdy pojedyncze źródło chwilowo nie działa.

### Radio i IPTV

- odtwarzanie w tle przez Media3/ExoPlayer z kontrolkami systemowymi,
- import, eksport i edycja playlist M3U/M3U8,
- radio, telewizja, grupy kanałów, ulubione stacje i metadane aktualnego utworu,
- wyszukiwanie stacji w katalogu Radio Browser,
- uzupełnianie brakujących logotypów kanałów przez iptv-org i favikony,
- obsługa `User-Agent` oraz `Referer` zapisanych w playliście,
- Google Cast w wariancie `play`; wariant `foss` nie wymaga usług Google.

## Instalacja

[APK](https://github.com/trvny/kanarek/releases).

- `play`: zawiera obsługę Google Cast,
- `foss`: wariant bez GMS, przeznaczony dla środowisk FOSS i F-Droid.

Minimalna wersja systemu to Android 8.0 (API 26).

## Szybki start

1. Zainstaluj wybrany wariant APK.
2. Otwórz Kanarka i wybierz **Wiadomości** albo **Radio i TV**.
3. Dodaj własne źródła RSS/Atom lub zaimportuj OPML.
4. Dodaj stacje ręcznie, wyszukaj radio albo zaimportuj playlistę M3U/M3U8.
5. Przytrzymaj ekran główny Androida, otwórz **Widżety** i dodaj wybrany widżet Kanarka.

Pole Backend URL może pozostać puste: zwykłe feedy są wtedy odświeżane na urządzeniu, a odkrywanie źródeł, wyszukiwanie stacji i dobieranie logo mogą korzystać z wbudowanego domyślnego serwisu Kanarka. Własny adres Workera ustaw dopiero wtedy, gdy chcesz kierować przez niego normalne odświeżanie feedów albo używać funkcji zależnych od świadomej konfiguracji operatora, takich jak czysty czytnik i synchronizacja stanu.

## Dokumentacja

- [Architektura](docs/ARCHITECTURE.md)
- [Budowanie, testy i CI](docs/DEVELOPMENT.md)
- [Cloudflare Worker i API](docs/WORKER.md)
- [Historia projektu](docs/HISTORY.md)
- [Notatki do zgłoszenia w F-Droidzie](docs/FDROID.md)

Kanarek powstawał w repozytorium [trvny/feeds](https://github.com/trvny/feeds) do sierpnia 2026 i
został stamtąd wydzielony razem z całą historią.

## Rozwój

Skrypty Gradle wrappera celowo nie są commitowane. Na świeżym klonie zainstaluj dokładną wersję Gradle wskazaną w `gradle/wrapper/gradle-wrapper.properties`, a następnie utwórz wrapper przed użyciem `./gradlew`:

```bash
GRADLE_VERSION=$(sed -n 's#^distributionUrl=.*/gradle-\([0-9][A-Za-z0-9.-]*\)-\(bin\|all\)\.zip$#\1#p' gradle/wrapper/gradle-wrapper.properties)
command -v gradle >/dev/null || { echo "Najpierw zainstaluj Gradle $GRADLE_VERSION" >&2; exit 1; }
gradle --version | grep -F "Gradle $GRADLE_VERSION" >/dev/null || { echo "Do utworzenia wrappera użyj Gradle $GRADLE_VERSION" >&2; exit 1; }
gradle wrapper --gradle-version "$GRADLE_VERSION" --no-daemon
./gradlew assembleDebug
./gradlew testPlayDebugUnitTest
```

Konfiguracja zależna od systemu i bootstrap równoważny z CI są opisane w [dokumencie developerskim](docs/DEVELOPMENT.md).

## Licencja

Projekt jest udostępniany na warunkach licencji opisanej w [LICENSE](LICENSE).

---
## 💬 Quote from the drawer
<!-- markdownlint-disable MD033 -->
<!--STARTS_HERE_QUOTE_README-->
<i>❝That men do not learn very much from the lessons of history is the most important of all the lessons that history has to teach. — Aldous Huxley❞</i>
<!--ENDS_HERE_QUOTE_README-->
<!-- markdownlint-enable MD033 -->

## 📰 Recently on the air
<!--README_FEED:START-->
- [Muzyczna niedziela w Chrzanowie i Wygiełzowie. Wyrostek, filmowe przeboje i Janosik w skansenie - Przelom.pl](https://news.google.com/atom/articles/CBMiwgFBVV95cUxQakE4NzZHR0gzN2h6VGZudUpKX0dPWjRBWm4yR0lYX09fVEgyMklySmFDYzhMN2NXcHlBcXFvOG9kYTB2Z0d3SjZUalIwdzJ1M1gtUHF3b3dxOXBtZ0xTZXhlb19LcXNMSGcySHBtVGFiWElJaUltbmVvQmgxcmlJVkx5RlRCSEVTaVRhWm5OSllyYmM5ZkZvRkFsQnRGX1BTNExTLWVvVll2dXRiWjFVZS1JcGkwVUM4UTJhdjQwZ21yQQ?oc=5)
- [Mieszkańcy krytykują przebudowę ul. Chrzanowskiej - Przelom.pl](https://news.google.com/atom/articles/CBMimwFBVV95cUxOLTRpajJHSEhodVN2bU5MRUwwdDJRNHl3SDE4UzZMSzJ4Q0pFTThoWWxocVRGazN4eUtoa3ZmTWRHUkRFUVNzbmlFRkdQWnBmTGNBYTU5V1hPQzlGMUdwTEhFNGp5blVxcnpYSGt2cE9zOHRlRG5DSzRHYnE2RlVuY3ZyX2wtaWttYlBSMXlTY0djVmxWYlJnSXBEQQ?oc=5)
- [Alex Jones gets Sandy Hook family's Texas verdict reduced on appeal](https://www.reuters.com/legal/government/alex-jones-gets-sandy-hook-familys-texas-verdict-reduced-appeal-2026-08-21/)
- [Polski podatek tokenowy: ile więcej płacisz za AI, bo piszesz po polsku](https://promptowy.com/polski-podatek-tokenowy-ile-drozej-ai/)
- [US EPA to extend renewable fuel standard compliance deadline for refiners](https://www.reuters.com/business/energy/epa-extends-renewable-fuel-standard-compliance-deadline-refiners-2026-08-21/)
- [Supreme Court lets Trump continue work on White House ballroom for now](https://www.reuters.com/world/supreme-court-lets-trump-continue-work-white-house-ballroom-now-2026-08-21/)
<!--README_FEED:END-->
