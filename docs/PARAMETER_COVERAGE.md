# Sofron Terra Lab — stan wszystkich 84 zatwierdzonych ustawień

**Ważne:** status **Steruje generatorem** oznacza, że `gradle/approved-worldgen.gradle` przelicza część aktywnych funkcji terenu podczas kompilacji zgodnie z wartością z `config/approved-worldgen-spec.json`. Nie jest gwarancją konkretnego wyglądu z obrazka. `Do implementacji lub kalibracji` oznacza, że parametr został zachowany, ale **nie wolno jeszcze reklamować odpowiadającego mu systemu jako zaimplementowanego**.

| Dział | Parametr | Zatwierdzona wartość | Status |
|---|---|---|---|
| Wizja świata | `betaInfluence` | 80 | **Steruje generatorem** |
| Wizja świata | `geologicalRealism` | 50 | Do implementacji lub kalibracji |
| Wizja świata | `landmarkFrequency` | 50 | Do implementacji lub kalibracji |
| Wizja świata | `worldMood` | balanced | Do implementacji lub kalibracji |
| Wizja świata | `terrainRules` | hybrid | Do implementacji lub kalibracji |
| Kontynenty i oceany | `landCoverage` | 60 | Do implementacji lub kalibracji |
| Kontynenty i oceany | `continentScale` | 20 | Do implementacji lub kalibracji |
| Kontynenty i oceany | `coastComplexity` | 60 | **Steruje generatorem** |
| Kontynenty i oceany | `oceanDepth` | 100 | **Steruje generatorem** |
| Kontynenty i oceany | `oceanTrenches` | 50 | **Steruje generatorem** |
| Kontynenty i oceany | `continentalShelf` | 50 | Do implementacji lub kalibracji |
| Kontynenty i oceany | `continentalSystem` | hybrid | Do implementacji lub kalibracji |
| Góry i grzbiety | `mountainFrequency` | 50 | **Steruje generatorem** |
| Góry i grzbiety | `rangeLength` | 40 | **Steruje generatorem** |
| Góry i grzbiety | `rangeWidth` | 50 | **Steruje generatorem** |
| Góry i grzbiety | `peakHeight` | 256 | **Steruje generatorem** |
| Góry i grzbiety | `mountainSteepness` | 70 | **Steruje generatorem** |
| Góry i grzbiety | `erosionStrength` | 50 | Do implementacji lub kalibracji |
| Góry i grzbiety | `mountainIsolation` | 35 | Do implementacji lub kalibracji |
| Góry i grzbiety | `alpineSnow` | 50 | Do implementacji lub kalibracji |
| Góry i grzbiety | `mountainShape` | mixed | Do implementacji lub kalibracji |
| Klify, zwisy i doliny Classic | `betaCliffFrequency` | 80 | **Steruje generatorem** |
| Klify, zwisy i doliny Classic | `betaCliffHeight` | 75 | **Steruje generatorem** |
| Klify, zwisy i doliny Classic | `overhangFrequency` | 50 | **Steruje generatorem** |
| Klify, zwisy i doliny Classic | `floatingLandmass` | 15 | Do implementacji lub kalibracji |
| Klify, zwisy i doliny Classic | `classicValleyAmount` | 45 | **Steruje generatorem** |
| Klify, zwisy i doliny Classic | `valleyDepth` | 75 | **Steruje generatorem** |
| Klify, zwisy i doliny Classic | `valleyWidth` | 50 | Do implementacji lub kalibracji |
| Klify, zwisy i doliny Classic | `plateauFrequency` | 15 | Do implementacji lub kalibracji |
| Klify, zwisy i doliny Classic | `cliffPlacement` | mixed | Do implementacji lub kalibracji |
| Kras i iglice skalne | `karstFrequency` | 20 | **Steruje generatorem** |
| Kras i iglice skalne | `karstCluster` | 75 | **Steruje generatorem** |
| Kras i iglice skalne | `karstHeight` | 75 | **Steruje generatorem** |
| Kras i iglice skalne | `karstWidth` | 50 | **Steruje generatorem** |
| Kras i iglice skalne | `karstSharpness` | 50 | Do implementacji lub kalibracji |
| Kras i iglice skalne | `karstSeparation` | 33 | Do implementacji lub kalibracji |
| Kras i iglice skalne | `karstVegetation` | 75 | Do implementacji lub kalibracji |
| Kras i iglice skalne | `karstShape` | mixed | Do implementacji lub kalibracji |
| Kras i iglice skalne | `karstLocation` | mixed | Do implementacji lub kalibracji |
| Wyspy i archipelagi | `islandFrequency` | 50 | **Steruje generatorem** |
| Wyspy i archipelagi | `archipelagoDensity` | 75 | Do implementacji lub kalibracji |
| Wyspy i archipelagi | `islandVariety` | 75 | Do implementacji lub kalibracji |
| Wyspy i archipelagi | `alphaIslands` | 75 | **Steruje generatorem** |
| Wyspy i archipelagi | `skyIslandFrequency` | 5 | Do implementacji lub kalibracji |
| Wyspy i archipelagi | `islandMountain` | 70 | Do implementacji lub kalibracji |
| Wyspy i archipelagi | `islandShape` | mixed | Do implementacji lub kalibracji |
| Rzeki i dopływy | `riverFrequency` | 45 | Do implementacji lub kalibracji |
| Rzeki i dopływy | `riverWidth` | 50 | Do implementacji lub kalibracji |
| Rzeki i dopływy | `riverMeander` | 100 | Do implementacji lub kalibracji |
| Rzeki i dopływy | `riverSourceAltitude` | 50 | Do implementacji lub kalibracji |
| Rzeki i dopływy | `tributaryFrequency` | 50 | Do implementacji lub kalibracji |
| Rzeki i dopływy | `waterfallRivers` | 50 | Do implementacji lub kalibracji |
| Rzeki i dopływy | `riverBeaches` | 75 | Do implementacji lub kalibracji |
| Rzeki i dopływy | `subterraneanRivers` | 33 | Do implementacji lub kalibracji |
| Rzeki i dopływy | `navigableRivers` | 75 | Do implementacji lub kalibracji |
| Rzeki i dopływy | `riverAlgorithm` | watershed | Do implementacji lub kalibracji |
| Jeziora i wodospady | `lakeFrequency` | 25 | Do implementacji lub kalibracji |
| Jeziora i wodospady | `lakeScale` | 33 | Do implementacji lub kalibracji |
| Jeziora i wodospady | `mountainLakes` | 45 | Do implementacji lub kalibracji |
| Jeziora i wodospady | `waterfallFrequency` | 75 | Do implementacji lub kalibracji |
| Jeziora i wodospady | `wetlands` | 33 | Do implementacji lub kalibracji |
| Jeziora i wodospady | `lakeOutlets` | mixed | Do implementacji lub kalibracji |
| Jaskinie i podziemia | `caveFrequency` | 45 | Do implementacji lub kalibracji |
| Jaskinie i podziemia | `caveChambers` | 35 | Do implementacji lub kalibracji |
| Jaskinie i podziemia | `caveVertical` | 50 | Do implementacji lub kalibracji |
| Jaskinie i podziemia | `caveOpenings` | 25 | Do implementacji lub kalibracji |
| Jaskinie i podziemia | `caveWater` | 15 | Do implementacji lub kalibracji |
| Jaskinie i podziemia | `caveBiomeSafety` | 100 | Do implementacji lub kalibracji |
| Jaskinie i podziemia | `caveSystem` | hybrid | Do implementacji lub kalibracji |
| Powierzchnia i klimat | `stoneExposure` | 33 | Do implementacji lub kalibracji |
| Powierzchnia i klimat | `slopeSoils` | 80 | Do implementacji lub kalibracji |
| Powierzchnia i klimat | `beachWidth` | 75 | Do implementacji lub kalibracji |
| Powierzchnia i klimat | `climateGradient` | 66 | Do implementacji lub kalibracji |
| Powierzchnia i klimat | `microrelief` | 75 | **Steruje generatorem** |
| Powierzchnia i klimat | `biomeSize` | 50 | Do implementacji lub kalibracji |
| Powierzchnia i klimat | `vanillaBiomesOnly` | true | **Obowiązuje** |
| Wydajność i zgodność | `vanillaStructuresOnly` | true | **Obowiązuje** |
| Wydajność i zgodność | `seaLevel` | 64 | **Obowiązuje** |
| Wydajność i zgodność | `worldHeight` | 320 | **Obowiązuje** |
| Wydajność i zgodność | `performanceProfile` | quality | Do implementacji lub kalibracji |
| Wydajność i zgodność | `compatibility` | fabric26.3 | **Obowiązuje** |
| Wydajność i zgodność | `villagesTerrainAware` | true | Do implementacji lub kalibracji |
| Wydajność i zgodność | `multiSeedTests` | true | Do implementacji lub kalibracji |
| Wydajność i zgodność | `lodCompatible` | true | Do implementacji lub kalibracji |

## Podsumowanie

- **21** parametrów aktywnie steruje liczbami w generacji przy kompilacji.
- **5** parametrów opisuje twarde reguły techniczne projektu.
- **58** wymaga odrębnych algorytmów, testów lub kalibracji.
- Zachowane tylko biomy i struktury Vanilla, jak zatwierdzono.
- Automatyczny build uruchamia `syncApprovedTerrain`, więc zmiana zaimplementowanego parametru w JSON jest uwzględniana w następnym JAR-ze.
- Pełna hydrologia typu `watershed` nie może być udawana przez przypadkowy noise carving — wymaga obliczeń zlewisk i oddzielnego systemu wody z walidacją spadku wysokości.

## Kolejność dalszej implementacji

1. Mapowanie kontynentów i weryfikacja celu 60% lądu na wielu seedach.
2. Niezależny graf hydrologiczny zlewni i połączeń rzek, meandry, brzegi żwirowo-piaszczyste.
3. Wypełnienie rzek/jezior rzeczywistymi blokami wody, również powyżej Y64; stabilny przepływ i ujścia.
4. Podziemne odcinki rzek, wodospady oraz jaskinie w górach, bez podziemnych biomów na otwartym niebie.
5. Rzeczywiste filary krasowe i Alpha Islands, pomiary gór, stromości oraz rozmieszczenia obiektów Vanilla.
