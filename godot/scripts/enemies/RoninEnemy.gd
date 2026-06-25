extends "res://scripts/combat/CombatActor.gd"

const AttackDataScript := preload("res://scripts/combat/AttackData.gd")

@export var target_path: NodePath
@export var attack_data: Resource
@export var detection_range := 1200.0
@export var attack_range := 250.0
@export var desired_range := 180.0
@export var attack_cooldown := 0.95

@onready var target: Node2D = get_node_or_null(target_path)
@onready var hitbox: Area2D = $Hitbox

var attacking := false
var cooldown_left := 0.0

func _ready() -> void:
    body_color = Color(0.50, 0.56, 0.62)
    accent_color = Color(0.04, 0.07, 0.11)
    nameplate = "Ronin"
    super._ready()
    setup_legacy_sprite("res://assets/legacy_misty/res/ronin_atlas.png")
    if attack_data == null:
        attack_data = AttackDataScript.new()
        attack_data.name = "ronin_slash"
        attack_data.damage = 10
        attack_data.startup_seconds = 0.16
        attack_data.active_seconds = 0.11
        attack_data.recovery_seconds = 0.28
        attack_data.hitbox_size = Vector2(200, 100)
        attack_data.hitbox_offset = Vector2(200, 50)
    hitbox.owner_actor = self

func reset_actor(start_position: Vector2) -> void:
    super.reset_actor(start_position)
    attacking = false
    cooldown_left = 0.45
    if is_instance_valid(hitbox):
        hitbox.set_active(false)

func _physics_process(delta: float) -> void:
    _select_visual_action()
    update_visual(delta)
    if target == null:
        return
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
    cooldown_left = maxf(0.0, cooldown_left - delta)
    apply_gravity(delta)
    if not attacking:
        _think()
    move_and_slide()


func _select_visual_action() -> void:
    if dead:
        set_visual_action(6, false)
    elif attacking:
        set_visual_action(7, false)
    elif not is_on_floor():
        set_visual_action(3, true)
    elif absf(velocity.x) > 5.0:
        set_visual_action(1, true)
    else:
        set_visual_action(0, true)

func _think() -> void:
    var distance: float = target.global_position.x - global_position.x
    var abs_distance: float = absf(distance)
    face_direction(distance)
    if abs_distance > detection_range:
        velocity.x = 0
        return
    if abs_distance <= attack_range and cooldown_left <= 0.0:
        _attack()
    elif abs_distance > desired_range:
        velocity.x = signf(distance) * move_speed
    else:
        velocity.x = 0

func _attack() -> void:
    if attacking or dead or not GameManager.is_playing():
        return
    attacking = true
    set_visual_action(7, false, true)
    cooldown_left = attack_cooldown
    velocity.x = 0
    hitbox.configure(self, attack_data, facing)
    await get_tree().create_timer(attack_data.startup_seconds).timeout
    if dead or not GameManager.is_playing():
        attacking = false
        hitbox.set_active(false)
        return
    hitbox.set_active(true)
    await get_tree().create_timer(attack_data.active_seconds).timeout
    hitbox.set_active(false)
    await get_tree().create_timer(attack_data.recovery_seconds).timeout
    attacking = false
