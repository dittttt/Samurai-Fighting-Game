#!/usr/bin/env python
import json
import os
import re
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
found = [p for p in aseprite_candidates if p and Path(p).exists()]
print(f"godot_scaffold=PASS files={len(REQUIRED)} json=PASS scene_refs={len(refs)}")
print("aseprite=" + (found[0] if found else "NOT_FOUND"))
