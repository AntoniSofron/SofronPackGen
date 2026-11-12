#!/usr/bin/env python3
"""Regression guards for known catastrophic worldgen wiring mistakes."""
import json
from pathlib import Path

root = Path("src/main/resources/data/sofron_pack/worldgen/density_function")
def load(name):
    return json.loads((root / (name + ".json")).read_text())

final = load("terrain/final_density")
assert final["type"] == "minecraft:min"
solid = final["left"]
assert solid["type"] == "minecraft:add", "Karst must modify terrain rather than max-fill empty sky"
assert solid["right"] == "sofron_pack:karst/height_limited"
karst = load("karst/height_limited")
assert karst["type"] == "minecraft:mul"
assert karst["right"]["type"] == "minecraft:mul"
assert karst["right"]["right"]["to_value"] == 0
assert final["right"]["right"] == "sofron_pack:caves/gated_underground_channel"
channel = load("caves/gated_underground_channel")
assert channel["type"] == "minecraft:max", "Gate underground channels away from the surface"
assert channel["right"] == "sofron_pack:caves/depth_gate"
print("Density wiring invariants: PASS (static checks only)")
