extends Node

signal mode_changed(mode: int)
signal game_over(victory: bool)
signal player_health_changed(current: int, maximum: int)
signal enemy_health_changed(current: int, maximum: int)
signal audio_changed

enum GameMode { TITLE, ARENA, PAUSED, GAME_OVER }

const BASE_RESOLUTION := Vector2i(1920, 1120)

var mode: GameMode = GameMode.TITLE
var current_chapter := "prototype_arena"
var player_max_health := 100
var player_health := 100
var enemy_max_health := 100
var enemy_health := 100
var last_victory := false
var music_muted := false
var sfx_muted := false
var master_volume := 1.0

func _ready() -> void:
    _ensure_input_actions()

func _ensure_input_actions() -> void:
    _ensure_key_action("move_left", [KEY_A, KEY_LEFT])
    _ensure_key_action("move_right", [KEY_D, KEY_RIGHT])
    _ensure_key_action("jump", [KEY_SPACE, KEY_W, KEY_UP])
    _ensure_key_action("run", [KEY_SHIFT])
    _ensure_key_action("roll", [KEY_C, KEY_CTRL])
    _ensure_key_action("light_attack", [KEY_J])
    _ensure_key_action("heavy_attack", [KEY_K])
    _ensure_key_action("restart", [KEY_R])
    _ensure_key_action("pause", [KEY_P, KEY_ESCAPE])
    _ensure_mouse_action("light_attack", [MOUSE_BUTTON_LEFT])
    _ensure_mouse_action("heavy_attack", [MOUSE_BUTTON_RIGHT])

func _ensure_key_action(action_name: StringName, keys: Array[int]) -> void:
    if not InputMap.has_action(action_name):
        InputMap.add_action(action_name)
    for key in keys:
        var exists := false
        for event in InputMap.action_get_events(action_name):
            if event is InputEventKey and event.keycode == key:
                exists = true
                break
        if not exists:
            var event := InputEventKey.new()
            event.keycode = key
            InputMap.action_add_event(action_name, event)

func _ensure_mouse_action(action_name: StringName, buttons: Array[int]) -> void:
    if not InputMap.has_action(action_name):
        InputMap.add_action(action_name)
    for button in buttons:
        var exists := false
        for event in InputMap.action_get_events(action_name):
            if event is InputEventMouseButton and event.button_index == button:
                exists = true
                break
        if not exists:
            var event := InputEventMouseButton.new()
            event.button_index = button
            InputMap.action_add_event(action_name, event)

func show_title() -> void:
    mode = GameMode.TITLE
    last_victory = false
    mode_changed.emit(mode)

func reset_arena() -> void:
    mode = GameMode.ARENA
    last_victory = false
    player_health = player_max_health
    enemy_health = enemy_max_health
    mode_changed.emit(mode)
    player_health_changed.emit(player_health, player_max_health)
    enemy_health_changed.emit(enemy_health, enemy_max_health)

func set_paused(value: bool) -> void:
    if mode == GameMode.GAME_OVER:
        return
    mode = GameMode.PAUSED if value else GameMode.ARENA
    mode_changed.emit(mode)

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
    last_victory = victory
    mode = GameMode.GAME_OVER
    mode_changed.emit(mode)
    game_over.emit(victory)

func is_playing() -> bool:
    return mode == GameMode.ARENA

func toggle_music_mute() -> void:
    music_muted = not music_muted
    audio_changed.emit()

func toggle_sfx_mute() -> void:
    sfx_muted = not sfx_muted
    audio_changed.emit()

func set_master_volume(value: float) -> void:
    master_volume = clampf(value, 0.0, 1.0)
    audio_changed.emit()

func change_scene(path: String) -> void:
    get_tree().change_scene_to_file(path)
