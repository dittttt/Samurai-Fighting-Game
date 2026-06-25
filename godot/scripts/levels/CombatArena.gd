extends Node2D

const PLAYER_START := Vector2(100, 200)
const ENEMY_START := Vector2(1719, 200)

@onready var player: Node = $Player
@onready var enemy: Node = $RoninEnemy

var paused_overlay_alpha := 0.0

func _ready() -> void:
    _start_music()
    player.damaged.connect(_on_player_damaged)
    enemy.damaged.connect(_on_enemy_damaged)
    player.died.connect(func(): GameManager.finish_fight(false))
    enemy.died.connect(func(): GameManager.finish_fight(true))
    GameManager.mode_changed.connect(func(_mode: int): queue_redraw())
    GameManager.game_over.connect(func(_victory: bool): queue_redraw())
    reset_fight()

func _start_music() -> void:
    if DisplayServer.get_name() == "headless":
        return
    var music_player := get_node_or_null("MusicPlayer") as AudioStreamPlayer
    if music_player == null:
        return
    music_player.stream = load("res://assets/legacy_misty/audio/ingame.mp3")
    if music_player.stream:
        music_player.volume_db = -12.0
        music_player.play()

func _exit_tree() -> void:
    var music_player := get_node_or_null("MusicPlayer") as AudioStreamPlayer
    if music_player:
        music_player.stop()
        music_player.stream = null

func reset_fight() -> void:
    player.reset_actor(PLAYER_START)
    enemy.reset_actor(ENEMY_START)
    enemy.target = player
    GameManager.player_max_health = player.max_health
    GameManager.enemy_max_health = enemy.max_health
    GameManager.reset_arena()
    queue_redraw()

func _unhandled_input(event: InputEvent) -> void:
    if event.is_action_pressed("restart"):
        reset_fight()
        get_viewport().set_input_as_handled()
    elif event.is_action_pressed("pause"):
        GameManager.set_paused(GameManager.mode != GameManager.GameMode.PAUSED)
        get_viewport().set_input_as_handled()

func _process(_delta: float) -> void:
    queue_redraw()

func _on_player_damaged(_amount: int, _source: Node) -> void:
    GameManager.set_player_health(player.health)

func _on_enemy_damaged(_amount: int, _source: Node) -> void:
    GameManager.set_enemy_health(enemy.health)

func _draw() -> void:
    # Match the original Java panel size exactly: 12 x 7 tiles at 160 px each.
    draw_rect(Rect2(Vector2.ZERO, Vector2(1920, 1120)), Color(0.055, 0.047, 0.074))

    if GameManager.mode == GameManager.GameMode.PAUSED:
        draw_rect(Rect2(Vector2.ZERO, Vector2(1920, 1120)), Color(0, 0, 0, 0.45))
