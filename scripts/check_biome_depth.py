#!/usr/bin/env python3
"""Diagnostic guard: never feed solid terrain density into climate depth."""
import json
from pathlib import Path
p=Path("src/main/resources/data/minecraft/worldgen/noise_settings/overworld.json")
o=json.loads(p.read_text())
assert o["noise_router"]["depth"] == 0, "Diagnostic biome depth must remain neutral"
assert o["noise_router"]["final_density"] == "sofron_pack:terrain/final_density"
print("Biome depth isolation: PASS (diagnostic; underground biomes disabled)")
