**Polski** · [English](README.md) · [简体中文](README_zh.md)

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
## 💬 Cytat z szuflady
<!-- markdownlint-disable MD033 -->
<!--STARTS_HERE_QUOTE_README-->
<i>❝The Space Shuttle never flew on new year’s day or eve because its computers couldn’t handle a year rollover.❞</i>
<!--ENDS_HERE_QUOTE_README-->
<!-- markdownlint-enable MD033 -->

## 📰 Ostatnio w eterze
<!--README_FEED:START-->
- [Urban Word of the Day — grebo](https://www.urbandictionary.com/define.php?term=grebo&defid=1975218)
- [How to Engage with New Media: A Strategic Guide for Nonprofit Organizations](https://carnegieendowment.org/research/2026/08/how-to-engage-with-new-media-a-strategic-guide-for-nonprofit-organizations)
- [Urban Word of the Day — board chow](https://www.urbandictionary.com/define.php?term=board%20chow&defid=2568411)
- [Pasażerowie mogą odetchnąć z ulgą! Koniec tymczasowego przystanku w Chrzanowie - Przelom.pl - portal ziemi chrzanowskiej](https://news.google.com/atom/articles/CBMixAFBVV95cUxOS3BfMTRsT1NSLWFBRXQwdHpoYU1rOTVKNkJ4T0M2LWRHWFZ1NGVpanVyR2lEQ1pxVXFScDVKUV9janVRNEk0VGJDTkdLYThvU1VPSHBzeDdkM3lqMEpjcHRpU2d6VGVLQjZRZzBybTJvaGw4RHBlX0pnOFdhR3A0VzdIcEVfdG5BRnVwTTRvX3BvUXRkenVBekliN0s0NTdzYWJ6Mk4xVFV1Y0ljMnVsUW82ZmZNT0VMMF9GMFF2WXNhcVZq?oc=5)
- [ZKKM zmienił komunikat. Te autobusy ominą część Libiąża - Przelom.pl - portal ziemi chrzanowskiej](https://news.google.com/atom/articles/CBMipgFBVV95cUxPQXplaXdNTDRaSWN4bWJ1LWNMN2pTYTZhelVOenZDdlgzUTJEV1hTTGFyRFd1RXlRMldVblNRcFk3Y2hRclQ0UGxVbDQxUy1JaHpaTl9ZSXF1bkZmLXRXZDlFZk0tUmN2WXFwVHpPSV9fOVlsbkRORHBVS2pVdy1aRDV1WGZDVmptQUdBRHhVcVpJUmpUUWk1SmJnZjJFaFhfa1U5cW1B?oc=5)
- [Drugie takie miejsce w Małopolsce. Zaplecze techniczne Kolei Małopolskich rośnie w Oświęcimiu - oswiecimonline.pl](https://news.google.com/atom/articles/CBMiwAFBVV95cUxQb3dBZkZFZ3R4SWgycm1uNC1RR2tMYTdUTUh2X21jQk02X1JJWmpXZUZ4cUNPM1R2TXh0Y0ZDajhuQzJRTDQyWTVYa2RwSlFPdlBTYlZobVhMQzVIQlVWeHBXOW9KZU1RMzVrbEZEbDdtdTA4QUJsMkRSTTVQb0NwU2VsWkZaVjlOVEFVdHFBb2c5ZGVRcGhOU3huZWdjRmtfQ1NkdENTUGMwUFF2WUx4NXV3SEYzbkdNNTR0M0JkTm8?oc=5)
<!--README_FEED:END-->
