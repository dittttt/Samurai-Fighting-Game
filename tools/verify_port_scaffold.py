#!/usr/bin/env python
import json
import os
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
REQUIRED = [
    "godot/project.godot",
    "godot/scenes/main/Main.tscn",
    "godot/scripts/autoload/GameManager.gd",
    "godot/scripts/player/Player.gd",
    "godot/scripts/enemies/RoninEnemy.gd",
    "godot/scripts/combat/AttackData.gd",
    "godot/scripts/combat/CombatActor.gd",
    "tools/aseprite/export_aseprite.sh",
    "tools/godot/verify_godot.sh",
    "docs/GODOT_PORT_PLAN.md",
]

def fail(message: str) -> None:
    raise SystemExit(f"VERIFY FAIL: {message}")

for rel in REQUIRED:
    if not (ROOT / rel).exists():
        fail(f"missing {rel}")

for rel in ["godot/data/dialogue/prototype_intro.json", "godot/data/attacks/wago_light_slash.json", "godot/data/attacks/wago_heavy_slash.json"]:
    with open(ROOT / rel, "r", encoding="utf-8") as f:
        json.load(f)

scene = (ROOT / "godot/scenes/main/Main.tscn").read_text(encoding="utf-8")
refs = re.findall(r'path="res://([^"]+)"', scene)
for ref in refs:
    if not (ROOT / "godot" / ref).exists():
        fail(f"scene reference missing: res://{ref}")

project = (ROOT / "godot/project.godot").read_text(encoding="utf-8")
for token in ['config/name="WAGO"', 'run/main_scene="res://scenes/main/Main.tscn"', 'GameManager']:
    if token not in project:
        fail(f"project.godot missing token {token}")

aseprite_candidates = [
    os.environ.get("ASEPRITE_PATH", ""),
    "E:/Steam/steamapps/common/Aseprite/Aseprite.exe",
    "C:/Program Files/Aseprite/Aseprite.exe",
    "C:/Program Files (x86)/Steam/steamapps/common/Aseprite/Aseprite.exe",
]
aseprite_found = [p for p in aseprite_candidates if p and Path(p).exists()]

godot_candidates = [
    os.environ.get("GODOT_PATH", ""),
    "C:/Softwares/Godot_v4.7-stable_win64_console.exe",
    "C:/Softwares/Godot_v4.7-stable_win64.exe",
    "C:/Softwares/Godot.exe",
]
godot_found = [p for p in godot_candidates if p and Path(p).exists()]

print(f"godot_scaffold=PASS files={len(REQUIRED)} json=PASS scene_refs={len(refs)}")
print("aseprite=" + (aseprite_found[0] if aseprite_found else "NOT_FOUND"))
print("godot=" + (godot_found[0] if godot_found else "NOT_FOUND"))

if godot_found:
    godot = godot_found[0]
    version = subprocess.run([godot, "--version"], cwd=str(ROOT), text=True, capture_output=True, check=True)
    print("godot_version=" + version.stdout.strip())
    for args in (["--headless", "--path", str(ROOT / "godot"), "--quit"], ["--headless", "--path", str(ROOT / "godot"), "--quit-after", "1"]):
        proc = subprocess.run([godot] + args, cwd=str(ROOT), text=True, capture_output=True)
        output = (proc.stdout or "") + (proc.stderr or "")
        if proc.returncode != 0 or "SCRIPT ERROR" in output or "ERROR:" in output:
            print(output)
            fail(f"Godot validation failed for args: {' '.join(args)}")
    print("godot_headless=PASS")
