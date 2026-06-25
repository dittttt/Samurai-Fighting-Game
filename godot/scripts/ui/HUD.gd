extends CanvasLayer

@onready var player_bar: ProgressBar = $MarginContainer/VBoxContainer/PlayerHealth
@onready var enemy_bar: ProgressBar = $MarginContainer/VBoxContainer/EnemyHealth
@onready var result_label: Label = $MarginContainer/VBoxContainer/ResultLabel
@onready var controls_label: Label = $MarginContainer/VBoxContainer/ControlsLabel
@onready var story_label: Label = $MarginContainer/VBoxContainer/StoryLabel

func _ready() -> void:
    GameManager.player_health_changed.connect(_on_player_health_changed)
    GameManager.enemy_health_changed.connect(_on_enemy_health_changed)
    GameManager.game_over.connect(_on_game_over)
    GameManager.mode_changed.connect(_on_mode_changed)
    controls_label.text = "Move: A/D or Arrows    Jump: Space/W/Up    Roll: C/Ctrl    Attack: J/K    Pause: P/Esc    Restart: R"
    story_label.text = "WAGO prototype: defeat the ronin. Placeholder art now; Aseprite sprites come next."
    result_label.text = ""

func _on_player_health_changed(current: int, maximum: int) -> void:
    player_bar.max_value = maximum
    player_bar.value = current
    player_bar.tooltip_text = "Wago health: %d/%d" % [current, maximum]

func _on_enemy_health_changed(current: int, maximum: int) -> void:
    enemy_bar.max_value = maximum
    enemy_bar.value = current
    enemy_bar.tooltip_text = "Ronin health: %d/%d" % [current, maximum]

func _on_mode_changed(mode: int) -> void:
    if mode == GameManager.GameMode.ARENA:
        result_label.text = ""
    elif mode == GameManager.GameMode.PAUSED:
        result_label.text = "PAUSED - Press P/Esc to resume, R to restart"

func _on_game_over(victory: bool) -> void:
    result_label.text = "VICTORY - Press R to fight again" if victory else "DEFEAT - Press R to retry"
