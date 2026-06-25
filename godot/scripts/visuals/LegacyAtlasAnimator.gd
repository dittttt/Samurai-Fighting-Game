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

var sprite: Sprite2D
var action_row := 0
var frame_index := 0
var frame_tick := 0.0
var frame_seconds := 0.075
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
    if frame_tick < frame_seconds:
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
