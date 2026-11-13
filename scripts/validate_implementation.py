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
assert approved["seaLevel"] == 64 and approved["worldHeight"] == "320"

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
        if "/worldgen/noise/" not in str(path):
            check_ref(node, "density_function", path)
for path, obj in data.items():
    walk(obj, path)

s = data[ROOT / "minecraft/worldgen/noise_settings/overworld.json"]
assert s["sea_level"] == 64
assert s["noise"] == {"min_y": -64, "height": 384}
assert s["default_fluid"] == "minecraft:water"
assert s["material_rule"] == "minecraft:overworld"
assert s["noise_router"]["final_density"] == "sofron_terra_lab:terrain/final_density"
assert s["noise_router"]["depth"] == "minecraft:overworld/depth"
assert s["noise_router"]["continents"] == "minecraft:overworld/continents"
assert s["noise_router"]["chunk_surface_level"] == "minecraft:overworld/chunk_surface_level"
assert s["aquifers"]["surface_level"] == "minecraft:overworld/chunk_surface_level"
final = data[ROOT / "sofron_terra_lab/worldgen/density_function/terrain/final_density.json"]
assert final["left"] == "minecraft:overworld/final_density"
assert final["right"] == "sofron_terra_lab:terrain/sculpted_relief"
land_gate = data[ROOT / "sofron_terra_lab/worldgen/density_function/terrain/land_gate.json"]
assert all(p["value"] == 0 for p in land_gate["spline"]["points"] if p["location"] <= -0.12)
ocean = data[ROOT / "sofron_terra_lab/worldgen/density_function/terrain/ocean_trenches.json"]
assert ocean["type"] == "minecraft:mul" and ocean["left"] < 0
assert not any(('worldgen', 'biome') == p.parts[-3:-1] for p in data)
assert not any(p.parts[-3:-1] in {('worldgen','structure'), ('worldgen','structure_set')} for p in data)
assert not any("sofron_pack" in p.parts for p in data), "Legacy namespace prohibited"
print(f"PASS: 84 approved choices, {len(data)} parsed worldgen files, vanilla biome/aquifer safeguards")
