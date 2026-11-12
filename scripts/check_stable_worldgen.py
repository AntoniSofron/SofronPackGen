#!/usr/bin/env python3
"""Regression checks for the active Minecraft 26.3 Overworld generation pipeline.

This is a schema/reference test; it is not a substitute for the runtime smoke test.
"""
import json
from pathlib import Path

root = Path("src/main/resources/data")
def read(path):
    return json.loads((root/path).read_text(encoding="utf-8"))

s = read("minecraft/worldgen/noise_settings/overworld.json")
router = s["noise_router"]
for name in ("continents", "erosion", "depth", "ridges", "temperature", "vegetation"):
    assert router[name] == f"minecraft:overworld/{name}", (name, router[name])
assert router["chunk_surface_level"] == "minecraft:overworld/chunk_surface_level"
assert router["final_density"] == "sofron_pack:terrain/stable_final_density"
assert s["material_rule"] == "minecraft:overworld"
assert s["sea_level"] == 64
assert s["default_fluid"] == "minecraft:water"
assert s["noise"] == {"min_y": -64, "height": 384}

aquifers = s["aquifers"]
assert aquifers["surface_level"] == "minecraft:overworld/chunk_surface_level"
assert aquifers["exclusion"]["type"] == "minecraft:min"
assert aquifers["exclusion"]["left"]["right"] == "minecraft:overworld/erosion"
assert aquifers["exclusion"]["right"]["left"]["left"] == "minecraft:overworld/depth"

g = "sofron_pack/worldgen/density_function/terrain/"
final = read(g + "stable_final_density.json")
assert final == {
    "type": "minecraft:add",
    "left": "minecraft:overworld/final_density",
    "right": "sofron_pack:terrain/stable_landform",
}
landform = read(g + "stable_landform.json")
assert landform["type"] == "minecraft:mul"
assert landform["left"] == "sofron_pack:terrain/stable_land_mask"

mask = read(g + "stable_land_mask.json")["spline"]["points"]
assert mask == sorted(mask, key=lambda p: p["location"])
assert all(p["value"] == 0 for p in mask if p["location"] <= -0.12)
assert any(p["value"] > 0 for p in mask if p["location"] > 0.2)

fade = read(g + "stable_vertical_envelope.json")
assert fade["type"] == "minecraft:mul"
assert fade["left"]["type"] == fade["right"]["type"] == "minecraft:gradient"
assert fade["left"]["from_value"] == 0
assert fade["right"]["to_value"] == 0
assert fade["left"]["to_coordinate"] <= 88
assert fade["right"]["from_coordinate"] >= 200

active = set()
def collect(v):
    if isinstance(v, str) and v.startswith("sofron_pack:"):
        active.add(v)
    elif isinstance(v, dict):
        for vv in v.values(): collect(vv)
    elif isinstance(v, list):
        for vv in v: collect(vv)
collect(s)
for name in ("stable_final_density", "stable_landform", "stable_mountain_profile", "stable_land_mask", "stable_vertical_envelope"):
    obj = read(g + name + ".json")
    collect(obj)
assert not any(x.endswith(("caves/combined", "karst/height_limited", "terrain/extended_base", "terrain/river_valley")) for x in active), active
print("Minecraft 26.3 biome, fluid, cave and land-only density routing: PASS (static)")
