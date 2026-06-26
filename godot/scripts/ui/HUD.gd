extends CanvasLayer

const SCREEN_SIZE := Vector2(1920, 1120)
const MENU_SCALE := 2.0
const MENU_BUTTON_DEFAULT := Vector2(140, 56)
const MENU_BUTTON_SIZE := Vector2(280, 112)
const SOUND_DEFAULT_SIZE := 42
const SOUND_SIZE := 84
const URM_DEFAULT_SIZE := 56
const URM_SIZE := 112
const VOLUME_DEFAULT_WIDTH := 28
const VOLUME_DEFAULT_HEIGHT := 44
const SLIDER_DEFAULT_WIDTH := 215
const VOLUME_WIDTH := 56
const VOLUME_HEIGHT := 88
const SLIDER_WIDTH := 430

@onready var player_bar: ProgressBar = $MarginContainer/VBoxContainer/PlayerHealth
@onready var enemy_bar: ProgressBar = $MarginContainer/VBoxContainer/EnemyHealth
@onready var result_label: Label = $MarginContainer/VBoxContainer/ResultLabel
@onready var controls_label: Label = $MarginContainer/VBoxContainer/ControlsLabel
@onready var story_label: Label = $MarginContainer/VBoxContainer/StoryLabel
@onready var hud_margin: MarginContainer = $MarginContainer

var title_layer: Control
var pause_layer: Control
var game_over_layer: Control
var music_button: TextureButton
var sfx_button: TextureButton
var volume_slider: TextureRect
var volume_knob: TextureButton
var volume_dragging := false

func _ready() -> void:
    GameManager.player_health_changed.connect(_on_player_health_changed)
    GameManager.enemy_health_changed.connect(_on_enemy_health_changed)
    GameManager.game_over.connect(_on_game_over)
    GameManager.mode_changed.connect(_on_mode_changed)
    GameManager.audio_changed.connect(_on_audio_changed)
    controls_label.text = "A/D move | Space jump | Shift run | C/Ctrl roll | J/Left click light | K/Right click heavy | Esc pause"
    story_label.text = "Misty parity port: original menu, controls, music, assets, values first."
    result_label.text = ""
    _build_legacy_layers()
    _on_player_health_changed(GameManager.player_health, GameManager.player_max_health)
    _on_enemy_health_changed(GameManager.enemy_health, GameManager.enemy_max_health)
    _on_mode_changed(GameManager.mode)

func _build_legacy_layers() -> void:
    title_layer = _make_layer("TitleMenu")
    add_child(title_layer)
    _build_title_menu(title_layer)

    pause_layer = _make_layer("PauseMenu")
    add_child(pause_layer)
    _build_pause_menu(pause_layer)

    game_over_layer = _make_layer("GameOver")
    add_child(game_over_layer)
    _build_game_over(game_over_layer)

func _make_layer(layer_name: String) -> Control:
    var layer := Control.new()
    layer.name = layer_name
    layer.set_anchors_preset(Control.PRESET_FULL_RECT)
    layer.mouse_filter = Control.MOUSE_FILTER_STOP
    layer.visible = false
    return layer

func _build_title_menu(layer: Control) -> void:
    var bg := TextureRect.new()
    bg.name = "JavaGifFirstFrameBackground"
    bg.set_anchors_preset(Control.PRESET_FULL_RECT)
    bg.texture = load("res://assets/legacy_misty/res/background3_first.png")
    bg.stretch_mode = TextureRect.STRETCH_SCALE
    layer.add_child(bg)

    var menu_box := TextureRect.new()
    menu_box.name = "MenuBackgroundRed"
    menu_box.texture = load("res://assets/legacy_misty/res/menu_background_red.png")
    menu_box.position = Vector2((SCREEN_SIZE.x - 282.0 * MENU_SCALE) * 0.5, (SCREEN_SIZE.y - 336.0 * MENU_SCALE) * 0.5)
    menu_box.size = Vector2(282, 336) * MENU_SCALE
    menu_box.stretch_mode = TextureRect.STRETCH_SCALE
    layer.add_child(menu_box)

    layer.add_child(_make_menu_button(0, Vector2(SCREEN_SIZE.x * 0.5 - MENU_BUTTON_SIZE.x * 0.5, 215 * MENU_SCALE), Callable(self, "_on_play_pressed")))
    layer.add_child(_make_menu_button(1, Vector2(SCREEN_SIZE.x * 0.5 - MENU_BUTTON_SIZE.x * 0.5, 285 * MENU_SCALE), Callable(self, "_on_options_pressed")))
    layer.add_child(_make_menu_button(2, Vector2(SCREEN_SIZE.x * 0.5 - MENU_BUTTON_SIZE.x * 0.5, 355 * MENU_SCALE), Callable(self, "_on_quit_pressed")))

func _build_pause_menu(layer: Control) -> void:
    var dark := ColorRect.new()
    dark.name = "DimBackground"
    dark.set_anchors_preset(Control.PRESET_FULL_RECT)
    dark.color = Color(0, 0, 0, 0.35)
    layer.add_child(dark)

    var panel := TextureRect.new()
    panel.name = "PauseMenuPanel"
    panel.texture = load("res://assets/legacy_misty/res/pause_menu.png")
    panel.size = Vector2(258, 389) * MENU_SCALE
    panel.position = Vector2(SCREEN_SIZE.x * 0.5 - panel.size.x * 0.5, 75 * MENU_SCALE)
    panel.stretch_mode = TextureRect.STRETCH_SCALE
    layer.add_child(panel)

    music_button = _make_sound_button(Vector2(515 * MENU_SCALE, 190 * MENU_SCALE), Callable(self, "_on_music_pressed"))
    sfx_button = _make_sound_button(Vector2(515 * MENU_SCALE, 236 * MENU_SCALE), Callable(self, "_on_sfx_pressed"))
    layer.add_child(music_button)
    layer.add_child(sfx_button)

    layer.add_child(_make_urm_button(2, Vector2(379 * MENU_SCALE, 375 * MENU_SCALE), Callable(self, "_on_pause_menu_pressed")))
    layer.add_child(_make_urm_button(1, Vector2(450 * MENU_SCALE, 375 * MENU_SCALE), Callable(self, "_on_replay_pressed")))
    layer.add_child(_make_urm_button(0, Vector2(521 * MENU_SCALE, 375 * MENU_SCALE), Callable(self, "_on_unpause_pressed")))
    _build_volume(layer)
    _refresh_sound_buttons()
    _refresh_volume_knob()

func _build_game_over(layer: Control) -> void:
    var dark := ColorRect.new()
    dark.name = "GameOverDim"
    dark.set_anchors_preset(Control.PRESET_FULL_RECT)
    dark.color = Color(0, 0, 0, 0.78)
    layer.add_child(dark)

    var label := Label.new()
    label.name = "GameOverLabel"
    label.text = "GAME OVER"
    label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
    label.add_theme_font_size_override("font_size", 80)
    label.add_theme_color_override("font_color", Color.WHITE)
    label.position = Vector2(0, SCREEN_SIZE.y / 3.0 - 80)
    label.size = Vector2(SCREEN_SIZE.x, 120)
    layer.add_child(label)

    layer.add_child(_make_urm_button(1, Vector2(SCREEN_SIZE.x * 0.5 - URM_SIZE - 40, SCREEN_SIZE.y / 1.7), Callable(self, "_on_replay_pressed")))
    layer.add_child(_make_urm_button(2, Vector2(SCREEN_SIZE.x * 0.5 + 40, SCREEN_SIZE.y / 1.7), Callable(self, "_on_pause_menu_pressed")))

func _build_volume(layer: Control) -> void:
    var tex: Texture2D = load("res://assets/legacy_misty/res/volume_buttons.png")
    volume_slider = TextureRect.new()
    volume_slider.name = "VolumeSlider"
    volume_slider.texture = _atlas(tex, Rect2(VOLUME_DEFAULT_WIDTH * 3, 0, SLIDER_DEFAULT_WIDTH, VOLUME_DEFAULT_HEIGHT))
    volume_slider.position = Vector2(372 * MENU_SCALE, 329 * MENU_SCALE)
    volume_slider.size = Vector2(SLIDER_WIDTH, VOLUME_HEIGHT)
    volume_slider.stretch_mode = TextureRect.STRETCH_SCALE
    volume_slider.mouse_filter = Control.MOUSE_FILTER_STOP
    volume_slider.gui_input.connect(_on_volume_gui_input)
    layer.add_child(volume_slider)

    volume_knob = TextureButton.new()
    volume_knob.name = "VolumeKnob"
    _set_volume_knob_textures()
    volume_knob.size = Vector2(VOLUME_WIDTH, VOLUME_HEIGHT)
    volume_knob.ignore_texture_size = true
    volume_knob.stretch_mode = TextureButton.STRETCH_SCALE
    volume_knob.gui_input.connect(_on_volume_gui_input)
    layer.add_child(volume_knob)

func _make_menu_button(row: int, pos: Vector2, callback: Callable) -> TextureButton:
    var tex: Texture2D = load("res://assets/legacy_misty/res/button_atlas.png")
    var button := TextureButton.new()
    button.name = ["PlayButton", "OptionsButton", "QuitButton"][row]
    button.position = pos
    button.size = MENU_BUTTON_SIZE
    button.ignore_texture_size = true
    button.stretch_mode = TextureButton.STRETCH_SCALE
    button.texture_normal = _atlas(tex, Rect2(0, row * MENU_BUTTON_DEFAULT.y, MENU_BUTTON_DEFAULT.x, MENU_BUTTON_DEFAULT.y))
    button.texture_hover = _atlas(tex, Rect2(MENU_BUTTON_DEFAULT.x, row * MENU_BUTTON_DEFAULT.y, MENU_BUTTON_DEFAULT.x, MENU_BUTTON_DEFAULT.y))
    button.texture_pressed = _atlas(tex, Rect2(MENU_BUTTON_DEFAULT.x * 2, row * MENU_BUTTON_DEFAULT.y, MENU_BUTTON_DEFAULT.x, MENU_BUTTON_DEFAULT.y))
    button.pressed.connect(callback)
    return button

func _make_sound_button(pos: Vector2, callback: Callable) -> TextureButton:
    var button := TextureButton.new()
    button.position = pos
    button.size = Vector2(SOUND_SIZE, SOUND_SIZE)
    button.ignore_texture_size = true
    button.stretch_mode = TextureButton.STRETCH_SCALE
    button.pressed.connect(callback)
    return button

func _make_urm_button(row: int, pos: Vector2, callback: Callable) -> TextureButton:
    var tex: Texture2D = load("res://assets/legacy_misty/res/urm_buttons.png")
    var button := TextureButton.new()
    button.position = pos
    button.size = Vector2(URM_SIZE, URM_SIZE)
    button.ignore_texture_size = true
    button.stretch_mode = TextureButton.STRETCH_SCALE
    button.texture_normal = _atlas(tex, Rect2(0, row * URM_DEFAULT_SIZE, URM_DEFAULT_SIZE, URM_DEFAULT_SIZE))
    button.texture_hover = _atlas(tex, Rect2(URM_DEFAULT_SIZE, row * URM_DEFAULT_SIZE, URM_DEFAULT_SIZE, URM_DEFAULT_SIZE))
    button.texture_pressed = _atlas(tex, Rect2(URM_DEFAULT_SIZE * 2, row * URM_DEFAULT_SIZE, URM_DEFAULT_SIZE, URM_DEFAULT_SIZE))
    button.pressed.connect(callback)
    return button

func _atlas(tex: Texture2D, region: Rect2) -> AtlasTexture:
    var atlas := AtlasTexture.new()
    atlas.atlas = tex
    atlas.region = region
    return atlas

func _refresh_sound_buttons() -> void:
    if music_button == null or sfx_button == null:
        return
    _set_sound_button_textures(music_button, GameManager.music_muted)
    _set_sound_button_textures(sfx_button, GameManager.sfx_muted)

func _set_sound_button_textures(button: TextureButton, muted: bool) -> void:
    var tex: Texture2D = load("res://assets/legacy_misty/res/sound_button.png")
    var row := 1 if muted else 0
    button.texture_normal = _atlas(tex, Rect2(0, row * SOUND_DEFAULT_SIZE, SOUND_DEFAULT_SIZE, SOUND_DEFAULT_SIZE))
    button.texture_hover = _atlas(tex, Rect2(SOUND_DEFAULT_SIZE, row * SOUND_DEFAULT_SIZE, SOUND_DEFAULT_SIZE, SOUND_DEFAULT_SIZE))
    button.texture_pressed = _atlas(tex, Rect2(SOUND_DEFAULT_SIZE * 2, row * SOUND_DEFAULT_SIZE, SOUND_DEFAULT_SIZE, SOUND_DEFAULT_SIZE))

func _set_volume_knob_textures() -> void:
    var tex: Texture2D = load("res://assets/legacy_misty/res/volume_buttons.png")
    volume_knob.texture_normal = _atlas(tex, Rect2(0, 0, VOLUME_DEFAULT_WIDTH, VOLUME_DEFAULT_HEIGHT))
    volume_knob.texture_hover = _atlas(tex, Rect2(VOLUME_DEFAULT_WIDTH, 0, VOLUME_DEFAULT_WIDTH, VOLUME_DEFAULT_HEIGHT))
    volume_knob.texture_pressed = _atlas(tex, Rect2(VOLUME_DEFAULT_WIDTH * 2, 0, VOLUME_DEFAULT_WIDTH, VOLUME_DEFAULT_HEIGHT))

func _refresh_volume_knob() -> void:
    if volume_knob == null or volume_slider == null:
        return
    var min_x: float = volume_slider.position.x + VOLUME_WIDTH * 0.5
    var max_x: float = volume_slider.position.x + SLIDER_WIDTH - VOLUME_WIDTH * 0.5
    var knob_center_x: float = lerpf(min_x, max_x, GameManager.master_volume)
    volume_knob.position = Vector2(knob_center_x - VOLUME_WIDTH * 0.5, volume_slider.position.y)

func _on_volume_gui_input(event: InputEvent) -> void:
    if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT:
        volume_dragging = event.pressed
        if event.pressed:
            _set_volume_from_mouse()
    elif event is InputEventMouseMotion and volume_dragging:
        _set_volume_from_mouse()

func _set_volume_from_mouse() -> void:
    var mouse_x: float = get_viewport().get_mouse_position().x
    var min_x: float = volume_slider.global_position.x + VOLUME_WIDTH * 0.5
    var max_x: float = volume_slider.global_position.x + SLIDER_WIDTH - VOLUME_WIDTH * 0.5
    var ratio: float = inverse_lerp(min_x, max_x, clampf(mouse_x, min_x, max_x))
    GameManager.set_master_volume(ratio)

func _on_play_pressed() -> void:
    var arena := get_parent()
    if arena != null and arena.has_method("start_from_title"):
        arena.start_from_title()

func _on_options_pressed() -> void:
    # Original Java OPTIONS falls through to the quit/default branch. Keep visible button behavior safe by treating it as a no-op for now.
    pass

func _on_quit_pressed() -> void:
    get_tree().quit()

func _on_music_pressed() -> void:
    GameManager.toggle_music_mute()

func _on_sfx_pressed() -> void:
    GameManager.toggle_sfx_mute()

func _on_pause_menu_pressed() -> void:
    var arena := get_parent()
    if arena != null and arena.has_method("return_to_title"):
        arena.return_to_title()

func _on_replay_pressed() -> void:
    var arena := get_parent()
    if arena != null and arena.has_method("reset_fight"):
        arena.reset_fight(true)

func _on_unpause_pressed() -> void:
    GameManager.set_paused(false)

func _on_player_health_changed(current: int, maximum: int) -> void:
    player_bar.max_value = maximum
    player_bar.value = current
    player_bar.tooltip_text = "Wago health: %d/%d" % [current, maximum]

func _on_enemy_health_changed(current: int, maximum: int) -> void:
    enemy_bar.max_value = maximum
    enemy_bar.value = current
    enemy_bar.tooltip_text = "Ronin health: %d/%d" % [current, maximum]

func _on_audio_changed() -> void:
    _refresh_sound_buttons()
    _refresh_volume_knob()

func _on_mode_changed(mode: int) -> void:
    var is_title := mode == GameManager.GameMode.TITLE
    var is_pause := mode == GameManager.GameMode.PAUSED
    var is_game_over := mode == GameManager.GameMode.GAME_OVER
    title_layer.visible = is_title
    pause_layer.visible = is_pause
    game_over_layer.visible = is_game_over
    hud_margin.visible = not is_title
    if mode == GameManager.GameMode.ARENA:
        result_label.text = ""
    elif is_pause:
        result_label.text = ""
    elif is_game_over:
        result_label.text = "VICTORY" if GameManager.last_victory else "DEFEAT"

func _on_game_over(victory: bool) -> void:
    result_label.text = "VICTORY" if victory else "DEFEAT"
