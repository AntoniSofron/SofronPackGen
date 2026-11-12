#!/usr/bin/env python3
"""Static connectivity checks; no claims about water placement or hydrology."""
import json
from pathlib import Path

root=Path("src/main/resources/data/sofron_pack/worldgen/density_function/terrain")
def load(name):
    return json.loads((root/(name+".json")).read_text())
def refs(x):
    if isinstance(x,str):
        yield x
    elif isinstance(x,dict):
        for v in x.values(): yield from refs(v)
    elif isinstance(x,list):
        for v in x: yield from refs(v)
extended=set(refs(load("extended_base")))
assert "sofron_pack:terrain/tributary_valley" in extended
assert "sofron_pack:terrain/lake_basin" in extended
trib=set(refs(load("tributary_valley")))
assert "sofron_pack:terrain/river_axis" in trib
assert "sofron_pack:climate/continents" in trib
assert "sofron_pack:terrain/lake_mask" in set(refs(load("lake_basin")))
print("Tributary and lake basin density connectivity: PASS")
