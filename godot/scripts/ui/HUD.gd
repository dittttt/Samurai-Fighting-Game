extends CanvasLayer

@onready var player_bar: ProgressBar = $MarginContainer/VBoxContainer/PlayerHealth
@onready var enemy_bar: ProgressBar = $MarginContainer/VBoxContainer/EnemyHealth
@onready var result_label: Label = $MarginContainer/VBoxContainer/ResultLabel

func _ready() -> void:
    GameManager.player_health_changed.connect(_on_player_health_changed)
    GameManager.enemy_health_changed.connect(_on_enemy_health_changed)
    GameManager.game_over.connect(_on_game_over)
    result_label.text = ""

func _on_player_health_changed(current: int, maximum: int) -> void:
    player_bar.max_value = maximum
    player_bar.value = current

func _on_enemy_health_changed(current: int, maximum: int) -> void:
    enemy_bar.max_value = maximum
    enemy_bar.value = current

func _on_game_over(victory: bool) -> void:
    result_label.text = "VICTORY" if victory else "DEFEAT"
