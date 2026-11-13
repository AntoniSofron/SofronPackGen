#!/usr/bin/env python3
"""Regression contract: peaks up to Y256 grow from land; no high-altitude slabs.

Checks the active Minecraft 26.3 JSON graph and samples its deterministic
mountain-column formula. Runtime chunk tests remain separately required.
"""
import json
import math
from pathlib import Path

root = Path("src/main/resources/data")
terrain = root / "sofron_terra_lab/worldgen/density_function/terrain"
load = lambda path: json.loads(path.read_text(encoding="utf-8"))
j = lambda name: load(terrain / (name + ".json"))

spec = load(Path("config/approved-worldgen-spec.json"))["parameters"]
settings = load(root / "minecraft/worldgen/noise_settings/overworld.json")
assert spec["seaLevel"] == settings["sea_level"] == 63
assert spec["peakHeight"] == 256

final = j("final_density")
assert final["type"] == "minecraft:max"
assert final["right"] == "sofron_terra_lab:terrain/mountain_ranges"

mass = j("mountain_ranges")
assert mass["type"] == "minecraft:min"
alt = mass["left"]
assert alt["type"] == "minecraft:add"
g = alt["left"]
amp = alt["right"]
anchor = mass["right"]
assert g["type"] == "minecraft:gradient" and g["axis"] == "y"
assert g["from_coordinate"] == 80 and g["to_coordinate"] == 320
assert g["from_value"] > g["to_value"]
assert amp["type"] == "minecraft:mul"
assert amp["right"] == "sofron_terra_lab:terrain/mountain_strength"
assert anchor["type"] == "minecraft:gradient"
assert anchor["from_coordinate"] == 24 and anchor["to_coordinate"] == 64
assert anchor["from_value"] < 0 < anchor["to_value"]

def gradient(node, y):
    low, high = node["from_coordinate"], node["to_coordinate"]
    t = min(1.0, max(0.0, (y-low)/(high-low)))
    return node["from_value"] + t * (node["to_value"]-node["from_value"])

def mountain(s, y):
    return min(gradient(g, y) + amp["left"] * s, gradient(anchor, y))

# Each vertical solid interval MUST remain attached to underlying ground,
# including at the configured Y256 summit: no isolated positive-density band.
for strength in [0, 0.02, 0.05, 0.1, 0.2, 0.4, 0.6, 0.8, 0.9, 1]:
    ys = [y for y in range(-64, 320) if mountain(strength, y) > 1e-6]
    if ys:
        assert ys == list(range(ys[0], ys[-1] + 1)), (strength, ys[0], ys[-1])
        assert ys[0] <= 40, (strength, ys[0])
        assert ys[-1] <= spec["peakHeight"], (strength, ys[-1])
    if strength == 1:
        assert mountain(strength, 255) > 0
        assert abs(mountain(strength, 256)) < 1e-6
        assert mountain(strength, 257) < 0
    if strength == 0:
        assert not ys

# Other positive landforms must die away well before the peak ceiling:
# they may roughen cliffs/karst but must not create an independent Y256 sky slab.
beta = j("beta_overhangs")
assert beta["right"]["right"]["right"]["left"]["right"]["to_coordinate"] <= 190
karst = j("karst_spires")
assert karst["right"]["right"]["right"]["left"]["right"]["to_coordinate"] <= 205
surface = j("surface_envelope")
assert surface["right"]["to_coordinate"] <= 224
upland = j("upland_lift")
assert upland["right"]["right"]["right"]["to_coordinate"] <= 224
relief = j("sculpted_relief")
def references(obj):
    if isinstance(obj, str):
        return {obj}
    if isinstance(obj, dict):
        return set().union(*(references(v) for v in obj.values()))
    if isinstance(obj, list):
        return set().union(*(references(v) for v in obj))
    return set()
assert "sofron_terra_lab:terrain/mountain_ranges" not in references(relief), "Mountain mass may not be added as an altitude pulse"
assert "sofron_terra_lab:terrain/upland_lift" in references(relief)
print("PASS: grounded mountain density remains vertically connected; peak target Y256; sea Y63")
