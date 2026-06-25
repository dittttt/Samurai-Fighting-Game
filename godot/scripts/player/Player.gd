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
    body_color = Color(0.84, 0.80, 0.65)
    accent_color = Color(0.47, 0.03, 0.09)
    nameplate = "Wago"
    super._ready()
    setup_legacy_sprite("res://assets/legacy_misty/res/samurai_atlas.png")
    if light_attack == null:
        light_attack = AttackDataScript.new()
        light_attack.name = "light_slash"
        light_attack.damage = 10
        light_attack.startup_seconds = 0.06
        light_attack.active_seconds = 0.11
        light_attack.recovery_seconds = 0.16
        light_attack.hitbox_size = Vector2(36, 20)
        light_attack.hitbox_offset = Vector2(24, -18)
    if heavy_attack == null:
        heavy_attack = AttackDataScript.new()
        heavy_attack.name = "heavy_slash"
        heavy_attack.damage = 24
        heavy_attack.startup_seconds = 0.14
        heavy_attack.active_seconds = 0.13
        heavy_attack.recovery_seconds = 0.32
        heavy_attack.hitbox_size = Vector2(44, 24)
        heavy_attack.hitbox_offset = Vector2(28, -18)
        heavy_attack.knockback = Vector2(170, -40)
    hitbox.owner_actor = self

func reset_actor(start_position: Vector2) -> void:
    super.reset_actor(start_position)
    attacking = false
    rolling = false
    if is_instance_valid(hitbox):
        hitbox.set_active(false)

func _physics_process(delta: float) -> void:
    _select_visual_action()
    update_visual(delta)
    if dead:
        apply_gravity(delta)
        velocity.x = move_toward(velocity.x, 0.0, move_speed * delta)
        move_and_slide()
        return
    if not GameManager.is_playing():
        velocity.x = 0
        apply_gravity(delta)
        move_and_slide()
        return
    apply_gravity(delta)
    if not attacking and not rolling:
        _read_movement()
        _read_combat()
    move_and_slide()


func _select_visual_action() -> void:
    if dead:
        set_visual_action(6, false)
    elif attacking:
        set_visual_action(8 if current_visual_action == 8 else 7, false)
    elif rolling:
        set_visual_action(4, false)
    elif not is_on_floor():
        set_visual_action(3, true)
    elif absf(velocity.x) > 5.0:
        set_visual_action(1, true)
    else:
        set_visual_action(0, true)

func _read_movement() -> void:
    var direction: float = Input.get_axis("move_left", "move_right")
    face_direction(direction)
    var speed: float = run_speed if Input.is_action_pressed("run") else move_speed
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
    if attacking or rolling or dead or not GameManager.is_playing():
        return
    attacking = true
    set_visual_action(8 if data == heavy_attack else 7, false, true)
    velocity.x = 0
    hitbox.configure(self, data, facing)
    await get_tree().create_timer(data.startup_seconds).timeout
    if dead or not GameManager.is_playing():
        attacking = false
        hitbox.set_active(false)
        return
    hitbox.set_active(true)
    await get_tree().create_timer(data.active_seconds).timeout
    hitbox.set_active(false)
    await get_tree().create_timer(data.recovery_seconds).timeout
    attacking = false

func _roll() -> void:
    if rolling or attacking or dead or not GameManager.is_playing():
        return
    rolling = true
    invulnerable = true
    velocity.x = facing * roll_speed
    await get_tree().create_timer(roll_seconds).timeout
    invulnerable = false
    rolling = false
