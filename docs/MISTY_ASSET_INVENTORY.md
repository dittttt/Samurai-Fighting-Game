# Misty Java Prototype Asset Inventory

The old Java/Swing 1v1 game was **not deleted**. It still lives under `Misty/`, and the preservation branch is `1v1`. The Godot port now copies the original assets into `godot/assets/legacy_misty/` and uses the samurai/ronin atlases in the playable prototype.

## Original source folders

```text
Misty/src/     Java gameplay code
Misty/res/     Original images, sprite atlases, tile data, UI art
Misty/audio/   Original menu and gameplay music
```

## Copied Godot legacy-asset folder

```text
godot/assets/legacy_misty/res/
godot/assets/legacy_misty/audio/
godot/data/legacy_misty_assets.json
```

## Key animation atlases

Both character atlases are `1344x648`, split as `12` columns by `9` rows. Each frame is `112x72`. Java row mapping from `Constants.PlayerConstants`:

| Row | Java action | Frame count | Godot use now |
| --- | --- | ---: | --- |
| 0 | IDLE | 3 | idle |
| 1 | RUNNING | 10 | run/chase |
| 2 | WALKING | 4 | reserved |
| 3 | JUMP/FALLING | 4 | airborne |
| 4 | ROLL | 6 | dodge/roll |
| 5 | HIT | 6 | future hit-stun |
| 6 | DEATH | 11 | death |
| 7 | LIGHT_ATTACK | 7 | light attack |
| 8 | HEAVY_ATTACK | 11 | heavy attack / future heavy variants |

## Level/UI/audio assets

- `Japanese_Vilage_UPDATED.png`: 12x7 tile atlas, 32x32 tiles.
- `level_one_data.png`: old 12x7 tile-index map.
- `button_atlas.png`, `pause_menu.png`, `health_power_bar.png`, etc.: original Java UI art.
- `menu.mp3/.wav`, `ingame.mp3/.wav`: original music.

## Metroidvania direction

The old Java game is the combat/asset seed, not the final scope. WAGO now needs a metroidvania structure: interconnected Froy regions, ability-gated routes, Elk/Pahica/Lyon progression, Nox-Dong truth mechanic, Umbral Folk encounters, Bhunt liberation ability, bosses, NPC allies, story flags, save rooms, map UI, inventory/relics, and chapter gates.
