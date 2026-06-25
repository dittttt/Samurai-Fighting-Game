# WAGO Godot Port

This folder is the Godot 4.x port foundation for WAGO. It is intentionally committed before Godot is installed so the project structure, scripts, Aseprite pipeline, and migration plan are ready.

## Open it

1. Install Godot 4.x.
2. Open Godot Project Manager.
3. Import `godot/project.godot`.
4. Run `scenes/main/Main.tscn`.

## Current goal

Rebuild the Java 1v1 prototype as a Godot vertical slice first:

- Wago player controller
- Ronin enemy AI
- light/heavy sword attacks
- hitbox/hurtbox combat
- health HUD
- victory/defeat state

Then expand into the story-driven WAGO game.


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
