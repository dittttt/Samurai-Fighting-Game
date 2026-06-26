# Misty-to-Godot Parity Plan

The current priority is **not** expanding the metroidvania yet. First, the Godot port must preserve the original Misty Java 1v1 game's feel, screens, controls, UI assets, music behavior, combat quirks, and constants.

## Original Java files checked

Source files checked from the original `Misty` project:

- `Misty/src/main/Game.java`
- `Misty/src/main/GamePanel.java`
- `Misty/src/main/GameWindow.java`
- `Misty/src/inputs/KeyboardInputs.java`
- `Misty/src/inputs/MouseInputs.java`
- `Misty/src/gamestates/Gamestate.java`
- `Misty/src/gamestates/Menu.java`
- `Misty/src/gamestates/Playing.java`
- `Misty/src/entities/Character.java`
- `Misty/src/entities/Player.java`
- `Misty/src/entities/Enemy.java`
- `Misty/src/levels/Level.java`
- `Misty/src/levels/LevelManager.java`
- `Misty/src/ui/MenuButton.java`
- `Misty/src/ui/PauseOverlay.java`
- `Misty/src/ui/SoundButton.java`
- `Misty/src/ui/UrmButton.java`
- `Misty/src/ui/VolumeButton.java`
- `Misty/src/ui/GameOverOverlay.java`
- `Misty/src/utilz/Constants.java`
- `Misty/src/utilz/HelpMethods.java`
- `Misty/src/utilz/LoadSave.java`

Original resources checked:

- `background3.gif`, `menu_background_red.png`, `button_atlas.png`
- `pause_menu.png`, `sound_button.png`, `urm_buttons.png`, `volume_buttons.png`
- `samurai_atlas.png`, `ronin_atlas.png`
- `Japanese_Vilage_UPDATED.png`, `level_one_data.png`, `world01.txt`
- `menu.wav`, `ingame.wav`

## Render/world baseline

| Value | Java source | Godot target |
| --- | ---: | ---: |
| UPS/FPS | `200 UPS`, `120 FPS` | Godot physics plus converted timings |
| Tile source size | `32` | `32` |
| Scale | `5.0` | `5.0` |
| Menu scale | `2.0` | `2.0` |
| Rendered tile size | `160` | `160` |
| Tiles wide | `12` | `12` |
| Tiles high | `7` | `7` |
| Game width | `1920` | `1920` |
| Game height | `1120` | `1120` |
| Player start | `(100, 200)` | `(100, 200)` |
| Enemy start | `(1719, 200)` | `(1719, 200)` |

## Original screen/state flow

| Feature | Java behavior | Godot status |
| --- | --- | --- |
| Starts on main menu | `Gamestate.state = MENU` | Restored: Godot starts in `TITLE` |
| Menu background | Draws `background3.gif` scaled full-screen | Restored as static first-frame PNG because Godot does not natively play the Java GIF as a texture |
| Menu box | Draws `menu_background_red.png`, scaled 2x, centered | Restored |
| Menu buttons | `button_atlas.png`, rows play/options/quit, cols normal/hover/pressed | Restored via Godot `TextureButton` atlas regions |
| Enter key starts | `Menu.keyPressed(ENTER)` starts playing | TODO: add Enter shortcut to title menu |
| Options button | Original enters `OPTIONS`, then `Game.update` default exits | Godot keeps Options as safe no-op for now instead of quitting unexpectedly |
| Quit button | exits app | Restored |
| ESC pause | In `Playing`, ESC toggles `paused` | Restored |
| F11 fullscreen | Toggles fullscreen | Restored in `CombatArena._unhandled_input` |
| Pause panel | `pause_menu.png`, scaled 2x at y=`75*2` | Restored |
| Music/sfx buttons | `sound_button.png`, row 0 unmuted, row 1 muted, hover/pressed columns | Restored |
| URM buttons | `urm_buttons.png`: unpause/replay/menu | Restored visually; replay restarts fight in Godot, menu returns to title |
| Volume slider | `volume_buttons.png`, draggable/wheel visual slider | Restored mouse drag visual + audio volume; wheel TODO |
| Game over overlay | Dark overlay, `GAME OVER`, replay/menu URM buttons | Restored equivalent |

## Music behavior

| Feature | Java behavior | Godot status |
| --- | --- | --- |
| Menu music | Loads `audio/menu.wav`, loops continuously on menu | Restored |
| In-game music | Loads `audio/ingame.wav`, loops continuously in playing | Restored |
| Pause music | Game remains `PLAYING`, so ingame music continues while pause overlay is up | Restored |
| Game over music | Stops in-game music | Restored |
| Music mute | Stop/resume clips based on current state | Restored |
| SFX mute | Flag exists, but original had no real SFX implementation | Restored as flag/button only |

## Input parity

| Input | Java | Godot status |
| --- | --- | --- |
| Move | `A/D` | Restored plus arrows retained |
| Jump | `Space` | Restored plus W/Up retained |
| Run | `Shift` | Restored |
| Roll | `Ctrl` or `C` | Restored |
| Light attack | `J` and left mouse click | Restored |
| Heavy attack | `K` and right mouse click | Restored |
| Damage debug | `H`, 1 sec cooldown, player takes 20 | TODO, debug-only |
| Pause | `Esc` | Restored, plus P retained |
| Restart | Replay button only in Java game-over; Godot `R` retained as convenience | Extra, not original |

## Actor/attack sizes

| Value | Java | Godot target/status |
| --- | ---: | ---: |
| Sprite frame | `112x72` | `112x72` |
| Rendered sprite | `560x360` | `560x360` |
| X draw offset | right `44*5`, left `48*5` | Partially restored; Godot static sprite offset still needs left/right offset correction |
| Y draw offset | `36*5` | Partially restored |
| Actor hitbox | `20*5 x 30*5 = 100x150` | Restored |
| Attack hitbox | `40*5 x 20*5 = 200x100` | Restored |
| Light damage | `10` | Restored |
| Heavy damage | `20` | Restored |
| Invulnerability | `500ms` | TODO: Godot is still `300ms`, should change to `0.5s` |

## Movement conversion

Java updates at `200 UPS`, so per-update values convert to Godot px/sec:

| Value | Java per update | Godot px/sec target/status |
| --- | ---: | ---: |
| Walk | `0.6 * 5 = 3.0` | `600` restored |
| Run | `0.85 * 5 = 4.25` | `850` restored |
| Jump | `-2.25 * 5 = -11.25` | `-2250` restored |
| Gravity | `0.04 * 5 = 0.2/update²` | `8000 px/sec²` restored |
| Fall collision speed | `1.0 * 5 = 5` | TODO cap/ceiling behavior |
| Attack while moving | Java still calls movement while light/heavy animation overrides visual row | Restored: Godot no longer zeros movement on attack |
| Walking/running actor collision | Original had no actor-body pass-through | Restored requested behavior: actors block while walking/running |
| Roll/dash pass-through | User-requested Godot improvement | Kept: roll removes actor collision temporarily |

## Animation timing conversion

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

## Combat behavior

| Feature | Java behavior | Godot status |
| --- | --- | --- |
| Light hit frame | frame `4` of `7` | Restored as `0.40s` startup |
| Heavy hit frame | frame `5` of `11` | Restored as `0.525s` startup |
| One hit per attack | `attackProcessed` blocks repeat damage | Restored by `hit_targets`/active window |
| Light into heavy cancel | Original `setHeavyAttack` only starts if `!isAttacking()` | Matched: no true light-heavy cancel found in original files |
| Fake/cancel while attacking | No explicit cancel/fake system found in original files | Not implemented unless user wants it as new feature |
| Attack while moving | Original movement still updates during attack | Restored |
| Hit animation | `hit=true`, row 5, `aniSpeed=12`, resets tick | Restored with persistent `hit_reacting` |
| Death animation | row 6, `aniSpeed=12` | Partially restored; game-over timing still needs visual review |
| Roll animation | row 4, `aniSpeed=20`, clears after 6 frames | Partially restored; current dash distance/duration still needs exact feel tuning |

## Enemy AI inventory

| Feature | Java behavior | Godot status |
| --- | --- | --- |
| Decision interval | `500ms` | TODO |
| Idle chase start | player distance `<300` | TODO/full state machine not restored |
| Patrol chance | 3-5% idle patrol | TODO |
| Patrol speed/range | `0.3*5`, range `100` | TODO |
| Chase keep range | `<400` keep chasing | Partially restored as detection range |
| Attack range | `50`, attacks at `<=60`, leaves at `>75` | Partially restored |
| Heavy attack choice | heavy if distance `<35` | TODO |
| Attack cooldown | initial `1500ms`, then random `800-2000ms` | Partially restored; random cooldown TODO |
| Defensive roll | 80% dodge if player attacking within 150 and cooldown 3000ms | TODO |
| Enemy hit state/recovering | hit -> recovering -> chase/idle after 500ms | TODO |

## Implementation order from here

1. Preserve the restored commit `183afb8` hit/parity timing fix.
2. Restore old main menu, pause overlay, music/sfx buttons, volume slider, game-over overlay, title/start flow, ESC, F11, and mouse attacks. **Implemented in current parity pass.**
3. Restore original attack-while-moving behavior. **Implemented in current parity pass.**
4. Next: tune roll to original animation duration/feel while preserving requested pass-through dash behavior.
5. Next: restore exact left/right sprite draw offsets and per-character health bars above the sprites.
6. Next: restore enemy AI state machine completely: decision interval, patrol, defensive roll, heavy attack distance, randomized cooldowns, hit/recovery state.
7. Next: restore debug key `H` only if wanted; it existed in original but is not normal player-facing gameplay.
8. Verify each parity slice with headless Godot smoke tests plus focused ad-hoc checks; visual feel still needs manual play in Godot.
