extends Node2D

@onready var player: Node = $Player
@onready var enemy: Node = $RoninEnemy

func _ready() -> void:
    GameManager.reset_arena()
    player.damaged.connect(_on_player_damaged)
    enemy.damaged.connect(_on_enemy_damaged)
    player.died.connect(func(): GameManager.finish_fight(false))
    enemy.died.connect(func(): GameManager.finish_fight(true))

func _on_player_damaged(_amount: int, _source: Node) -> void:
    GameManager.set_player_health(player.health)

func _on_enemy_damaged(_amount: int, _source: Node) -> void:
    GameManager.set_enemy_health(enemy.health)
