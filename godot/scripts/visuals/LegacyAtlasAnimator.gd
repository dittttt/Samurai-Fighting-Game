extends Node

const FRAME_SIZE := Vector2i(112, 72)
const FRAME_COUNTS := {
    0: 3,  # idle
    1: 10, # running
    2: 4,  # walking
    3: 4,  # jump/fall
    4: 6,  # roll
    5: 6,  # hit
    6: 11, # death
    7: 7,  # light attack
    8: 11, # heavy attack
}
const FRAME_SECONDS := {
    0: 0.150, # idle: Java aniSpeed 30 at 200 UPS
    1: 0.100, # running: Java aniSpeed 20
    2: 0.150, # walking: Java aniSpeed 30
    3: 0.150, # jump/fall: Java default aniSpeed 30
    4: 0.100, # roll: Java aniSpeed 20
    5: 0.060, # hit: Java aniSpeed 12
    6: 0.060, # death: Java aniSpeed 12
    7: 0.100, # light attack: Java aniSpeed 20
    8: 0.105, # heavy attack: Java aniSpeed 21
}

var sprite: Sprite2D
var action_row := 0
var frame_index := 0
var frame_tick := 0.0
var loop := true

func setup(target_sprite: Sprite2D, texture_path: String) -> void:
    sprite = target_sprite
    sprite.texture = load(texture_path)
    if sprite.texture == null:
        push_error("Could not load legacy atlas: " + texture_path)
        return
    sprite.region_enabled = true
    sprite.centered = true
    _apply_region()

func set_action(row: int, should_loop: bool = true, restart: bool = false) -> void:
    if row != action_row or restart:
        action_row = row
        frame_index = 0
        frame_tick = 0.0
        loop = should_loop
        _apply_region()

func set_facing(facing: int) -> void:
    if sprite:
        sprite.flip_h = facing < 0

func update(delta: float) -> void:
    if sprite == null:
        return
    frame_tick += delta
    var seconds_per_frame: float = float(FRAME_SECONDS.get(action_row, 0.150))
    if frame_tick < seconds_per_frame:
        return
    frame_tick = 0.0
    var max_frames: int = int(FRAME_COUNTS.get(action_row, 1))
    if frame_index < max_frames - 1:
        frame_index += 1
    elif loop:
        frame_index = 0
    _apply_region()

func _apply_region() -> void:
    if sprite == null:
        return
    sprite.region_rect = Rect2(frame_index * FRAME_SIZE.x, action_row * FRAME_SIZE.y, FRAME_SIZE.x, FRAME_SIZE.y)
