class_name Hitbox
extends Area2D

signal hit_landed(target: Node)

@export var damage := 10
@export var knockback := Vector2(120, -20)
@export var active := false
var owner_actor: Node = null

func _ready() -> void:
    monitoring = true
    area_entered.connect(_on_area_entered)

func configure(actor: Node, attack: AttackData, facing: int) -> void:
    owner_actor = actor
    damage = attack.damage
    knockback = attack.knockback
    position = Vector2(attack.hitbox_offset.x * facing, attack.hitbox_offset.y)
    var shape := get_node_or_null("CollisionShape2D") as CollisionShape2D
    if shape and shape.shape is RectangleShape2D:
        shape.shape.size = attack.hitbox_size

func set_active(value: bool) -> void:
    active = value
    var shape := get_node_or_null("CollisionShape2D") as CollisionShape2D
    if shape:
        shape.disabled = not value

func _on_area_entered(area: Area2D) -> void:
    if not active:
        return
    if area is Hurtbox and area.actor != owner_actor:
        area.receive_hit(damage, owner_actor, knockback)
        hit_landed.emit(area.actor)
        set_active(false)
