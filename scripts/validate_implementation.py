#!/usr/bin/env python3
"""Validate the owner's immutable 84-setting contract and the active 26.3 worldgen graph."""
from pathlib import Path
import json

ROOT = Path("src/main/resources/data")
settings = Path("config/approved-worldgen-spec.json")
assert settings.exists(), "Approved settings not committed"
cfg = json.loads(settings.read_text(encoding="utf-8"))
catalog = json.loads(Path("design/catalog.json").read_text(encoding="utf-8"))
assert cfg["schema"] == "sofron-terra-lab-v1" and cfg["minecraft"] == "26.3"
assert cfg["requirements"] == {
    "vanillaBiomesOnly": True, "vanillaStructuresOnly": True,
    "noLegacyCode": True, "previewsAreIllustrative": True,
}
approved = cfg["parameters"]
questions = catalog["questions"]
assert len(questions) == len(approved) == 84, (len(questions), len(approved))
assert set(approved) == {q["key"] for q in questions}
for q in questions:
    value = approved[q["key"]]
    if q["type"] == "range":
        assert isinstance(value, (int, float)) and not isinstance(value, bool)
        assert q["min"] <= value <= q["max"], (q["key"], value)
    elif q["type"] == "choice":
        assert value in [v for v, _ in q["options"]], (q["key"], value)
    else:
        assert q["type"] == "bool" and isinstance(value, bool)
assert approved["vanillaBiomesOnly"] and approved["vanillaStructuresOnly"]
assert approved["seaLevel"] == 63 and approved["worldHeight"] == "320"

data = {}
for path in ROOT.rglob("*.json"):
    data[path] = json.loads(path.read_text(encoding="utf-8"))
def check_ref(identifier, kind, path):
    name, key = identifier.split(":", 1)
    target = ROOT / name / "worldgen" / kind / (key + ".json")
    assert target in data, f"{path}: missing {kind} {identifier}"

def walk(node, path):
    if isinstance(node, dict):
        if node.get("type") == "minecraft:noise" and isinstance(node.get("noise"), str):
            if node["noise"].startswith("sofron_terra_lab:"):
                check_ref(node["noise"], "noise", path)
        if node.get("type") == "minecraft:spline":
            pts = node["spline"]["points"]
            assert all(a["location"] < b["location"] for a, b in zip(pts, pts[1:])), path
        for key, value in node.items():
            if key == 'noise' and node.get('type') == 'minecraft:noise':
                continue
            walk(value, path)
    elif isinstance(node, list):
        for value in node: walk(value, path)
    elif isinstance(node, str) and node.startswith("sofron_terra_lab:"):
        # Only density-function JSON may reference the custom density graph.
        # New Vanilla 26.3 worldgen/feature and placed_feature IDs are checked
        # separately below; they are NOT density functions.
        if "worldgen/density_function" in path.as_posix() or "worldgen/noise_settings" in path.as_posix():
            check_ref(node, "density_function", path)
for path, obj in data.items():
    walk(obj, path)

s = data[ROOT / "minecraft/worldgen/noise_settings/overworld.json"]
assert s["sea_level"] == 63
assert s["noise"] == {"min_y": -64, "height": 384}
assert s["default_fluid"] == "minecraft:water"
assert s["material_rule"] == "minecraft:overworld"
assert s["noise_router"]["final_density"] == "sofron_terra_lab:terrain/final_density"
assert s["noise_router"]["depth"] == "minecraft:overworld/depth"
assert s["noise_router"]["continents"] == "minecraft:overworld/continents"
assert s["noise_router"]["chunk_surface_level"] == "minecraft:overworld/chunk_surface_level"
assert s["aquifers"]["surface_level"] == "minecraft:overworld/chunk_surface_level"
final = data[ROOT / "sofron_terra_lab/worldgen/density_function/terrain/final_density.json"]
assert final["type"] == "minecraft:max"
assert final["left"]["type"] == "minecraft:add"
assert final["left"]["left"] == "minecraft:overworld/final_density"
assert final["left"]["right"] == "sofron_terra_lab:terrain/sculpted_relief"
assert final["right"] == "sofron_terra_lab:terrain/mountain_ranges"
land_gate = data[ROOT / "sofron_terra_lab/worldgen/density_function/terrain/land_gate.json"]
assert all(p["value"] == 0 for p in land_gate["spline"]["points"] if p["location"] <= -0.12)
ocean = data[ROOT / "sofron_terra_lab/worldgen/density_function/terrain/ocean_trenches.json"]
assert ocean["type"] == "minecraft:mul" and ocean["left"] < 0
core = data[ROOT / "sofron_terra_lab/worldgen/density_function/terrain/mountain_ranges.json"]
assert core["type"] == "minecraft:min"
alt = core["left"]
assert alt["type"] == "minecraft:add"
assert alt["left"]["type"] == "minecraft:gradient" and alt["left"]["axis"] == "y"
assert alt["left"]["from_value"] > alt["left"]["to_value"], "Mountain mass must descend with Y"
assert alt["right"]["right"] == "sofron_terra_lab:terrain/mountain_strength"
assert core["right"]["type"] == "minecraft:gradient" and core["right"]["to_coordinate"] <= 64
for key in ("surface_envelope", "beta_overhangs", "karst_spires", "upland_lift"):
    assert key in {p.stem for p in data}, key
# Only these existing vanilla biomes have palette overrides.
biome_files = {p.relative_to(ROOT).as_posix() for p in data
               if p.relative_to(ROOT).parts[1:-1] == ('worldgen', 'biome')}
assert biome_files == {f'minecraft/worldgen/biome/{name}.json' for name in ('swamp', 'birch_forest', 'old_growth_birch_forest', 'taiga', 'old_growth_spruce_taiga', 'old_growth_pine_taiga')}, biome_files
swamp = data[ROOT / 'minecraft/worldgen/biome/swamp.json']
assert swamp['effects']['grass_color'] == '#688D46'
assert swamp['effects']['foliage_color'] == '#688D46'
assert swamp['effects']['water_color'] == '#68834D'
assert swamp['effects']['grass_color_modifier'] == 'none'
assert swamp['attributes']['minecraft:visual/sky_color'] == '#98C9D9'
assert len(swamp['features']) == 11
vegetation = swamp['features'][9]
assert vegetation.count('minecraft:trees_swamp') == 1
assert vegetation.count('minecraft:patch_waterlily') == 1
assert vegetation.count('sofron_terra_lab:swamp_willows') == 1
assert vegetation.index('sofron_terra_lab:swamp_willows') == vegetation.index('minecraft:trees_swamp') + 1
placed = data[ROOT / 'sofron_terra_lab/worldgen/placed_feature/swamp_willows.json']
assert placed['feature'] == 'sofron_terra_lab:swamp_willows'
assert placed['placement'] == [
    {'type': 'minecraft:count', 'count': 5},
    {'type': 'minecraft:in_square'},
    {'type': 'minecraft:surface_water_depth_filter', 'max_water_depth': 2},
    {'type': 'minecraft:heightmap', 'heightmap': 'OCEAN_FLOOR'},
    {'type': 'minecraft:biome'},
    {'type': 'minecraft:block_predicate_filter', 'predicate': {
        'type': 'minecraft:would_survive', 'state': 'minecraft:oak_sapling'}},
]
feature = data[ROOT / 'sofron_terra_lab/worldgen/feature/swamp_willows.json']
assert feature['type'] == 'sofron_terra_lab:grounded_willow'
for i in range(1, 6):
    assert (ROOT / f'sofron_terra_lab/structure/swamp_willow_{i}.nbt').exists()

assert swamp['carvers'] == ['minecraft:cave','minecraft:cave_extra_underground','minecraft:canyon']
assert swamp['temperature'] == 0.8 and swamp['downfall'] == 0.9

assert not any(p.parts[-3:-1] in {('worldgen','structure'), ('worldgen','structure_set')} for p in data)
assert not any("sofron_pack" in p.parts for p in data), "Legacy namespace prohibited"
print(f"PASS: 84 approved choices, {len(data)} parsed worldgen files, vanilla biome/aquifer safeguards")
