class_name CombatActor
extends CharacterBody2D

const LegacyAtlasAnimator := preload("res://scripts/visuals/LegacyAtlasAnimator.gd")

signal damaged(amount: int, source: Node)
signal died

@export var max_health := 100
@export var move_speed := 92.0
@export var run_speed := 140.0
@export var jump_velocity := -245.0
@export var gravity := 720.0
@export var invulnerability_seconds := 0.30
@export var body_color := Color(0.86, 0.84, 0.74, 1.0)
@export var accent_color := Color(0.35, 0.05, 0.09, 1.0)
@export var nameplate := "Fighter"

var health := 100
var facing := 1
var invulnerable := false
var dead := false
var atlas_animator: Node = null
var current_visual_action := 0

func _ready() -> void:
    health = max_health
    queue_redraw()

func setup_legacy_sprite(texture_path: String) -> void:
    var sprite := get_node_or_null("Sprite2D") as Sprite2D
    if sprite == null:
        sprite = Sprite2D.new()
        sprite.name = "Sprite2D"
        add_child(sprite)
    sprite.position = Vector2(0, -36)
    sprite.scale = Vector2(1.35, 1.35)
    atlas_animator = LegacyAtlasAnimator.new()
    add_child(atlas_animator)
    atlas_animator.setup(sprite, texture_path)

func set_visual_action(row: int, should_loop: bool = true, restart: bool = false) -> void:
    current_visual_action = row
    if atlas_animator != null:
        atlas_animator.set_action(row, should_loop, restart)

func update_visual(delta: float) -> void:
    if atlas_animator != null:
        atlas_animator.set_facing(facing)
        atlas_animator.update(delta)

func reset_actor(start_position: Vector2) -> void:
    global_position = start_position
    velocity = Vector2.ZERO
    health = max_health
    facing = 1
    invulnerable = false
    dead = false
    queue_redraw()

func apply_gravity(delta: float) -> void:
    if not is_on_floor():
        velocity.y += gravity * delta
    elif velocity.y > 0:
        velocity.y = 0

func face_direction(direction: float) -> void:
    if is_zero_approx(direction):
        return
    facing = 1 if direction > 0 else -1
    queue_redraw()

func take_damage(amount: int, source: Node = null, knockback: Vector2 = Vector2.ZERO) -> void:
    if dead or invulnerable:
        return
    health = maxi(0, health - amount)
    damaged.emit(amount, source)
    if knockback != Vector2.ZERO:
        var push_direction: float = signf(global_position.x - source.global_position.x) if source is Node2D else float(-facing)
        if push_direction == 0:
            push_direction = -facing
        velocity = Vector2(absf(knockback.x) * push_direction, knockback.y)
    if health <= 0:
        dead = true
        invulnerable = false
        died.emit()
    else:
        _begin_invulnerability()
    queue_redraw()

func _begin_invulnerability() -> void:
    invulnerable = true
    await get_tree().create_timer(invulnerability_seconds).timeout
    invulnerable = false
    queue_redraw()

func _process(_delta: float) -> void:
    queue_redraw()

func _draw() -> void:
    if atlas_animator != null:
        return
    var alpha: float = 0.45 if invulnerable and Engine.get_physics_frames() % 8 < 4 else 1.0
    var body: Color = body_color
    body.a = alpha
    var accent: Color = accent_color
    accent.a = alpha

    # pixel-art placeholder body; replace with Aseprite sprites later.
    draw_rect(Rect2(Vector2(-7, -28), Vector2(14, 28)), body)
    draw_rect(Rect2(Vector2(-5, -38), Vector2(10, 10)), body.lightened(0.15))
    draw_rect(Rect2(Vector2(-8, -11), Vector2(16, 5)), accent)
    draw_rect(Rect2(Vector2(-5, 0), Vector2(4, 8)), body.darkened(0.25))
    draw_rect(Rect2(Vector2(1, 0), Vector2(4, 8)), body.darkened(0.25))

    # face marker and sword direction.
    draw_rect(Rect2(Vector2(2 * facing, -35), Vector2(2 * facing, 2)), Color.BLACK)
    var sword_start: Vector2 = Vector2(8 * facing, -19)
    var sword_end: Vector2 = Vector2(31 * facing, -25)
    draw_line(sword_start, sword_end, Color(0.91, 0.88, 0.73), 2.0)
    draw_line(Vector2(7 * facing, -18), Vector2(13 * facing, -15), accent, 2.0)

    if dead:
        draw_line(Vector2(-13, -4), Vector2(13, -4), Color(0.5, 0.02, 0.04), 3.0)
