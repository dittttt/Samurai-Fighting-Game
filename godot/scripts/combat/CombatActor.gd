class_name CombatActor
extends CharacterBody2D

signal damaged(amount: int, source: Node)
signal died

@export var max_health := 100
@export var move_speed := 92.0
@export var run_speed := 140.0
@export var jump_velocity := -245.0
@export var gravity := 720.0
@export var invulnerability_seconds := 0.35

var health := 100
var facing := 1
var invulnerable := false
var dead := false

func _ready() -> void:
    health = max_health

func apply_gravity(delta: float) -> void:
    if not is_on_floor():
        velocity.y += gravity * delta

func face_direction(direction: float) -> void:
    if direction == 0:
        return
    facing = 1 if direction > 0 else -1
    scale.x = abs(scale.x) * facing

func take_damage(amount: int, source: Node = null, knockback: Vector2 = Vector2.ZERO) -> void:
    if dead or invulnerable:
        return
    health = maxi(0, health - amount)
    damaged.emit(amount, source)
    if knockback != Vector2.ZERO:
        velocity = Vector2(knockback.x * -facing, knockback.y)
    if health <= 0:
        dead = true
        died.emit()
    else:
        _begin_invulnerability()

func _begin_invulnerability() -> void:
    invulnerable = true
    await get_tree().create_timer(invulnerability_seconds).timeout
    invulnerable = false
