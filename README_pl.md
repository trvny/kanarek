**Polski** · [English](README.md) · [简体中文](README_zh.md)

<div align="center">

<img src="assets/kanarek.svg" alt="Kanarek" width="96">

# Kanarek

**Czytnik i widżet wiadomości oraz odtwarzacz radia/IPTV dla Androida.**

[![android CI](https://img.shields.io/github/actions/workflow/status/twojstar/kanarek/android-ci.yml?label=android%20CI&logo=android&logoColor=111&color=FFC107&style=flat-square)](https://github.com/twojstar/kanarek/actions/workflows/android-ci.yml)
[![worker CI](https://img.shields.io/github/actions/workflow/status/twojstar/kanarek/worker-ci.yml?label=worker%20CI&logo=cloudflare&logoColor=111&color=FFC107&style=flat-square)](https://github.com/twojstar/kanarek/actions/workflows/worker-ci.yml)
[![last commit](https://img.shields.io/github/last-commit/twojstar/kanarek?color=FFC107&logo=git&logoColor=111&style=flat-square)](https://github.com/twojstar/kanarek/commits/main)
[![license](https://img.shields.io/github/license/twojstar/kanarek?color=FFC107&style=flat-square)](LICENSE)<br>
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white) ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat-square&logo=typescript&logoColor=white) ![Cloudflare Worker](https://img.shields.io/badge/Worker-F38020?style=flat-square&logo=cloudflareworkers&logoColor=white)<br>
<a href="https://deepwiki.com/twojstar/kanarek"><img src="https://deepwiki.com/badge.svg" alt="DeepWiki"></a>

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

[APK](https://github.com/twojstar/kanarek/releases).

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

## [Licencja](LICENSE)

[![License](https://www.shieldcn.dev/github/license/twojstar/kanarek.svg?variant=branded&size=xm&mode=light&theme=neutral&font=jetbrains-mono)](https://spdx.org/licenses/MIT)

---
## 💬 Cytat z szuflady
<!-- markdownlint-disable MD033 -->
<!--STARTS_HERE_QUOTE_README-->
<i>❝“Code generation, like drinking alcohol, is good in moderation.”— Alex Lowe❞</i>
<!--ENDS_HERE_QUOTE_README-->
<!-- markdownlint-enable MD033 -->

## 📰 Ostatnio w eterze
<!--README_FEED:START-->
- [How to Engage with New Media: A Strategic Guide for Nonprofit Organizations](https://carnegieendowment.org/research/2026/08/how-to-engage-with-new-media-a-strategic-guide-for-nonprofit-organizations)
- [Powiatowi społecznicy spotkają się w Libiążu - Przelom.pl - portal ziemi chrzanowskiej](https://news.google.com/atom/articles/CBMilgFBVV95cUxQcEhKWlVWS1hZNE9yNGtrTVhmU29Jd2pxTEk2SFlIb2xybWxDVW9ybi1FLUVRanVjZERONVl5V2Y5WEhuZkVKRlM5V0FyVmI5aDNyYlZoSTJHZFNMemlWczZIRExSQnF2amNPVENBemo4UGRMekVKUUlCODVwWjBSRmE4MHI2dDljZGtGTHNsRVEtZ2hMWGc?oc=5)
- [W Puszczy Dulowskiej powstanie rezerwat? Jest oficjalny wniosek - Przelom.pl - portal ziemi chrzanowskiej](https://news.google.com/atom/articles/CBMiuwFBVV95cUxNVmFGeGRzUF9JWjVoSVFpREs3cmY0U2ZZQ1VLc05VSlI2NUs2X05kM292YnVNdGpGR3VmVktDaHhodDVLd1k4eHNkOU9DdVRxbllhcV9YSzU3ZVk0VXlOVm1Fang5a0dMNEE5b29ZY0ZwUzQ4a3ZudEtlLXd0dGZKVDlSWmEwVXhGSHo5a2cxQVlELWdtSjI3R2hkTHdUMW42TWtYS0pzQUNsbS1TTENNdEdqV3UydzlJb1kw?oc=5)
- [To będzie wyjątkowy dzień dla psów i ich właścicieli. Krzeszowice szykują akcję - Przelom.pl - portal ziemi chrzanowskiej](https://news.google.com/atom/articles/CBMiwgFBVV95cUxPMUFWanRpTDdQeU8tWE9KeXhEWDg5ZlN2eGtnczAzb1RkQXphbXJuLTZianZubnZoYS1DdUxfTW1VNkp0NjA2VVNQSGFWTTRVbTAwTlVrZFg4bkFnWWY3TmNvdlAwSHFEUk1qakcyODJCcF92RDNDejdxTk4wOTdFZkw3M0FGRTFUd3lweGxySjhfckFodUhzTmR5X3B0Y0kxQzRuZEJ5QTJHT2c1RWhhRl9sZGZuMDlOQ1NTYnhQYzdDUQ?oc=5)
- [Uwaga! Zamknięta droga w Libiążu - Przelom.pl - portal ziemi chrzanowskiej](https://news.google.com/atom/articles/CBMihAFBVV95cUxOMnRqekw1a1owUDVVZjVVSXpMRXhVSUtwUnNMUzB0SG5acDVzZDVINW1kWlIydTBHTDRyWTFBazd5R295VXVKQnlsZWljVTJ1Rkt6TUxGenpoV3F3TnQxMFM2eWt1X0h2bE51czBwSVdldER6S3kwNHdrSTllbWt2RnJoTDc?oc=5)
- [To oni będą ratować mieszkańców! Czterech nowych strażaków w Chrzanowie - Przelom.pl - portal ziemi chrzanowskiej](https://news.google.com/atom/articles/CBMiuAFBVV95cUxOcHRLczZTTTNJeEtjSDhuUDJFRWNqM0k2Ym9uSFhMcVBxSGFEX00waHM0NF9nbmczcDRFbFpXOGdzcjg5eWpHUjVnYlJBV2RVdnZ1ZEoxODZfN3dJbkJBQnU2NXZuejI0ZVRxQkwweGVRZzR1VmhpMkItV3E0a3VSQzRwWTAzY29MbkVuM3EyX29zdktXV2JMaVctNGVGMUpTb3Y5ZV9SM3lLSmFycU1Za1B3akZJYnd3?oc=5)
<!--README_FEED:END-->