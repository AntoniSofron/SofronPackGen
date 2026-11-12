#!/usr/bin/env python3
"""Static registry and density-function graph validation (not a Minecraft runtime test)."""
import json
from pathlib import Path

ROOT = Path("src/main/resources/data")
errors = []
parsed = {}
for path in ROOT.rglob("*.json"):
    try:
        parsed[path] = json.loads(path.read_text(encoding="utf-8"))
    except (ValueError, UnicodeError) as exc:
        errors.append(f"{path}: {exc}")

def walk(node):
    if isinstance(node, dict):
        yield node
        for value in node.values():
            yield from walk(value)
    elif isinstance(node, list):
        for value in node:
            yield from walk(value)

def density_path(identifier):
    ns, name = identifier.split(":", 1)
    return ROOT / ns / "worldgen/density_function" / (name + ".json")

def noise_path(identifier):
    ns, name = identifier.split(":", 1)
    return ROOT / ns / "worldgen/noise" / (name + ".json")

for path, obj in parsed.items():
    for item in walk(obj):
        if item.get("type") == "minecraft:y_clamped_gradient":
            errors.append(f"{path}: unsupported density type minecraft:y_clamped_gradient in Minecraft 26.3; use minecraft:gradient")
        if item.get("type") == "minecraft:gradient":
            if item.get("axis") not in ("x", "y", "z") or item.get("tiling") not in ("clamp_to_edge", "repeat", "mirrored_repeat"):
                errors.append(f"{path}: invalid 26.3 gradient axis/tiling")
            if not all(isinstance(item.get(k), int) for k in ("from_coordinate", "to_coordinate")) or item.get("from_coordinate") == item.get("to_coordinate"):
                errors.append(f"{path}: invalid 26.3 gradient coordinates")
            if not all(isinstance(item.get(k), (int, float)) for k in ("from_value", "to_value")):
                errors.append(f"{path}: invalid 26.3 gradient values")
        if item.get("type") == "minecraft:noise":
            ident = item.get("noise")
            if isinstance(ident, str) and ident.startswith("sofron_pack:") and noise_path(ident) not in parsed:
                errors.append(f"{path}: missing noise {ident}")
        spline = item.get("spline")
        if item.get("type") == "minecraft:spline" and isinstance(spline, dict):
            points = spline.get("points", [])
            xs = [p.get("location") for p in points]
            if len(points) < 2 or any(not isinstance(x, (float, int)) for x in xs) or any(a >= b for a, b in zip(xs, xs[1:])):
                errors.append(f"{path}: invalid spline point ordering")
        for field in ("left", "right", "input", "coordinate", "function", "chunk_surface_level", "continents", "depth", "erosion", "final_density", "ridges"):
            val = item.get(field)
            if isinstance(val, str) and val.startswith("sofron_pack:") and density_path(val) not in parsed:
                errors.append(f"{path}: missing density {val}")
        if path.parts[-3:-1] == ("worldgen", "noise") and "octave_count" in item:
            if len(item.get("amplitude_modifiers", [])) != item["octave_count"]:
                errors.append(f"{path}: noise octave mismatch")

settings = ROOT / "minecraft/worldgen/noise_settings/overworld.json"
if settings in parsed:
    cfg = parsed[settings]
    if cfg.get("sea_level") != 64:
        errors.append("Expected sea level 64")
    if cfg.get("noise", {}).get("height") != 384:
        errors.append("Expected noise height 384")
    for key, value in cfg.get("noise_router", {}).items():
        if isinstance(value, str) and value.startswith("sofron_pack:") and density_path(value) not in parsed:
            errors.append(f"noise_router.{key}: missing density {value}")
else:
    errors.append("Missing overworld noise settings")

for error in errors:
    print("ERROR:", error)
print(f"Validated {len(parsed)} JSON resources; {len(errors)} error(s)")
raise SystemExit(bool(errors))
