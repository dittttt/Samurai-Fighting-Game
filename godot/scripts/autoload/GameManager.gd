extends Node

signal game_over(victory: bool)
signal player_health_changed(current: int, maximum: int)
signal enemy_health_changed(current: int, maximum: int)

enum GameMode { TITLE, ARENA, STORY, PAUSED, GAME_OVER }

const BASE_RESOLUTION := Vector2i(480, 270)

var mode: GameMode = GameMode.TITLE
var current_chapter := "prototype_arena"
var player_max_health := 100
var player_health := 100
var enemy_max_health := 100
var enemy_health := 100

func reset_arena() -> void:
    mode = GameMode.ARENA
    player_health = player_max_health
    enemy_health = enemy_max_health
    player_health_changed.emit(player_health, player_max_health)
    enemy_health_changed.emit(enemy_health, enemy_max_health)

func set_player_health(value: int) -> void:
    player_health = clampi(value, 0, player_max_health)
    player_health_changed.emit(player_health, player_max_health)
    if player_health <= 0:
        finish_fight(false)

func set_enemy_health(value: int) -> void:
    enemy_health = clampi(value, 0, enemy_max_health)
    enemy_health_changed.emit(enemy_health, enemy_max_health)
    if enemy_health <= 0:
        finish_fight(true)

func finish_fight(victory: bool) -> void:
    if mode == GameMode.GAME_OVER:
        return
    mode = GameMode.GAME_OVER
    game_over.emit(victory)

func change_scene(path: String) -> void:
    get_tree().change_scene_to_file(path)
