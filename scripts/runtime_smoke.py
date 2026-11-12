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
PASSWORD = "sofron-ci-local-test-only"

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
        "level-name=worldgen-smoke\n"
        "level-seed=3880636525852909587\n"
        "online-mode=false\n"
        "generate-structures=false\n"
        "difficulty=peaceful\n"
        "max-tick-time=-1\n"
        "view-distance=4\n"
        "simulation-distance=4\n"
        "enable-rcon=true\n"
        "rcon.password=" + PASSWORD + "\n"
        f"rcon.port={PORT}\n"
        "server-port=25566\n"
        "motd=Gen2 runtime test\n", encoding="utf-8"
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
            if not re.search(r"\\bfabric\\b", packs, re.IGNORECASE):
                raise AssertionError("Fabric Mods data pack is not enabled: the server is testing Vanilla, not Sofron Pack Gen worldgen.")

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
            top_water = []
            lower_water = []
            for dx, dz in ((0,0),(8,8),(-8,-8),(16,0),(0,16)):
                px, pz = x+dx, z+dz
                biome63, biome_msg = passes(f"execute if biome {px} 63 {pz} minecraft:deep_ocean")
                water54, msg54 = passes(f"execute if block {px} 54 {pz} minecraft:water")
                water63, msg63 = passes(f"execute if block {px} 63 {pz} minecraft:water")
                print(f"sample ({px},{pz}) biome at Y63={biome63}: {biome_msg}; water Y54={water54} ({msg54}); Y63={water63} ({msg63})", flush=True)
                lower_water.append(water54)
                top_water.append(water63)
            print("First deep-ocean water/solid column:", flush=True)
            for y in range(49, 67):
                found = []
                for block in ("water", "air", "stone", "grass_block", "dirt", "ice", "snow_block"):
                    yes, _ = passes(f"execute if block {x} {y} {z} minecraft:{block}")
                    if yes: found.append(block)
                print(f"  Y={y}: {','.join(found) if found else 'other material'}", flush=True)
            if not any(lower_water):
                raise AssertionError("Deep ocean at sampled coordinates has no water at Y54.")
            if not any(top_water):
                raise AssertionError("Deep ocean has water at Y54 but no water at Y63: sea surface is incomplete; aquatic generation cannot be accepted.")
            print("PASS: server world loads and a natural deep ocean contains water blocks.", flush=True)
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
