extends Node2D

const PLAYER_START := Vector2(128, 210)
const ENEMY_START := Vector2(352, 210)

@onready var player: Node = $Player
@onready var enemy: Node = $RoninEnemy

var paused_overlay_alpha := 0.0

func _ready() -> void:
    player.damaged.connect(_on_player_damaged)
    enemy.damaged.connect(_on_enemy_damaged)
    player.died.connect(func(): GameManager.finish_fight(false))
    enemy.died.connect(func(): GameManager.finish_fight(true))
    GameManager.mode_changed.connect(func(_mode: int): queue_redraw())
    GameManager.game_over.connect(func(_victory: bool): queue_redraw())
    reset_fight()

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
    # Background: temporary pixel-art mood board until Aseprite art exists.
    draw_rect(Rect2(Vector2.ZERO, Vector2(480, 270)), Color(0.055, 0.047, 0.074))
    draw_circle(Vector2(390, 54), 20.0, Color(0.78, 0.70, 0.55, 0.9))
    draw_rect(Rect2(Vector2(0, 214), Vector2(480, 56)), Color(0.10, 0.09, 0.10))
    draw_rect(Rect2(Vector2(0, 235), Vector2(480, 35)), Color(0.18, 0.13, 0.11))
    draw_line(Vector2(0, 235), Vector2(480, 235), Color(0.54, 0.11, 0.12), 2.0)

    # distant silhouettes
    for i in range(8):
        var x: int = 22 + i * 62
        draw_rect(Rect2(Vector2(x, 184 - (i % 3) * 7), Vector2(8, 50)), Color(0.035, 0.032, 0.045))
        draw_polygon(PackedVector2Array([Vector2(x - 12, 190), Vector2(x + 4, 158), Vector2(x + 20, 190)]), PackedColorArray([Color(0.035, 0.032, 0.045)]))

    # Arena boundary hints.
    draw_line(Vector2(20, 235), Vector2(20, 170), Color(0.22, 0.08, 0.08), 2.0)
    draw_line(Vector2(460, 235), Vector2(460, 170), Color(0.22, 0.08, 0.08), 2.0)

    if GameManager.mode == GameManager.GameMode.PAUSED:
        draw_rect(Rect2(Vector2.ZERO, Vector2(480, 270)), Color(0, 0, 0, 0.45))
