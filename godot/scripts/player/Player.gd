extends "res://scripts/combat/CombatActor.gd"

const AttackDataScript := preload("res://scripts/combat/AttackData.gd")

@export var light_attack: Resource
@export var heavy_attack: Resource
@export var roll_speed := 210.0
@export var roll_seconds := 0.22

@onready var hitbox: Area2D = $Hitbox

var attacking := false
var rolling := false

func _ready() -> void:
    super._ready()
    if light_attack == null:
        light_attack = AttackDataScript.new()
    if heavy_attack == null:
        heavy_attack = AttackDataScript.new()
        heavy_attack.name = "heavy_slash"
        heavy_attack.damage = 20
        heavy_attack.startup_seconds = 0.14
        heavy_attack.active_seconds = 0.12
        heavy_attack.recovery_seconds = 0.32
        heavy_attack.hitbox_size = Vector2(42, 22)
    hitbox.owner_actor = self

func _physics_process(delta: float) -> void:
    if dead:
        return
    apply_gravity(delta)
    if not attacking and not rolling:
        _read_movement()
        _read_combat()
    move_and_slide()

func _read_movement() -> void:
    var direction := Input.get_axis("move_left", "move_right")
    face_direction(direction)
    var speed := run_speed if Input.is_action_pressed("run") else move_speed
    velocity.x = direction * speed
    if is_on_floor() and Input.is_action_just_pressed("jump"):
        velocity.y = jump_velocity
    if Input.is_action_just_pressed("roll"):
        _roll()

func _read_combat() -> void:
    if Input.is_action_just_pressed("light_attack"):
        _attack(light_attack)
    elif Input.is_action_just_pressed("heavy_attack"):
        _attack(heavy_attack)

func _attack(data: Resource) -> void:
    if attacking or rolling:
        return
    attacking = true
    velocity.x = 0
    hitbox.configure(self, data, facing)
    await get_tree().create_timer(data.startup_seconds).timeout
    hitbox.set_active(true)
    await get_tree().create_timer(data.active_seconds).timeout
    hitbox.set_active(false)
    await get_tree().create_timer(data.recovery_seconds).timeout
    attacking = false

func _roll() -> void:
    if rolling or attacking:
        return
    rolling = true
    velocity.x = facing * roll_speed
    await get_tree().create_timer(roll_seconds).timeout
    rolling = false
