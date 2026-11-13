# Implementation contract after the 84 choices

1. Accept the generated JSON from the offline configurator, validate ranges and requirements.
2. Implement a **new** Minecraft 26.3 worldgen from scratch — no old SofronPackGen terrain functions, biomes, carvers or karst.
3. Keep **only Vanilla biomes and Vanilla structures**.
4. Start with consistent continents, coastlines and FULL real ocean water, and test worldgen in an actual 26.3 Fabric server.
5. Add distinct mountain ranges + Beta-like cliff regions + erosion; check local material rule and biome selection.
6. Implement a *new* karst pillar feature constrained to geological region masks. No Biomes O' Plenty code or textures.
7. Add Alpha/Classic-inspired island and valley regions without adding custom biomes.
8. Implement rivers using height-gradient/watershed routing, explicit water fill and biome-aware sand/gravel banks.
9. Connect lakes and underground rivers to that drainage system, not random cavities.
10. Test 4+ seeds, 24+ sample sites, fluid Y-levels, surface/underground biomes, vanilla structures, chunk seams and worldgen time.
11. Keep click-to-build BUILD.bat and BUILD_AND_INSTALL.bat working; CI must compile and run a Minecraft server integration test.
12. Do not call any terrain subsystem complete until measured in game and compared to the approved configuration.

**Current branch status:** questionnaire and buildable Java/Fabric scaffold only. **The requested worldgen is NOT implemented yet.**
