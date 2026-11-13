#!/usr/bin/env python3
"""Validate approved schema contract, not yet Minecraft worldgen."""
import json
from pathlib import Path

d=json.loads(Path("design/catalog.json").read_text(encoding="utf-8"))
qs=d["questions"]
assert d["minecraft"]=="26.3"
assert len(qs)==84 and len(d["categories"])==11 and len(d["audit"])==11
keys=[q["key"] for q in qs]
assert len(set(keys))==len(keys)
for q in qs:
    assert q["category"] in {c["id"] for c in d["categories"]}
    if q["type"]=="range": assert q["min"]<=q["default"]<=q["max"]
    elif q["type"]=="choice": assert q["default"] in [v for v,_ in q["options"]]
    elif q["type"]=="bool": assert isinstance(q["default"],bool)
    else: raise AssertionError(q["type"])
assert next(q["default"] for q in qs if q["key"]=="vanillaBiomesOnly")
assert next(q["default"] for q in qs if q["key"]=="vanillaStructuresOnly")
assert Path("design/Sofron_Terra_Lab_Konfigurator.html").exists()
assert Path("src/main/resources/data/minecraft/worldgen/noise_settings/overworld.json").exists(), "Approved configuration must activate real worldgen"
print(f"PASS: {len(qs)} choices; vanilla biomes and structures; approved worldgen enabled")
