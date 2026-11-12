#!/usr/bin/env python3
"""Conservative guards against exposed caves and disabled aquifers."""
import json
from pathlib import Path
root=Path("src/main/resources/data")
def load(p):
    return json.loads((root/p).read_text(encoding="utf-8"))
settings=load("minecraft/worldgen/noise_settings/overworld.json")
gate=load("sofron_pack/worldgen/density_function/caves/depth_gate.json")
assert settings["sea_level"] == 64
assert settings["default_fluid"] == "minecraft:water"
assert settings["aquifers"]["exclusion"] == 0
assert settings["aquifers"]["surface_level"] == "sofron_pack:terrain/chunk_surface_level"
assert settings["noise_router"]["chunk_surface_level"] == "sofron_pack:terrain/chunk_surface_level"
assert gate["type"] == "minecraft:gradient" and gate["axis"] == "y"
assert gate["to_coordinate"] <= 64 and gate["to_value"] >= 2
print("Surface and aquifer safety checks: PASS (static only)")
