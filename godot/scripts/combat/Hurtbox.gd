class_name Hurtbox
extends Area2D

@export var actor_path: NodePath
@onready var actor: Node = get_node_or_null(actor_path) if actor_path != NodePath("") else owner

func receive_hit(damage: int, source: Node, knockback: Vector2) -> void:
    if actor and actor.has_method("take_damage"):
        actor.take_damage(damage, source, knockback)
