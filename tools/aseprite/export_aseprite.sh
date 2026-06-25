#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ASEPRITE="${ASEPRITE_PATH:-}"

if [[ -z "$ASEPRITE" ]]; then
  for candidate in     "E:/Steam/steamapps/common/Aseprite/Aseprite.exe"     "C:/Program Files/Aseprite/Aseprite.exe"     "C:/Program Files (x86)/Steam/steamapps/common/Aseprite/Aseprite.exe"; do
    if [[ -f "$candidate" ]]; then
      ASEPRITE="$candidate"
      break
    fi
  done
fi

if [[ -z "$ASEPRITE" || ! -f "$ASEPRITE" ]]; then
  echo "Aseprite not found. Set ASEPRITE_PATH=/path/to/Aseprite.exe" >&2
  exit 1
fi

SRC_DIR="$ROOT/art/aseprite"
OUT_DIR="$ROOT/godot/assets/sprites"
mkdir -p "$OUT_DIR"
shopt -s nullglob
files=("$SRC_DIR"/*.ase "$SRC_DIR"/*.aseprite)

if (( ${#files[@]} == 0 )); then
  echo "No .ase or .aseprite files found in $SRC_DIR"
  echo "Aseprite path OK: $ASEPRITE"
  exit 0
fi

for src in "${files[@]}"; do
  base="$(basename "$src")"
  name="${base%.*}"
  echo "Exporting $base -> godot/assets/sprites/${name}.png + .json"
  "$ASEPRITE" -b "$src"     --sheet "$OUT_DIR/${name}.png"     --data "$OUT_DIR/${name}.json"     --format json-array     --list-tags
 done
