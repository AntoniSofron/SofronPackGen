#!/usr/bin/env python3
import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]/"src/main/resources"
files=list(root.rglob("*.json"))
problems=[]
def walk(x):
    if isinstance(x,dict):
        yield x
        for v in x.values():yield from walk(v)
    elif isinstance(x,list):
        for v in x:yield from walk(v)
for f in files:
    try:o=json.loads(f.read_text(encoding="utf-8"))
    except Exception as e:
        problems.append(f"{f}: invalid JSON: {e}");continue
    for n in walk(o):
        if isinstance(n.get("points"),list) and "coordinate" in n:
            xs=[p.get("location") for p in n["points"]]
            if any(not isinstance(x,(int,float)) for x in xs) or any(b<=a for a,b in zip(xs,xs[1:])):
                problems.append(f"{f}: unsorted/duplicate spline points")
for p in problems:print("ERROR:",p)
print(f"JSON files: {len(files)}, errors: {len(problems)}")
raise SystemExit(bool(problems))
