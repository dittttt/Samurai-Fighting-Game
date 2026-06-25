extends Node2D

const TILE_SIZE := 32
const MAP_OFFSET := Vector2(48, 46)

var atlas: Texture2D
var level_image: Image
var level_data: Array[int] = []
var map_width := 0
var map_height := 0

func _ready() -> void:
    atlas = load("res://assets/legacy_misty/res/Japanese_Vilage_UPDATED.png")
    var level_texture: Texture2D = load("res://assets/legacy_misty/res/level_one_data.png")
    if level_texture:
        level_image = level_texture.get_image()
    if level_image:
        map_width = level_image.get_width()
        map_height = level_image.get_height()
        for y in range(map_height):
            for x in range(map_width):
                var value: int = level_image.get_pixel(x, y).r8
                if value >= 84:
                    value = 0
                level_data.append(value)
    queue_redraw()

func _draw() -> void:
    if atlas == null or level_data.is_empty():
        return
    for y in range(map_height):
        for x in range(map_width):
            var value: int = level_data[y * map_width + x]
            var src := Rect2((value % 12) * TILE_SIZE, int(value / 12) * TILE_SIZE, TILE_SIZE, TILE_SIZE)
            var dst := Rect2(MAP_OFFSET.x + x * TILE_SIZE, MAP_OFFSET.y + y * TILE_SIZE, TILE_SIZE, TILE_SIZE)
            draw_texture_rect_region(atlas, dst, src)
