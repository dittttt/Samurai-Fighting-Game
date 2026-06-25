extends Area2D

signal hit_landed(target: Node)

@export var damage := 10
@export var knockback := Vector2(120, -20)
@export var active := false
var owner_actor: Node = null
var hit_targets: Array[Node] = []

func _ready() -> void:
    monitoring = true
    monitorable = false
    area_entered.connect(_on_area_entered)
    set_active(active)

func configure(actor: Node, attack: Resource, actor_facing: int) -> void:
    owner_actor = actor
    hit_targets.clear()
    damage = int(attack.damage)
    knockback = attack.knockback
    var shape := get_node_or_null("CollisionShape2D") as CollisionShape2D
    if shape and shape.shape is RectangleShape2D:
        shape.shape.size = attack.hitbox_size
    var attack_width: float = float(attack.hitbox_size.x)
    var actor_width := 100.0
    var x_offset := actor_width + attack_width * 0.5 if actor_facing >= 0 else -attack_width * 0.5
    position = Vector2(x_offset, float(attack.hitbox_offset.y))

func set_active(value: bool) -> void:
    active = value
    var shape := get_node_or_null("CollisionShape2D") as CollisionShape2D
    if shape:
        shape.disabled = not value
    if active:
        call_deferred("_hit_current_overlaps")

func _hit_current_overlaps() -> void:
    if not active:
        return
    for area in get_overlapping_areas():
        _try_hit(area)

func _on_area_entered(area: Area2D) -> void:
    _try_hit(area)

func _try_hit(area: Area2D) -> void:
    if not active or not area.has_method("receive_hit"):
        return
    var target_actor: Node = area.actor
    if target_actor == null or target_actor == owner_actor or target_actor in hit_targets:
        return
    hit_targets.append(target_actor)
    area.receive_hit(damage, owner_actor, knockback)
    hit_landed.emit(target_actor)
