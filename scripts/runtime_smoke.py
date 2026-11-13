#!/usr/bin/env python3
"""Minecraft 26.3 worldgen integration smoke test.

Starts a Fabric development server and uses local RCON to assert that a naturally
located deep ocean contains actual water blocks at sea level, not merely LOD.
Requires Java 25, Gradle, network and a full Fabric Loom runtime.
"""
import os
import pathlib
import re
import socket
import struct
import subprocess
import threading
import time

ROOT = pathlib.Path(__file__).resolve().parents[1]
RUN = ROOT / "run"
LOG = ROOT / "runtime-smoke.log"
PORT = 25575
PASSWORD = "terra-lab-26-3-test-only"

def pack_rcon(request_id, packet_type, body):
    payload = struct.pack("<ii", request_id, packet_type) + body.encode("utf-8") + b"\0\0"
    return struct.pack("<i", len(payload)) + payload

def read_exact(sock, count):
    buf = b""
    while len(buf) < count:
        chunk = sock.recv(count - len(buf))
        if not chunk:
            raise ConnectionError("RCON connection closed")
        buf += chunk
    return buf

def read_packet(sock):
    size = struct.unpack("<i", read_exact(sock, 4))[0]
    if size < 10 or size > 100_000:
        raise ValueError(f"Invalid RCON packet size: {size}")
    data = read_exact(sock, size)
    req_id, packet_type = struct.unpack("<ii", data[:8])
    return req_id, packet_type, data[8:-2].decode("utf-8", errors="replace")

def rcon(command):
    with socket.create_connection(("127.0.0.1", PORT), timeout=20) as sock:
        sock.settimeout(65)
        sock.sendall(pack_rcon(10, 3, PASSWORD))
        authenticated = False
        for _ in range(3):
            request_id, _, _ = read_packet(sock)
            if request_id == -1:
                raise RuntimeError("RCON authentication failed")
            if request_id == 10:
                authenticated = True
                break
        if not authenticated:
            raise RuntimeError("No RCON authentication response")
        sock.sendall(pack_rcon(11, 2, command))
        for _ in range(6):
            request_id, packet_type, message = read_packet(sock)
            if request_id == 11:
                return message
        raise RuntimeError(f"No RCON response for {command}")

def wait_ready(proc, lines, timeout=240):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        if proc.poll() is not None:
            raise RuntimeError(f"Gradle/server exited with code {proc.returncode}; see {LOG}")
        if any("Done (" in line and ("Server thread" in line or "server thread" in line) for line in lines):
            return
        if any("Failed to load registries" in line or "Failed to parse" in line for line in lines):
            raise RuntimeError("Minecraft worldgen registry loading failed")
        time.sleep(1)
    raise TimeoutError("Minecraft server did not reach Done state within 240 seconds")

def main():
    RUN.mkdir(exist_ok=True)
    (RUN / "eula.txt").write_text("eula=true\n", encoding="utf-8")
    (RUN / "server.properties").write_text(
        f"level-name=terra-lab-smoke-{os.environ.get('TERRA_SEED','3880636525852909587')}\n"
        f"level-seed={os.environ.get('TERRA_SEED','3880636525852909587')}\n"
        "online-mode=false\n"
        "generate-structures=true\n"
        "difficulty=peaceful\n"
        "max-tick-time=-1\n"
        "view-distance=4\n"
        "simulation-distance=4\n"
        "enable-rcon=true\n"
        "rcon.password=" + PASSWORD + "\n"
        f"rcon.port={PORT}\n"
        "server-port=25566\n"
        "motd=Terra Lab runtime worldgen test\n", encoding="utf-8"
    )
    lines = []
    print("Starting Minecraft 26.3 Fabric server...", flush=True)
    with LOG.open("w", encoding="utf-8") as logfile:
        proc = subprocess.Popen(
            ["gradle", "--no-daemon", "--console=plain", "runServer"],
            cwd=ROOT, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT, text=True, bufsize=1,
        )
        def reader():
            for line in proc.stdout:
                logfile.write(line)
                logfile.flush()
                lines.append(line)
                if "ERROR" in line or "Done (" in line:
                    print(line.rstrip(), flush=True)
        thread = threading.Thread(target=reader, daemon=True)
        thread.start()
        try:
            wait_ready(proc, lines)
            print("Fabric server started; testing actual water blocks...", flush=True)
            print("Mod load lines:", flush=True)
            for line in lines:
                if "Sofron Pack" in line or "Loading " in line and " mods:" in line or "Loading Minecraft" in line:
                    print("  " + line.strip(), flush=True)
            packs = rcon("datapack list enabled")
            print("Enabled data packs:", packs, flush=True)
            if "sofron_terra_lab" not in packs.lower():
                raise AssertionError("Sofron Terra Lab data pack is not enabled: the server is testing Vanilla, not our worldgen.")

            # Verify all five user-provided NBT structure templates actually
            # load in Minecraft 26.3 and place their original oak-wood trunks.
            # Test above world surface (Y280) to avoid collisions and false positives.
            if os.environ.get("TERRA_SEED", "3880636525852909587") == "3880636525852909587":
                root_offsets = [(5, 5, "oak_log"), (4, 3, "oak_wood"),
                                (5, 6, "oak_log"), (5, 6, "oak_log"),
                                (5, 5, "oak_log")]
                for index, (dx, dz, block) in enumerate(root_offsets, start=1):
                    tx, ty, tz = 32 + index * 32, 280, 32
                    rcon(f"forceload add {tx} {tz}")
                    result = rcon(f"place template sofron_terra_lab:swamp_willow_{index} {tx} {ty} {tz}")
                    probe = rcon(
                        f"execute if block {tx+dx} {ty} {tz+dz} minecraft:{block}")
                    print(f"Willow template {index}: {result}; trunk: {probe}", flush=True)
                    if not ("Test passed" in probe or "count: 1" in probe):
                        raise AssertionError(
                            f"Willow variant {index} NBT failed real placement: {result}; {probe}")
                print("PASS: all five cleaned willow NBT templates place their trunks in Minecraft", flush=True)
                # Validate the *placed feature* registration in a naturally
                # selected vanilla swamp, not only the raw template command.
                located_swamp = rcon("locate biome minecraft:swamp")
                print("Locate actual vanilla swamp:", located_swamp, flush=True)
                swamp_match = re.search(r"\[\s*(-?\d+)\s*,\s*(?:~|-?\d+)\s*,\s*(-?\d+)\s*\]", located_swamp)
                if not swamp_match:
                    raise AssertionError(f"No vanilla swamp found for willow feature test: {located_swamp}")
                wx, wz = map(int, swamp_match.groups())
                successes = 0
                for dx, dz in ((0,0), (8,8), (-8,-8), (16,0), (0,16)):
                    tx, tz = wx+dx, wz+dz
                    rcon(f"forceload add {tx} {tz}")
                    response = rcon(f"place feature sofron_terra_lab:swamp_willows {tx} 80 {tz}")
                    print(f"Willow placed feature test ({tx},{tz}): {response}", flush=True)
                    if "placed \"sofron_terra_lab:swamp_willows\"" in response.lower():
                        successes += 1
                if successes == 0:
                    raise AssertionError(
                        "The new willow placed feature exists but cannot grow at any of five "
                        "sampled natural swamp locations; verify underwater roots / biome placement.")
                print(f"PASS: native willow feature placed successfully in swamp ({successes}/5)", flush=True)

            # Locate a real VANILLA deep-ocean biome, not a generated height-map.
            located = rcon("locate biome minecraft:deep_ocean")
            print("Locate deep ocean:", located, flush=True)
            match = re.search(r"\[\s*(-?\d+)\s*,\s*(?:~|-?\d+)\s*,\s*(-?\d+)\s*\]", located)
            if not match:
                raise AssertionError(f"Could not parse deep-ocean coordinates: {located}")
            x, z = map(int, match.groups())
            rcon(f"forceload add {x} {z}")
            time.sleep(7)
            def passes(command):
                answer = rcon(command)
                return ("Test passed" in answer or "count: 1" in answer), answer
            ocean_samples = []
            for dx, dz in ((0,0),(8,8),(-8,-8),(16,0),(0,16)):
                px, pz = x+dx, z+dz
                biome62, biome_msg = passes(f"execute if biome {px} 62 {pz} minecraft:deep_ocean")
                water54, msg54 = passes(f"execute if block {px} 54 {pz} minecraft:water")
                water62, msg62 = passes(f"execute if block {px} 62 {pz} minecraft:water")
                air63, msg63 = passes(f"execute if block {px} 63 {pz} minecraft:air")
                print(f"sample ({px},{pz}) deep-ocean Y62={biome62}; "
                      f"water Y54={water54}, Y62={water62}; air Y63={air63}; "
                      f"responses={biome_msg};{msg54};{msg62};{msg63}", flush=True)
                if biome62:
                    ocean_samples.append((px, pz, water54, water62, air63))
            print("First deep-ocean water/solid column (sea_level=63):", flush=True)
            for y in range(56, 66):
                found = []
                for block in ("water", "air", "stone", "grass_block", "dirt", "ice"):
                    yes, _ = passes(f"execute if block {x} {y} {z} minecraft:{block}")
                    if yes: found.append(block)
                print(f"  Y={y}: {','.join(found) if found else 'other material'}", flush=True)
            if len(ocean_samples) < 2:
                raise AssertionError(f"Fewer than two confirmed deep-ocean samples: {ocean_samples}")
            missing = [(px, pz, w54, w62, a63) for px, pz, w54, w62, a63 in ocean_samples
                       if not (w54 and w62 and a63)]
            if missing:
                raise AssertionError(f"Sea level mismatch or dry deep ocean: {missing}. "
                                     "Expected water Y54/Y62 and air Y63 (sea_level=63).")
            # Verify Vanilla depth routing never chooses the Deep Dark high above the surface.
            above_samples = ((0, 160, 0), (256, 160, 256), (-256, 160, -256))
            for lx, ly, lz in above_samples:
                is_deep, answer = passes(f"execute if biome {lx} {ly} {lz} minecraft:deep_dark")
                print(f"Deep Dark biome at open-sky Y={ly} ({lx},{lz}): {is_deep} ({answer})", flush=True)
                if is_deep:
                    raise AssertionError(f"Deep Dark biome mapped to high sky / surface at {(lx,ly,lz)}")
            # Smoke-test mountain geometry on actual chunks, not on LOD silhouettes.
            # A single natural arch may have air directly below it; a WHOLE CLUSTER
            # of high rocky columns with no lower support is an accidental sky island.
            peaks = rcon("locate biome minecraft:jagged_peaks")
            print("Locate Vanilla jagged_peaks:", peaks, flush=True)
            peak_match = re.search(r"\[\s*(-?\d+)\s*,\s*(?:~|-?\d+)\s*,\s*(-?\d+)\s*\]", peaks)
            if peak_match:
                px, pz = map(int, peak_match.groups())
                spots = [(px+dx, pz+dz) for dx, dz in
                    ((0,0),(16,0),(-16,0),(0,16),(0,-16),(32,32),(-32,-32),(-32,32),(32,-32))]
                for sx, sz in spots:
                    rcon(f"forceload add {sx} {sz}")
                time.sleep(5)
                def is_solid(sx, y, sz):
                    yes, _ = passes(
                        f"execute unless block {sx} {y} {sz} minecraft:air "
                        f"unless block {sx} {y} {sz} minecraft:cave_air "
                        f"unless block {sx} {y} {sz} minecraft:void_air")
                    return yes
                tall, unsupported, height_profiles = [], [], []
                for sx, sz in spots:
                    # Surface height estimate from *actual generated blocks*.
                    # Unlike Distant Horizons, these are server block queries.
                    top = next((y for y in range(288, 79, -8)
                                if is_solid(sx, y, sz)), None)
                    height_profiles.append((sx, sz, top))
                    if top is None or top < 208:
                        continue
                    supports = [y for y in (112, 144, 176, 192)
                                if is_solid(sx, y, sz)]
                    tall.append((sx, sz, top, supports))
                    if not supports:
                        unsupported.append((sx, sz, top))
                print(f"Jagged peaks real-block height samples: {height_profiles}", flush=True)
                print(f"Mountain columns above Y208: {tall}", flush=True)
                if len(unsupported) >= 3:
                    raise AssertionError(
                        f"Large cluster of unsupported high columns near jagged_peaks: {unsupported}. "
                        "Likely floating landmass, not a connected mountain.")
                print(f"Mountain continuity probe: {len(tall)} high columns, "
                      f"{len(unsupported)} isolated single-column overhangs", flush=True)
            else:
                print("No jagged_peaks located within the biome lookup radius; mountain-specific runtime probe skipped.", flush=True)
            print("PASS: sea Y63 (water through Y62), actual ocean blocks and upper-level biome safety verified.", flush=True)
        finally:
            try:
                rcon("stop")
            except Exception:
                pass
            try:
                proc.wait(timeout=25)
            except subprocess.TimeoutExpired:
                proc.terminate()
                try: proc.wait(timeout=10)
                except subprocess.TimeoutExpired: proc.kill()

if __name__ == "__main__":
    main()
