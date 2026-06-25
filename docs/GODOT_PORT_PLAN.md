# WAGO Godot Port Plan

> **For Hermes:** Use this as the implementation guide for moving WAGO from the Java/Swing prototype to Godot 4.x.

**Goal:** Rebuild the current 1v1 samurai prototype as a Godot vertical slice, then expand it into a Katana ZERO / Blasphemous-inspired story action game.

**Architecture:** Godot scenes own composition; GDScript classes own behavior. Combat uses reusable `CombatActor`, `Hitbox`, `Hurtbox`, and `AttackData` resources so player, enemies, bosses, and Wago's future Bhunt technique share the same foundation.

**Tech Stack:** Godot 4.x, GDScript, Aseprite, JSON story/attack data, GitHub.

---

## Current changes made

- Added `godot/project.godot` configured for pixel-perfect 480x270 viewport scaling.
- Added autoload managers: `GameManager`, `DialogueManager`, `StoryFlags`.
- Added starter combat scripts: `CombatActor`, `AttackData`, `Hitbox`, `Hurtbox`.
- Added starter player and ronin enemy scripts.
- Added a minimal `Main.tscn` arena scene scaffold.
- Added starter attack JSON and prototype dialogue JSON.
- Added an Aseprite export pipeline for the Steam install on `E:`.
- Added `tools/verify_port_scaffold.py` for verification before Godot is installed.

## Aseprite workflow

Source files:

```text
art/aseprite/*.aseprite
```

Export command:

```bash
bash tools/aseprite/export_aseprite.sh
```

Detected Steam path:

```text
E:/Steam/steamapps/common/Aseprite/Aseprite.exe
```

Recommended animation tags:

```text
idle, walk, run, jump, fall, roll, light_attack, heavy_attack, parry, hit, death, bhunt
```

## Next tasks

### Task 1: Install Godot 4.x

Install Godot 4.x, then import:

```text
godot/project.godot
```

Verification:

```bash
python tools/verify_port_scaffold.py
```

Expected:

```text
godot_scaffold=PASS
```

### Task 2: Open the main scene

Open:

```text
godot/scenes/main/Main.tscn
```

Fix any editor auto-upgrade warnings Godot reports.

### Task 3: Replace placeholder boxes with real sprites

1. Create `art/aseprite/wago.aseprite`.
2. Tag animations in Aseprite.
3. Run `bash tools/aseprite/export_aseprite.sh`.
4. Import generated PNG/JSON into Godot.
5. Add an `AnimatedSprite2D` or `AnimationPlayer` to `Player.tscn` once player scene extraction begins.

### Task 4: Extract reusable scenes

Split the current one-file `Main.tscn` scaffold into:

```text
godot/scenes/player/Player.tscn
godot/scenes/enemies/RoninEnemy.tscn
godot/scenes/combat/Hitbox.tscn
godot/scenes/combat/Hurtbox.tscn
godot/scenes/ui/HUD.tscn
```

### Task 5: Recreate the Java 1v1 prototype in Godot

Acceptance criteria:

- Wago moves left/right.
- Wago jumps.
- Wago rolls.
- Wago light/heavy attacks.
- Ronin follows and attacks.
- Hitboxes damage hurtboxes once per active window.
- HUD updates health.
- Victory/defeat state triggers.

### Task 6: Start WAGO vertical slice

After 1v1 works, create the Elk escape vertical slice:

```text
Title screen -> Elk intro dialogue -> tutorial -> Gabal guard fight -> escape wall -> mini boss
```

## Notes

This scaffold is intentionally text-only and agent-editable. It can be committed before Godot is installed. Godot will generate `.godot/` cache files locally after first open; those are ignored by Git.


## Local Godot verification

Godot was found at:

```text
C:/Softwares/Godot_v4.7-stable_win64_console.exe
```

Run this before committing Godot changes:

```bash
bash tools/godot/verify_godot.sh
python tools/verify_port_scaffold.py
```


## Player quickstart

See `docs/PLAYER_QUICKSTART.md`, or double-click `RUN_WAGO.bat` from the repository root.


## Misty Java prototype assets are source material

The original Java 1v1 game under `Misty/` is not discarded. Its samurai/ronin atlases, village tiles, UI art, and music have been copied into `godot/assets/legacy_misty/` and documented in `docs/MISTY_ASSET_INVENTORY.md`. The current Godot prototype uses those original character atlases and the old village tile data instead of only placeholder shapes.

For the metroidvania, treat Misty as the combat/asset seed:

1. Preserve `1v1` branch as the Java snapshot.
2. Use `main` for the Godot metroidvania.
3. Migrate old combat feel first.
4. Expand into WAGO story regions: Elk, roads of Froy, Pahica tournament, Lyon archives, Gabal strongholds, and Arthur/Umbral endgame spaces.
