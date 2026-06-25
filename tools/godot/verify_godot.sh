#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
GODOT="${GODOT_PATH:-}"

if [[ -z "$GODOT" ]]; then
  for candidate in \
    "C:/Softwares/Godot_v4.7-stable_win64_console.exe" \
    "C:/Softwares/Godot_v4.7-stable_win64.exe" \
    "C:/Softwares/Godot.exe"; do
    if [[ -f "$candidate" ]]; then
      GODOT="$candidate"
      break
    fi
  done
fi

if [[ -z "$GODOT" || ! -f "$GODOT" ]]; then
  echo "Godot not found. Set GODOT_PATH=/path/to/Godot_console.exe" >&2
  exit 1
fi

echo "Godot: $GODOT"
"$GODOT" --version
"$GODOT" --headless --path "$ROOT/godot" --import --quit
"$GODOT" --headless --path "$ROOT/godot" --quit
"$GODOT" --headless --path "$ROOT/godot" --quit-after 1
"$GODOT" --headless --path "$ROOT/godot" --script tests/playable_smoke.gd
