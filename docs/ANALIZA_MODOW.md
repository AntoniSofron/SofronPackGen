# SOFRON TERRA LAB — audyt 11 źródeł i projekt nowego generatora

**Wersja analizy:** 12 listopada 2026 · **Platforma docelowa:** Minecraft Java 26.3 / Fabric / Java 25 · **Zakres:** wyłącznie biomy i struktury Vanilla.

Ten raport bazuje przede wszystkim na faktycznej zawartości przesłanego archiwum `other mods&packs(1).zip`. Publiczne opisy i wydanie Minecraft 26.3 są odrębnymi źródłami do weryfikacji API i licencji. Nie twierdzę, że samo przeczytanie kodu daje pełną wiedzę o losowych wynikach na wszystkich seedach — wymaga to uruchomienia i testów.

## Inwentarz archiwum

| Projekt / wariant | Rola | Ograniczenie wykorzystania |
|---|---|---|
| Cascades / Hybrid Beta | klif Beta, zwisy, krajobrazy, rzeki żeglowne, skalne plaże | CC BY-NC-ND 4.0 według aktualnej karty Modrinth; w ZIP brak LICENCE.md — nie kopiować. |
| Tectonic | pasma, kontynenty, oceany, jaskinie i rzeki pod ziemią | Brak licencji w archiwalnym katalogu; prawa niepotwierdzone — tylko analiza. |
| Terralith 2.6.5 (26.3) | Alpha Islands, organiczne krainy, spires, jeziora, klify | Własna licencja Stardust Labs zabraniająca redystrybucji zmodyfikowanych wersji. |
| Terralith 1.20 | porównanie ewolucji terenu i starych Alpha Islands | Własna licencja Stardust Labs. |
| Biomes O’ Plenty 26.3 | smukłe ostańce skalne i erodowane kolumny (Jade Cliffs) | All rights reserved — zakaz kopiowania implementacji. |
| Continents | rozdzielanie oceanów, kontynentów i wysp | Własna licencja Stardust Labs. |
| William Wythers Overhauled Overworld | meandry, naturalne brzegi, dopasowanie materiałów, bogata hydrologia | Brak dedykowanej licencji w archiwum — nie kopiować. |
| Navigable Rivers 1.5.0 | rzeki przepływowe i wypełnione wodą | Brak licencji w ZIP. |
| Moderner Beta | charakter starego Classic/Infdev/Alpha/Beta | MIT (zachować notice przy kopiowaniu, ale preferujemy własny kod). |
| TerraBlender 26.3 | integracja biome/region registry | LGPLv3 |
| SofronPackGen v0.1 (wyłączony) | nie używać jako źródła / przeciwprzykład krasu | Kod użytkownika — pominięty zgodnie z wyraźnym poleceniem. |

## Szczegółowe mechanizmy

### Cascades / Hybrid Beta

**Źródło z ZIP:** `Hybrid-Beta-master/Cascades/`. `README.md` deklaruje dokładnie to, czego szukamy: klify inspirowane Beta 1.7.3, zwisy i wyspy zawieszone, szerokie jeziora, piaszczyste i żwirowe plaże oraz podziemne rzeki dostępne dla łodzi. Konkretny mechanizm obejmuje nadpisanie `data/minecraft/worldgen/noise_settings/overworld.json`, modyfikacje `overworld/offset.json`, `factor.json`, `rivers_extra_density.json`, `river_carver/noise_selector.json`, `river_carver/river_carver.json` i `overworld/fluid_level_floodedness.json`. Nie można z tego wywieść gwarancji hydrologicznej dla każdej rzeki; dowodzi to rozdzielenia funkcji terenu od funkcji wodnych. **Uwaga na wersję:** w ZIP użyto starego formatu `aquifers_enabled` i `surface_rule`, a 26.3 wymaga `aquifers` i `material_rule`. Mimo że aktualne publikacje projektu obejmują 26.3, konkretne pliki z załączonego ZIP nie są automatycznie przenośne. **Wniosek projektowy:** rozdzielić maskę widowiskowych „klasycznych” form od stałej, bezpiecznej linii brzegowej i od sieci rzek.

### Tectonic

**Źródło z ZIP:** `tectonic-rewrite-squared/`. `ConfigState.java` rozdziela `GlobalTerrain`, `Continents`, `Islands`, `Oceans`, `Biomes`, `Caves` i `Experimental`. Wartości konfiguracyjne obejmują m.in. `vertical_scale`, `elevation_boost`, `ocean_depth`, `deep_ocean_depth`, `underground_rivers`, `erosionScale`, `continentsScale`, `ridgeScale`, a także parametry redukcji jaskiń. Istnieją osobne `continent_selector.json`, `terrain_spline/factor/*`, `terrain_spline/offset/*` oraz cały podsystem `underground_river/noise/*` i `underground_river/total.json`. **To jest klucz:** spójne makrostruktury przed lokalnym detalem. W ZIP ustawienia noise_settings mają miejscami stary format — trzeba użyć wyłącznie idei architektonicznej i napisać oryginalną implementację 26.3. **Wniosek:** długie pasma i głębokie oceany muszą powstawać z powiązanych, ale odrębnych pól.

### Terralith 2.6.5 + 26.3

**Źródło z ZIP:** `Terralith_v2.6.5+26.3/`. Nowoczesny `worldgen/noise_settings/overworld.json` korzysta z `aquifers`, `material_rule`, `chunk_surface_level` i osobnych funkcji klimatu. Występują dokładnie biomy `terralith:alpha_islands` i `terralith:alpha_islands_winter` oraz ich tag, a także mechanizmy `overworld/extra_terrain_sum`, `subtract_terrain_sum`, `overworld/reference/pillars_large_base` i struktury typu spire. **Ważne rozróżnienie:** wpis `alpha_islands.json` sam nie definiuje w całości geometrii wyspy — krajobraz wynika z doboru biomów, maskowania funkcji gęstości i cech. Nowsza paczka ma własne materiały i biomy; **nasz mod ma wyłącznie Vanilla**. Przenosimy kształtowanie powierzchni/wysp jako oryginalne funkcje, nie nowe biomy ani tekstury. **Licencja:** własna, ograniczająca rozpowszechnianie przeróbek.

### Terralith 1.20

**Źródło z ZIP:** `Terralith-1.20/`. Zawiera `alpha_islands.json` w warstwach bazowych i `1-20-3-overlay`, stare `noise_router/continents.json`, `offset.json` oraz zaawansowane formacje klifowe. Analiza jest cenna do rozpoznania stabilnych efektów wizualnych i ich ewolucji, ale JSON oparty na `firstOctave`, `surface_rule` i `aquifers_enabled` nie zadziała bez migracji do 26.3. **Wniosek:** oddzielić obserwowaną formę od historycznego sposobu jej zapisu.

### Biomes O’ Plenty 26.3

**Źródło z ZIP:** `BiomesOPlenty-26.3/common/src/main/java/biomesoplenty/worldgen/feature/misc/ErodedPillarFeature.java` — rzeczywisty punkt odniesienia dla kolumn, w odróżnieniu od nielubianego starego SofronPackGen. Funkcja ustala promień bazy, losuje wysokość (standardowo 10–19, okazjonalnie dodatkowe 15–29), zwęża profil ku górze, dodaje erozję brzegów i wygładza fragmenty. Paczka rejestruje `worldgen/feature/eroded_pillar.json` oraz `placed_feature/jade_cliffs_eroded_pillars.json`; placement ma `rarity_filter` `chance=3`, mapę wysokości `OCEAN_FLOOR_WG` i warunek biomu. **Nie wolno kopiować tego algorytmu:** licencja `All rights reserved`. Zamiast tego opracujemy **własny** algorytm ostańców ze zbieżnym ogólnym efektem (szerokość podstawy, smukłość, ostrzenie, skupiska, materiał vanilla), oparty o funkcję odległości i deterministyczne pole skalne. Aby nie dodawać biomów, umieszczanie warunkujemy oryginalną maską geologiczną, nie `jade_cliffs`.

### Continents 1.20

**Źródło z ZIP:** `Continents-1.20/`, około 50 JSON-ów; widać `adjusted_continents`, `continent_bias`, `islands`, `island_amount`, `island_impact`, `centroid/ring1...ring4` oraz wariant `large`. Wskazuje to na rozdzieloną strukturę kontynentalności, wysp i skali. Nie jest to zwykły suwak „ilość lądu” — obszary lądowe muszą zachować spójność przestrzenną i naturalne linie brzegowe. Własne rozwiązanie: wieloskalowa kontynentalność + maski archipelagów + kontrolowana kalibracja pokrycia lądem. Licencja Stardust Labs nie dopuszcza redystrybucji zmodyfikowanej paczki.

### William Wythers’ Overhauled Overworld 2.6.0

**Źródło z ZIP:** `William Wythers' Overhauled Overworld v2.6.0/`; pliki `placed_feature/terrain/carver/river.json`, `river_water.json`, a także `terrain/extended/river_gravel`, `river_soil`, lokalne wzory plaż, kamienia i gleby. **Istotny wniosek:** rzeźba koryta, umieszczenie płynu i materiały brzegów to różne etapy. Pakiet ma wiele overlayów dla starych wersji Minecrafta; w plikach JSON zdarzają się odwołania do typów `wythers`, które wymagają właściwej implementacji. Nie wolno zakładać, że skopiowanie samych JSON-ów utworzy sprawną rzekę w 26.3. Autorska implementacja musi spinać hydrologię z wysokością i mapą biomów Vanilla.

### Navigable Rivers 1.5.0

**Źródło z ZIP:** tylko osiem rzeczywistych plików, głównie wersjonowane `placed_feature/terrain/carver/river.json` i `river_water.json`. Nie zawiera samodzielnego pełnego programu obliczania dorzeczy; można zidentyfikować ideę dwuetapowego koryta/wody i powiązanie z przestrzenią `wythers`. **Zatem nie przypisuję tej paczce rozbudowanego solvera, którego w ZIP nie ma.** W projekcie „navigable” będzie faktycznie mierzone minimalną szerokością i głębokością przepływnego odcinka, a nie losowo rysowaną niebieską linią.

### Moderner Beta

**Źródło z ZIP:** `moderner-beta-multiversion/`, ponad 500 klas Java. Są klasy implementacji starych generatorów oraz obrazki presetów `classic_0_0_14a_08.png`, `classic_0_30.png`. System `InitRiverLayer`, `ComputeRiverLayer`, `MixRiverLayer` reprezentuje klasyczne fraktalne nakładanie warstw rzek; osobne biblioteki akwenów/biomów odtwarzają różne epoki Minecrafta. **Inspiracja:** strome ściany, stare tarasowanie i organiczny rozkład dolin. **Licencja MIT** — dopuszcza wykorzystanie kodu z zachowaniem informacji o licencji, lecz dla tego projektu i tak wybieramy odrębną implementację. Załączone README nie potwierdza obsługi 26.3 (do 26.2).

### TerraBlender 26.3

**Źródło z ZIP:** `TerraBlender-26.3`, licencja LGPLv3. To biblioteka do wstrzykiwania i łączenia regionów/biomów w systemie Minecrafta, z przykładami instalacji API. Nie tworzy samodzielnie pasm, krasu i rzek. Ponieważ użytkownik chce wyłącznie biomy Vanilla, **nie ustanawiam TerraBlender obowiązkową zależnością**. Może posłużyć do analizy zgodności z przyszłymi paczkami, lecz nie będzie podstawą modułu terenu.

### Archiwalny SofronPackGen — tylko przeciwprzykład

**Źródło z ZIP:** `26.3-Sofron-Pack-Gen-v0.1`. Pliki zawierają stary `karst_towers`, `karst_sharp_spires`, własne biomy i struktury. Użytkownik wyraźnie zabronił używania tego kodu i tego krasu. **Żadna funkcja z tego katalogu nie zostanie przeniesiona.** Dopuszczalne są wyłącznie ogólne doświadczenia o błędach: skałki na całych równinach, suche oceany, niezgodne biomy i nieskoordynowany final_density.

## Uwaga: „Classic Valley”

W przeszukanym archiwum nie znaleziono definicji ani nazwy `classic_valley` / `Classic Valley` jako osobnego generatora. W Terralith obecne są biomy `blooming_valley`, `lush_valley`, `moonlight_valley`, `valley_clearing` itd., ale nie dokładnie wskazana nazwa. W Moderner Beta istnieją presety „Classic”, a w Tectonic splajny pojedynczej i podwójnej doliny. Dlatego pytanie **„Dolinny krajobraz Classic”** w arkuszu jest świadomie projektem nowego stylu, a nie przypisanym błędnie istniejącym algorytmem. Jeśli użytkownik miał na myśli konkretną inną paczkę, będzie można skorygować referencję bez zmiany ustaleń o generatorze.

## Rozdzielenie generatora na warstwy

1. **Makrogeografia:** niezależna kontynentalność 2D, udział lądu, strefa szelfów, kanały oceaniczne oraz rozmieszczenie archipelagów. Jedno źródło danych dla **kształtu** oceanów i **klasyfikacji** biome/aquifer.
2. **Tektonika:** pole długich pasm (ukierunkowane grzbiety, zmienne szerokości, grupy grzbietów), szczyty i realistyczne gradienty wysokości. Ograniczenie maksymalnego Y i spójność erozji.
3. **Krajobraz Beta:** zagnieżdżone domeny 3D dla zwisów i klifów, aktywne tylko na dopasowanym gruncie; ograniczniki zapobiegające pustkom typu „gigantyczna komora z otwartym niebem”.
4. **Geologia krasowa:** niezależne maski prowincji krasowych, ostańce generowane z funkcji odległości + regionalna erozja + deterministyczny rozkład grup. Dla każdego słupa istnieje stabilna podstawa i zachowany dostęp do świata. Woda/roślinność tylko Vanilla.
5. **Hydrologia:** graf spływu po heightmapie z „pit filling”, akumulacją dopływu, źródłami wysoko w górach, wygładzeniem profilu rzeki ku ujściu, opcjonalnymi dopływami/jeziorami, estuariami i meandrami zależnymi od spadku. Geometria koryta ≠ klasyfikacja wody ≠ materiał brzegowy.
6. **Jaskinie:** zachować działające aquifery i podziemną selekcję biomów Minecraft 26.3; tunele podziemnych rzek związać z siecią powierzchniową, a nie losową funkcją gęstości.
7. **Biomy Vanilla:** żadnych custom biomes. Temperatura, wilgotność, kontynentalność, erozja i głębokość muszą być spójne z regionami. Sculk/Deep Dark nie może wyciekać na normalną powierzchnię.
8. **Struktury Vanilla:** zachować oryginalne zestawy, przy lokalnych korektach gruntu unikać przecinania wiosek, świątyń i ruin.
9. **Wydajność:** deterministyczne cache regionów, zadania per chunk bez zależności od kolejności generowania, profili na wielu seedach.

## Minecraft 26.3 — wymagania schematu JSON

W aktualnych wydaniach są m.in. `material_rule` zamiast `surface_rule`, `chunk_surface_level` zamiast `preliminary_surface_level`, obiekt `aquifers` zamiast `aquifers_enabled`, zaktualizowany format szumów (`base_octave`, `octave_count`, `amplitude_modifiers`), zmienione kodeki funkcji gradientu i jednokrotna precyzja obliczeń. Nie można kopiować JSON-ów 1.20 do 26.3 bez migracji i prawdziwej walidacji rejestrów.

## Testy akceptacyjne — warunek „działa”

| Klasa | Twardy test |
|---|---|
| Oceany | Rzeczywista woda w biomie oceanicznym w wielu punktach Y poniżej poziomu morza; bez dziur chunków; wejście do oceanu prowadzi do pełnej warstwy wody. |
| Rzeki | Ciągły bieg od wysoko położonego źródła ku niższemu ujściu; spadek monotoniczny wzdłuż nurtu z dopuszczeniem progów wodospadów; rzeczywiste bloki wody i łodzie. |
| Jeziora | Poprawna powierzchnia cieczy, brzegi i odpływy; brak arbitralnych pustych mis pod otwartym niebem w biomie oceanu. |
| Biomy | Brak podziemnych dekoracji na normalnym terenie; Deep Dark i Lush Caves generowane we właściwych głębokościach. |
| Powierzchnia | Naturalna gleba poza stromymi skałami, materiał stoków i śnieg dobrane do klimatu Vanilla. |
| Góry | Kontrola długości, szerokości pasm, wysokości szczytów, realistycznego zróżnicowania; brak prostokątnych ścian. |
| Kras | Formacje smukłe, rozmieszczone w ograniczonych prowincjach, nie na całym świecie; nie przenosimy BOP kodu. |
| Jaskinie | Brak wielkich przypadkowych pustek otwierających podziemne biomy na zewnątrz; podziemne rzeki mają realny płyn. |
| Struktury | 100% bazowych struktur Vanilla funkcjonuje w wielu regionach. |
| Zgodność | Fabric 26.3, Java 25, instalator Windows klikany dwa razy, brak starych zasobów SofronPackGen. |
| Wydajność | Benchmark chunk generation na co najmniej 4 seedach, powtarzalność niezależna od kolejności chunków. |

## Ramy projektu i ograniczenia

Prezentowane wizualizacje w interaktywnym HTML są **autorskimi ilustracjami deterministycznymi**, nie podglądem docelowej pracy chunk generatora. Zmiana suwaka natychmiast modyfikuje schemat, ale dopiero właściwa implementacja po zatwierdzeniu wartości i testy świata dowiodą zgodności. Dane porównawcze dotyczą wzorców wizualnych i potwierdzonych rodzajów mechanizmów — nie są zmyślonymi ustawieniami liczb używanymi przez autorów innych modów.

## Wskazówki do wypełnienia

Otwórz `Sofron_Terra_Lab_Konfigurator.html` w przeglądarce (działa offline), ustaw 84 parametry w 11 działach, obserwuj mapę / przekrój / kras / dorzecze, a następnie kliknij **Zapisz wybory (JSON)**. Ten plik będzie specyfikacją do implementacji. Wersja `.xlsx` służy do uwag i pracy tabelarycznej. Biomy i struktury Vanilla są wymaganiem zablokowanym, bez możliwości zmiany na custom.

## Źródła publiczne (niezależne od ZIP)

- Minecraft Java 26.3 changelog: https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3
- Tectonic: https://github.com/Apollounknowndev/tectonic
- Cascades: https://github.com/Crystalis7/Hybrid-Beta
- Terralith: https://modrinth.com/datapack/terralith
- Biomes O’ Plenty: https://github.com/Glitchfiend/BiomesOPlenty
- Moderner Beta: https://github.com/BlueStaggo/Moderner-Beta
- TerraBlender: https://github.com/Glitchfiend/TerraBlender


---

## Aneks techniczny — ścieżki algorytmów z pliku ZIP

Poniższa część nie powtarza kodu żadnego autora. Opisuje **rzeczywiste zależności** odczytane z zasobów załączonej paczki, żeby specyfikacja nie sprowadzała się do nazw modów i luźnej inspiracji.

### A. Cascades: obszar rzeki to osobna maska 3D

`Hybrid-Beta-master/Cascades/data/hybrid_beta/worldgen/density_function/overworld/river_carver/noise_selector.json` pokazuje warstwową selekcję obszarów na podstawie wysokości Y, ridges, continentalness i erosion. Analizowany plik ogranicza część działania do przedziału wysokości **48–128**, a w masce grzbietów wykorzystuje niewielki zakres wokół zera. Inny plik, `rivers_extra_density.json`, wylicza maskę dodatkowej gęstości rzeki z pola Y i bezwzględnej wartości ridges. `fluid_level_floodedness.json` zmienia bazowe pole zalania aquifera. **Wniosek:** nawet widowiskowy generator klifów może integrować rzeki z rzeźbą i aquiferami; nie wystarcza rysować krzywej rzeki na gotowym terenie.

### B. Tectonic: kilka poziomów wyboru regionu

`tectonic:continent_selector` w `src/common/main/resources/resourcepacks/tectonic/data/tectonic/worldgen/density_function/continent_selector.json` stanowi uzupełnienie `island_selector` z cache 2D. `terrain_spline/offset/depth_additive.json` używa zagnieżdżonych splajnów zależnych od kontynentalności, erozji, regionu i klimatu. W obrębie `underground_river/total.json` łączone są m.in. filtry podziemnych słupów i osobne splajny wysokości, ridges, kontynentów i erozji, a przełącznik zależy od konfigurowalnej stałej. **Wniosek:** rzeka podziemna musi być umiejscowiona regionalnie i pionowo, a nie wynikać z jednolitego tunelu losowego szumu. Analizowane JSON-y są starego formatu i nie można ich przenosić bezpośrednio do 26.3.

### C. Terralith 26.3: wyspa to układ biomy–gęstość–cechy

W `data/terralith/worldgen/biome/alpha_islands.json` opisane są środowiskowe atrybuty biomu, m.in. kolory nieba i mgły oraz spawny. To nie jest kompletny algorytm topograficzny. Odmiennie `data/terralith/worldgen/placed_feature/skylands/floating_islands.json` zawiera **12 prób** funkcji `terralith:skylands/island` przy zadanym zakresie wysokości **230–232** (nie oznacza to 12 skutecznych wysp na każdy chunk — placement ma też warunek biomu). Co więcej `worldgen/density_function/overworld/link/aquifer_manipulator.json` korzysta ze splajnów pól gęstości, by oddzielnie sterować aquiferami. **Wniosek:** „Alpha Islands” jako estetyka, „floating islands” jako osobna cecha i pełne oceany to trzy niezależne decyzje do uwzględnienia w nowym generatorze. Żaden terralithowy biom ani asset nie zostanie skopiowany.

### D. Biomes O’ Plenty: kolumny to osobny feature po zakończeniu makroterenu

`ErodedPillarFeature.java` wyraźnie odróżnia promień podstawy, wysokość, promień zależny od wysokości, zakłócenia brzegu i wygładzanie. Implementacja tworzy kolumnę lokalnie, a nie deformuje całej mapy kontynentalności. `jade_cliffs_eroded_pillars.json` określa placement: `rarity_filter`, rozproszenie w chunku, wysokość przez `OCEAN_FLOOR_WG` i ograniczenie biome. **Wniosek dla nas:** własna prowincja geologiczna + kontrolowana liczba ostańców per region, po czym oryginalny postprocess nie może naruszać koryt rzek ani struktur Vanilla. Parametry 84-pytaniowego arkusza oddzielnie kontrolują częstość, wysokość, szerokość, smukłość, odległość, grouping i zieleń.

### E. Continents: transformacja continentalness bez zmieniania pozostałych biomów

`data/continents/worldgen/density_function/adjusted_continents.json` w 1.20 używa splajnu na bazowym polu `overworld/base_continents`, a na koniec łączy wynik z maską wyspy startowej. W pakiecie obecne są dodatkowe centroids i pierścienie oraz wariant „large”. **Wniosek:** nowa skala kontynentów nie może być tylko mnożnikiem wysokości gór. Mapowanie naturalnych mas lądowych powinno pozostać zgodne ze współrzędną klimatu dla oceanicznych biomów Minecrafta.

### F. Wythers i Navigable Rivers: geometryczne wycinanie ≠ woda

W obu przesłanych paczkach występuje `data/wythers/worldgen/placed_feature/terrain/carver/river.json`, który w starym schemacie używa cechy typu `minecraft:disk` ze stanem **air** oraz kontroli tagów bloków i otoczenia. Drugi plik `river_water.json` korzysta z cechy `minecraft:disk` ze stanem **water**, testując m.in. sąsiednie bloki płynów i warunki materiałowe. **To ważne dla poprzednich błędów SofronPackGen:** samo wycięcie suchej doliny nigdy nie spowoduje automatycznie powstania rzeki. Jednak kopiowanie disk/carver z 1.20/1.21 do 26.3 bez migracji, licencji i rozwiązywania kolejności chunków byłoby błędem.

### G. Moderner Beta: odtworzenie klimatu starej generacji przez warstwy, nie tylko kolory

Obecne są warianty `Classic14a08Biome`, presety Classic 0.0.14a_08 i Classic 0.30, a także `level/biome/provider/fractal/layers/InitRiverLayer.java`, `ComputeRiverLayer.java`, `MixRiverLayer.java`. System oldschool może więc łączyć fraktalne rozmieszczenie biomów i rzek z osobnym generatorem chunków. **Nowy Sofron Terra Lab nie będzie uruchamiał starej mapy biomów**: przenosimy tylko estetykę geomorfologiczną (ścianki, zwisy, wyjątkowe formacje), zachowując biomy oraz struktury Vanilla 26.3.

### H. TerraBlender: zgodność regionalna to funkcja API, nie zadanie rzeźby terenu

Analizowany kod TerraBlender udostępnia interfejsy dla regionów i biome providers. W świetle wymogu „tylko biomy Vanilla” wprowadzenie wymaganej zależności TerraBlender nie przyniosłoby korzyści proporcjonalnej do złożoności. Nie jest potrzebne, by generować własne góry czy kras.

## Architektura hydrologiczna, którą należy zbudować po wyborach

Poniższy pipeline jest **proponowanym nowym algorytmem**, a nie skopiowanym rozwiązaniem żadnego autora:

```text
Kontynenty + geologia + erozja
            ↓
Globalna mapa wysokości / szelfy / brzegi
            ↓
Wyrównanie małych zamkniętych zagłębień (priority flood)
            ↓
Wybór kierunku odpływu i akumulacja zlewni
            ↓
Źródła górskie → dopływy → ujście/ocean
            ↓
Profil powierzchni wody (spadek; progi wodospadów)
            ↓
Wycięcie odpowiedniej głębokości i szerokości koryt
            ↓
Wypełnienie prawdziwymi blokami wody; aquifery pod ziemią
            ↓
Plaże rzeczne, żwir, piasek, test żeglowności
            ↓
Testy seamów między chunkami, rzeczywistych bloków i F3
```

Na makroskali dorzecza muszą być deterministyczne i niezależne od kolejności powstawania chunków. Najpierw ustala się spadki, ujścia i wysokości płynów, **dopiero potem** pozwala Minecraftowi umieścić bloki w odpowiednich etapach. Struktury Vanilla i wejścia do jaskiń są ograniczeniami terenowymi. Osobne `sea_level` i `aquifers.surface_level` mogą być poprawne tylko wtedy, gdy oba korzystają z *tej samej, spójnej klasyfikacji oceanu i preliminarnej wysokości*. Zmiana liczby bez naprawy klasyfikacji nie przywróci poprawnego oceanu.

## Co trzeba zweryfikować po uruchomieniu moda

1. **Wybór jakościowy:** zrzuty kilku seedów do oceny klifów Beta, kolumn i pasm względem autorskich schematów (nie wymaga identycznych pikseli).
2. **Wybór ilościowy:** skan pokrycia lądem np. próba siatki 10 000 punktów w promieniu 8–20 km; udział lądu porównać do ustawienia użytkownika.
3. **Góry:** profile wysokości oraz spójne długości łańcuchów dla co najmniej 4 seedów; ograniczenia Y i brak twardych granic makroregionów.
4. **Rzeki:** unikalne ID dorzeczy, poprawność ujścia, brak „suchych rzek”, monotoniczny spadek wody poza naturalnymi progami.
5. **Vanilla:** kontrola *rzeczywistego* biome ID F3, rozkładu struktur, wysokości materiałów powierzchniowych i podziemnych jaskiń.
6. **Fabric 26.3:** test realnego serwera z włączonym modowym datapackiem (w tym mod API), nie tylko wynik kompilacji Gradle.