extends Node

var flags := {}

func set_flag(name: String, value: bool = true) -> void:
    flags[name] = value

func has_flag(name: String) -> bool:
    return bool(flags.get(name, false))

func clear() -> void:
    flags.clear()
