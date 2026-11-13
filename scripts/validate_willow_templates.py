#!/usr/bin/env python3
"""Validate the five user-authored Minecraft 26.3 swamp-willow templates.

NBT was cleaned of *all recorded air/structure_void blocks*. Only solid wood,
leaves and vines remain; foliage can coexist with water and vanilla lilies.
"""
import gzip
import hashlib
import struct
from collections import Counter
from pathlib import Path

EXPECTED = {
    1: ("6e54356e5b14d298c7c388695b7a778885ee03be611bfefc228b8411550a40b9", (12, 17, 12), 359),
    2: ("812eb5b726bb3765ac501adea5f14d0999d2fbaf328c2fdd2ce3dfb25448a9da", (11, 13, 9), 241),
    3: ("b4fb4be6ce715c4a9d1fea02abf91383d7708a4454ed04ccddb22a7912f95ea3", (12, 14, 13), 331),
    4: ("385c2b1909d3bc35efcec89ff86e218babdc12a106a9abe5f48d57c312103190", (12, 13, 13), 289),
    5: ("5f37761763eb02b1a85aa1dcef12fd6acdd21da855e35b280367c0f76bee673b", (10, 10, 11), 216),
}

class NBT:
    def __init__(self, data):
        self.data = data
        self.i = 0

    def read(self, n):
        data = self.data[self.i:self.i + n]
        if len(data) != n:
            raise ValueError("Truncated NBT")
        self.i += n
        return data

    def num(self, fmt):
        return struct.unpack(">" + fmt, self.read(struct.calcsize(">" + fmt)))[0]

    def string(self):
        return self.read(self.num("H")).decode("utf-8")

    def tag(self, kind):
        if kind in (1, 2, 3, 4, 5, 6):
            return self.num({1: "b", 2: "h", 3: "i", 4: "q", 5: "f", 6: "d"}[kind])
        if kind == 7:
            return self.read(self.num("i"))
        if kind == 8:
            return self.string()
        if kind == 9:
            sub = self.num("B")
            n = self.num("i")
            if not 0 <= n < 100_000:
                raise ValueError("Invalid list length")
            return [self.tag(sub) for _ in range(n)]
        if kind == 10:
            result = {}
            while (sub := self.num("B")) != 0:
                result[self.string()] = self.tag(sub)
            return result
        if kind == 11:
            return [self.num("i") for _ in range(self.num("i"))]
        if kind == 12:
            return [self.num("q") for _ in range(self.num("i"))]
        raise ValueError("Unsupported NBT tag " + str(kind))

    def root(self):
        if self.num("B") != 10:
            raise ValueError("Not a structure compound")
        self.string()
        obj = self.tag(10)
        if self.i != len(self.data):
            raise ValueError("Trailing NBT payload")
        return obj

allowed = {"minecraft:oak_log", "minecraft:oak_wood", "minecraft:oak_leaves", "minecraft:vine"}
root = Path("src/main/resources/data/sofron_terra_lab/structure")
total = 0
for i, (digest, size, blockcount) in EXPECTED.items():
    path = root / f"swamp_willow_{i}.nbt"
    compressed = path.read_bytes()
    assert hashlib.sha256(compressed).hexdigest() == digest, (i, "binary checksum mismatch")
    data = NBT(gzip.decompress(compressed)).root()
    assert data["DataVersion"] == 5023
    assert tuple(data["size"]) == size, (i, data["size"])
    assert not data["entities"]
    assert len(data["blocks"]) == blockcount, (i, len(data["blocks"]))
    palette = data["palette"]
    ids = [p["id"] for p in palette]
    assert set(ids) <= allowed, (i, set(ids) - allowed)
    assert all(0 <= b["state"] < len(palette) for b in data["blocks"])
    assert all(
        len(b["pos"]) == 3 and all(0 <= v < size[axis] for axis, v in enumerate(b["pos"]))
        for b in data["blocks"]
    )
    assert len({tuple(b["pos"]) for b in data["blocks"]}) == blockcount
    blocks = Counter(palette[b["state"]]["id"] for b in data["blocks"])
    assert blocks["minecraft:oak_log"] >= 8
    assert blocks["minecraft:oak_leaves"] >= 170
    assert blocks["minecraft:vine"] >= 10
    assert min(
        b["pos"][1] for b in data["blocks"]
        if palette[b["state"]]["id"] in ("minecraft:oak_log", "minecraft:oak_wood")
    ) == 0
    print(f"swamp_willow_{i}: {size} {blockcount} blocks, {blocks}")
    total += blockcount
assert total == 1436, total
print("PASS: five sanitized, deterministic willow NBT templates without air/water/lilies")
