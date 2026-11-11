# Sofron Pack Gen — konfiguracja 26.3 (projekt źródłowy)

Wariant przygotowany na bazie dostarczonego projektu `26.3-Sofron-Pack-Gen-v0.1` z archiwum `other mods&packs.zip`.

## Wymagania
- Minecraft Java Edition 26.3
- Java Development Kit **25** lub nowszy
- Fabric Loader >= 0.19.5
- Gradle Wrapper dołączony do projektu
- Dostęp do repozytoriów Maven podczas pierwszej kompilacji

## Kompilacja
Linux/macOS: `./gradlew clean build`
Windows: `gradlew.bat clean build`

Plik JAR po udanej kompilacji powinien pojawić się w `build/libs/`.
Umieść go w folderze `mods` instalacji Fabric 26.3. **Utwórz nowy świat**; modyfikacja istniejących chunków nie jest wykonywana.

## Zrealizowane zmiany
- Bazę Cascades zachowano z dotychczasowych splajnów `offset` i `factor` (nie kopiowano starego formatu 1.20).
- `continentalness.xz_scale`: 0.18 -> 0.15, aby zwiększyć przestrzenną skalę układu kontynentów o ~20% (w sensie długości fali, nie powierzchni).
- `sea_level`: 63 -> 64.
- Źródło biomów w wymiarze: `minecraft:multi_noise` z presetem `minecraft:overworld`. Pozostałe pliki własnych biomów są zachowane jako nieaktywne zasoby.
- Selektor rzek: ridge [-0.1,0.1) -> [-0.16,0.16); profil funkcji `river_valley` poszerzono na osi ridge 1.6x; odpowiednie gradienty wysokości przesunięto +17.
- `jaggedness`: skalowanie 0.7 (nowy mnożnik całej funkcji).
- Własne `karst_towers` i `karst_sharp_spires` zastąpiono nową funkcją `hybrid_karst` inspirowaną Terralith, Wythers i Biomes O’ Plenty.
- Zachowano połączenia `sloped_cheese` z `hybrid_karst`, `river_valley`, `underground_caverns`, `underground_channels`.
- Zachowano konfigurację `aquifers` i `exclusion` z pierwotnego projektu.
- Naprawiono 26 niesortowanych lub zduplikowanych list punktów splajnów (22 w offset, 4 w sofron_factor): punkty sortowano rosnąco, a dla powtarzającej się lokalizacji zachowano ostatni. **Zmienia to semantykę źródłowych splajnów i wymaga testów w grze.**

## Ważne ograniczenia i brak potwierdzenia
1. **To nie jest potwierdzony działający JAR.** W środowisku tworzenia projektu jest Java 21, a Minecraft 26.3 wymaga Java 25. Nie wykonano kompilacji i nie uruchomiono gry.
2. Walidacja syntaktyczna JSON jest wykonana; nie jest równoważna walidacji kodeków Minecraft.
3. Nie zweryfikowano pełnej zgodności istniejących struktur, features, materiałów, custom biome i material_rule z runtime 26.3.
4. Nie wprowadzono proceduralnego modelu meandrów ani osobnego hydrologicznego solvera. `riverWidth=0.16` mapuje się na próg ridge, a nie na szerokość rzeki w blokach.
5. Wysokość gór 200, rozkład Alpha Islands, częstotliwość jezior 0.75 i gęstość krasu 0.7 nie są niezależnymi parametrami Minecrafta; **nie zostały uznane za w pełni zrealizowane**. Implementacja wymaga osobnego modelu i testów generacji.
6. Aquifery nie gwarantują ciągłości rzek ani połączeń powierzchniowych. Poziom morza Y=64 może wpływać na biome i generację struktur; wymagane testy.
7. Projekt zawiera oryginalne zasoby i struktury z dostarczonego archiwum. Nie dołączono kodu źródłowego innych modów.

## Testy zalecane po kompilacji
- Sprawdzić log `latest.log` przy ładowaniu rejestrów; w szczególności spline, density function, feature i biome.
- Wygenerować nowy świat dla kilku seedów i zbadać przekroje Y=80, Y=200, kras i jaskinie.
- Zweryfikować, czy `minecraft:overworld/final_density` używa zmodyfikowanego `minecraft:overworld/sloped_cheese` w runtime 26.3.
- Sprawdzić wodę w korytach, aquifery, odcięte tunele i zalewanie jaskiń.
- Porównać czasy generacji i liczbę poprawnie generowanych struktur.
- Zweryfikować zgodność custom structures/biomes z Fabric i wersją 26.3.

Plik `design/approved-settings.json` zachowuje pełną zatwierdzoną konfigurację; `design/implementation-status.json` wskazuje zakres faktycznie wdrożony.

## Zmiana konfiguracji: 11.11.2026
- Poziom morza: Y=64.
- Usunięto własne definicje biomów `sofron_pack` i własne struktury wraz z szablonami.
- Pozostają biomy Vanilla, funkcje terenu i krasu oraz struktury Minecraft.

## Hybrydowy kras (11.11.2026)
- Nowa funkcja `sofron_pack:overworld/hybrid_karst` jest połączona z `minecraft:overworld/sloped_cheese` przez `max`.
- Trzy regionalne profile: wysokie, smukłe filary (Terralith), nieregularne skały Huangshan (Wythers), zwężające się ostańce (Biomes O' Plenty). To oryginalna implementacja inspirowana charakterem tych modów, nie skopiowany kod.
- Regiony ograniczają `continents`, `erosion`, `ridges`, `temperature`, `vegetation` i wysokość Y=65..191; profil bazuje na wbudowanych szumach `minecraft:pillar`, `pillar_thickness`, `pillar_rareness` i `surface`.
- Usunięto stare funkcje `karst_towers`, `karst_sharp_spires` i trzy niezgodne formatem szumy `karst_*`.
- Zmienna liczba kolumn oraz jakość sylwetek wymagają testów w grze; profile są zależne od amplitud rzeczywistych szumów. Nie implementowano features Java BOP ani struktur NBT Wythers.
