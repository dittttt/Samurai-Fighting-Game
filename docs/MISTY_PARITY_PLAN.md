# Misty-to-Godot Parity Plan

The current priority is **not** expanding the metroidvania yet. First, the Godot port must preserve the original Misty Java 1v1 game's feel and constants.

## Original Java values checked

Source files checked:

- `Misty/src/main/Game.java`
- `Misty/src/entities/Character.java`
- `Misty/src/entities/Player.java`
- `Misty/src/entities/Enemy.java`
- `Misty/src/utilz/Constants.java`
- `Misty/src/utilz/HelpMethods.java`
- `Misty/src/gamestates/Playing.java`

### Render/world baseline

| Value | Java source | Godot target |
| --- | ---: | ---: |
| Tile source size | `32` | `32` |
| Scale | `5.0` | `5.0` |
| Rendered tile size | `160` | `160` |
| Tiles wide | `12` | `12` |
| Tiles high | `7` | `7` |
| Game width | `1920` | `1920` |
| Game height | `1120` | `1120` |
| Player start | `(100, 200)` | `(100, 200)` |
| Enemy start | `(1719, 200)` | `(1719, 200)` |

### Actor/attack sizes

| Value | Java | Godot target |
| --- | ---: | ---: |
| Sprite frame | `112x72` | `112x72` |
| Rendered sprite | `560x360` | `560x360` |
| Actor hitbox | `20*5 x 30*5 = 100x150` | `100x150` |
| Attack hitbox | `40*5 x 20*5 = 200x100` | `200x100` |
| Light damage | `10` | `10` |
| Heavy damage | `20` | `20` |

### Movement conversion

Java updates at `200 UPS`, so per-update values convert to Godot px/sec:

| Value | Java per update | Godot px/sec target |
| --- | ---: | ---: |
| Walk | `0.6 * 5 = 3.0` | `600` |
| Run | `0.85 * 5 = 4.25` | `850` |
| Jump | `-2.25 * 5 = -11.25` | `-2250` |
| Gravity | `0.04 * 5 = 0.2/update²` | `8000 px/sec²` |
| Fall collision speed | `1.0 * 5 = 5` | `1000` if/when capped |

### Animation timing conversion

Java animation advances every `aniSpeed` updates at `200 UPS`:

| Action row | Java action | Frames | Java `aniSpeed` | Godot seconds/frame |
| ---: | --- | ---: | ---: | ---: |
| 0 | idle | 3 | 30 | `0.150` |
| 1 | running | 10 | 20 | `0.100` |
| 2 | walking | 4 | 30 | `0.150` |
| 3 | jump/fall | 4 | 30 | `0.150` |
| 4 | roll | 6 | 20 | `0.100` |
| 5 | hit | 6 | 12 | `0.060` |
| 6 | death | 11 | 12 | `0.060` |
| 7 | light attack | 7 | 20 | `0.100` |
| 8 | heavy attack | 11 | 21 | `0.105` |

### Attack hit timing

| Attack | Java hit frame | Target startup | Target active | Target recovery |
| --- | ---: | ---: | ---: | ---: |
| Light | frame `4` of `7` | `0.40s` | `0.10s` | `0.20s` |
| Heavy | frame `5` of `11` | `0.525s` | `0.10s` | `0.53s` |

### Enemy AI values

| Value | Java | Godot target now |
| --- | ---: | ---: |
| Attack range | `50` | `50-60` |
| Chase start | `300` | `300` |
| Chase keep | `400` | `400` |
| Heavy if closer than | `35` | future distinction |
| Initial attack cooldown | `1500ms` | `1.5s` |
| Later attack cooldown | `800-2000ms` | future random cooldown |
| Patrol speed | `0.3*5 = 1.5/update` | future patrol pass |

## Implementation order

1. Restore hit animation persistence: damage should set row `5` and not be immediately overwritten by idle/run/attack selection.
2. Make `LegacyAtlasAnimator` use Java action-specific frame timings instead of one hardcoded `0.075s` speed.
3. Make light/heavy attack timing match Java hit frames.
4. Pull enemy range/cooldown values back toward Java instead of the overly aggressive scaffold values.
5. Keep the useful new Godot-only feature requested by the user: roll/dash can pass through actors; walking/running cannot.
6. Verify with headless Godot smoke tests plus a focused port-baseline smoke test.
