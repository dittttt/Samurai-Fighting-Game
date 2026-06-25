extends Area2D

@export var actor_path: NodePath
@onready var actor: Node = _resolve_actor()

func _ready() -> void:
    monitoring = true
    monitorable = true

func _resolve_actor() -> Node:
    if actor_path != NodePath(""):
        return get_node_or_null(actor_path)
    return get_parent()

func receive_hit(damage: int, source: Node, knockback: Vector2) -> void:
    if actor and actor.has_method("take_damage"):
        actor.take_damage(damage, source, knockback)
