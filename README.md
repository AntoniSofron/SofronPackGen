# Sofron Terra Lab — clean-room worldgen project

**Minecraft Java 26.3 · Fabric · Java 25**

This repository branch starts from a **new Git tree, not from existing SofronPackGen source code**. It includes an original visual decision sheet and buildable mod scaffold. No old SofronPackGen terrain, karst, biomes, structures, or assets are imported.

## Otwórz na iPhonie bez pobierania plików

W aplikacji ChatGPT podgląd plików `.html`, `.md` i `.xlsx` może nie działać. Korzystaj z linków internetowych:

- [Konfigurator interaktywny — przez podgląd HTML](https://rawcdn.githack.com/AntoniSofron/SofronPackGen/d0b5642495e9d7ca47594648b88bc6b8ad7943c7/design/Sofron_Terra_Lab_Konfigurator.html) (publiczny serwis zewnętrzny; może wymagać potwierdzenia strony).
- [Arkusz 84 pytań — wersja mobilna](https://rawcdn.githack.com/AntoniSofron/SofronPackGen/d0b5642495e9d7ca47594648b88bc6b8ad7943c7/design/Arkusz_84_Pytania.html).
- [Analiza 11 modów — czytaj jako GitHub Markdown](docs/ANALIZA_MODOW.md).
- [Arkusz CSV dla Excela](design/PYTANIA_I_USTAWIENIA.csv).

Dodałem workflow `.github/workflows/publish-configurator.yml` do publikacji na **GitHub Pages**. Właściciel repozytorium może jednorazowo włączyć **Settings → Pages → Source: GitHub Actions**. Gdy publikacja zakończy się pomyślnie, oficjalny adres strony będzie `https://antonisofron.github.io/SofronPackGen/`. Nie udostępniaj tego adresu jako działającego, dopóki nie potwierdzisz wyniku deployu.

Konfigurator zapamiętuje ustawienia lokalnie w przeglądarce i umożliwia skopiowanie całego JSON (przycisk **Kopiuj JSON**) nawet bez pobrania pliku.

## Start with the owner’s design

1. Open [the offline interactive configurator](design/Sofron_Terra_Lab_Konfigurator.html).
2. Choose among **84 settings in 11 areas**, with live **illustrative** previews of continents, mountains, karst and rivers and source-mod comparison diagrams.
3. Export `sofron-worldgen-spec.json` and approve it. Only then implement the actual terrain generator.
4. Research audit: [11 packs studied](docs/ANALIZA_MODOW.md); implementation contract: [NEXT_IMPLEMENTATION.md](docs/NEXT_IMPLEMENTATION.md).

**Current JAR is a design-stage scaffold. It does not yet change world generation!** The vanilla biome and structure requirements are intentionally preserved. The HTML previews are *concept illustrations*, not actual rendered Minecraft chunks.

## One-click Windows build

- `BUILD.bat` — downloads verified Gradle 9.5.0 if required, compiles the scaffold and produces JAR.
- `BUILD_AND_INSTALL.bat` — also copies the JAR to `%APPDATA%\.minecraft\mods` (or a mods folder passed as first argument).
- Requirements: Java JDK 25 on PATH, internet for first Gradle + Minecraft dependencies.
- Minecraft runtime requires Fabric Loader 0.20.1+ and Fabric API 0.166.2+26.3.

## Licensing

Third-party code/assets were **not** copied. Biomes O' Plenty is ARR, Stardust Labs content has redistribution restrictions, and Cascades' current license is CC BY-NC-ND. Reference patterns are described at a high level in our original research notes.

## CI

Github Actions validates the interactive specification catalog and compiles the fresh mod; a full integrated worldgen test will be required once actual generation is implemented.
