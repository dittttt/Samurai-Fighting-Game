extends SceneTree

func _initialize() -> void:
    await _run()

func _fail(message: String) -> void:
    printerr("PLAYABLE_SMOKE_FAIL: " + message)
    quit(1)

func _require(condition: bool, message: String) -> void:
    if not condition:
        _fail(message)

func _run() -> void:
    var packed: PackedScene = load("res://scenes/main/Main.tscn")
    _require(packed != null, "Main.tscn did not load")
    var scene: Node = packed.instantiate()
    root.add_child(scene)
    await process_frame
    await physics_frame

    var player: Node = scene.get_node_or_null("Player")
    var enemy: Node = scene.get_node_or_null("RoninEnemy")
    var hud: Node = scene.get_node_or_null("HUD")
    var gm: Node = root.get_node_or_null("GameManager")
    _require(player != null, "missing Player")
    _require(enemy != null, "missing RoninEnemy")
    _require(hud != null, "missing HUD")
    _require(gm != null, "missing GameManager autoload")
    _require(gm.mode == 0, "game should start on the original-style title menu")
    scene.start_from_title()
    await process_frame
    await physics_frame
    _require(gm.mode == 1, "arena did not start from title menu")
    _require(player.health == 100, "player did not reset to 100")
    _require(enemy.health == 100, "enemy did not reset to 100")

    enemy.take_damage(25, player, Vector2.ZERO)
    await process_frame
    _require(enemy.health == 75, "enemy damage failed")
    _require(gm.enemy_health == 75, "enemy HUD/game state did not update")

    player.take_damage(15, enemy, Vector2.ZERO)
    await process_frame
    _require(player.health == 85, "player damage failed")
    _require(gm.player_health == 85, "player HUD/game state did not update")

    enemy.invulnerable = false
    enemy.take_damage(100, player, Vector2.ZERO)
    await process_frame
    _require(gm.mode == 3, "victory did not trigger")
    _require(gm.last_victory, "victory flag was false")

    scene.reset_fight()
    await process_frame
    _require(gm.mode == 1, "restart did not return to arena")
    _require(player.health == 100 and enemy.health == 100, "restart did not reset health")

    print("playable_smoke=PASS playerHealth=%d enemyHealth=%d" % [player.health, enemy.health])
    quit(0)
