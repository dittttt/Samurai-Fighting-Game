class_name RoninEnemy
extends CombatActor

@export var target_path: NodePath
@export var attack_data: AttackData
@export var detection_range := 180.0
@export var attack_range := 38.0
@export var attack_cooldown := 1.15

@onready var target: Node2D = get_node_or_null(target_path)
@onready var hitbox: Hitbox = $Hitbox

var attacking := false
var cooldown_left := 0.0

func _ready() -> void:
    super._ready()
    if attack_data == null:
        attack_data = AttackData.new()
    hitbox.owner_actor = self

func _physics_process(delta: float) -> void:
    if dead or target == null:
        return
    cooldown_left = maxf(0.0, cooldown_left - delta)
    apply_gravity(delta)
    if not attacking:
        _think()
    move_and_slide()

func _think() -> void:
    var distance := target.global_position.x - global_position.x
    var abs_distance := absf(distance)
    face_direction(distance)
    if abs_distance > detection_range:
        velocity.x = 0
        return
    if abs_distance <= attack_range and cooldown_left <= 0.0:
        _attack()
    elif abs_distance > attack_range:
        velocity.x = signf(distance) * move_speed
    else:
        velocity.x = 0

func _attack() -> void:
    if attacking:
        return
    attacking = true
    cooldown_left = attack_cooldown
    velocity.x = 0
    hitbox.configure(self, attack_data, facing)
    await get_tree().create_timer(attack_data.startup_seconds).timeout
    hitbox.set_active(true)
    await get_tree().create_timer(attack_data.active_seconds).timeout
    hitbox.set_active(false)
    await get_tree().create_timer(attack_data.recovery_seconds).timeout
    attacking = false
