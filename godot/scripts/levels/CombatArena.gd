extends Node2D

const PLAYER_START := Vector2(100, 200)
const ENEMY_START := Vector2(1719, 200)
const MENU_MUSIC := "res://assets/legacy_misty/audio/menu.wav"
const INGAME_MUSIC := "res://assets/legacy_misty/audio/ingame.wav"

@onready var player: Node = $Player
@onready var enemy: Node = $RoninEnemy
@onready var music_player: AudioStreamPlayer = $MusicPlayer

var current_music_path := ""

func _ready() -> void:
    player.damaged.connect(_on_player_damaged)
    enemy.damaged.connect(_on_enemy_damaged)
    player.died.connect(func(): GameManager.finish_fight(false))
    enemy.died.connect(func(): GameManager.finish_fight(true))
    GameManager.mode_changed.connect(_on_mode_changed)
    GameManager.game_over.connect(func(_victory: bool): queue_redraw())
    GameManager.audio_changed.connect(_on_audio_changed)
    _place_actors()
    GameManager.player_max_health = player.max_health
    GameManager.enemy_max_health = enemy.max_health
    GameManager.player_health = player.health
    GameManager.enemy_health = enemy.health
    GameManager.show_title()
    _sync_music_to_mode()

func _place_actors() -> void:
    player.reset_actor(PLAYER_START)
    enemy.reset_actor(ENEMY_START)
    enemy.target = player
    queue_redraw()

func start_from_title() -> void:
    reset_fight(true)

func return_to_title() -> void:
    _place_actors()
    GameManager.show_title()

func reset_fight(start_playing: bool = true) -> void:
    _place_actors()
    GameManager.player_max_health = player.max_health
    GameManager.enemy_max_health = enemy.max_health
    if start_playing:
        GameManager.reset_arena()
    else:
        GameManager.show_title()
    queue_redraw()

func _on_mode_changed(_mode: int) -> void:
    _sync_music_to_mode()
    queue_redraw()

func _on_audio_changed() -> void:
    _sync_music_to_mode()

func _sync_music_to_mode() -> void:
    if DisplayServer.get_name() == "headless" or music_player == null:
        return
    if GameManager.music_muted:
        music_player.stop()
        return
    music_player.volume_db = -80.0 if GameManager.master_volume <= 0.001 else linear_to_db(GameManager.master_volume)
    if GameManager.mode == GameManager.GameMode.TITLE:
        _play_music(MENU_MUSIC)
    elif GameManager.mode == GameManager.GameMode.ARENA or GameManager.mode == GameManager.GameMode.PAUSED:
        _play_music(INGAME_MUSIC)
    else:
        music_player.stop()
        current_music_path = ""

func _play_music(path: String) -> void:
    if current_music_path != path or music_player.stream == null:
        var stream: AudioStream = load(path)
        if stream == null:
            push_warning("Could not load legacy Misty music: " + path)
            return
        if stream is AudioStreamWAV:
            stream.loop_mode = AudioStreamWAV.LOOP_FORWARD
        music_player.stream = stream
        current_music_path = path
        music_player.play()
    elif not music_player.playing:
        music_player.play()

func _exit_tree() -> void:
    if music_player:
        music_player.stop()
        music_player.stream = null

func _unhandled_input(event: InputEvent) -> void:
    if event is InputEventKey and event.pressed and not event.echo and event.keycode == KEY_F11:
        var mode := DisplayServer.window_get_mode()
        DisplayServer.window_set_mode(DisplayServer.WINDOW_MODE_WINDOWED if mode == DisplayServer.WINDOW_MODE_FULLSCREEN else DisplayServer.WINDOW_MODE_FULLSCREEN)
        get_viewport().set_input_as_handled()
        return
    if event.is_action_pressed("restart"):
        reset_fight(true)
        get_viewport().set_input_as_handled()
    elif event.is_action_pressed("pause"):
        if GameManager.mode == GameManager.GameMode.ARENA:
            GameManager.set_paused(true)
        elif GameManager.mode == GameManager.GameMode.PAUSED:
            GameManager.set_paused(false)
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
        draw_rect(Rect2(Vector2.ZERO, Vector2(1920, 1120)), Color(0, 0, 0, 0.25))
