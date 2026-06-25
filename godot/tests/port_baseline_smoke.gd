extends SceneTree

func _initialize() -> void:
    await _run()

func _fail(message: String) -> void:
    printerr("PORT_BASELINE_FAIL: " + message)
    quit(1)

func _require(condition: bool, message: String) -> void:
    if not condition:
        _fail(message)

func _run() -> void:
    _require(ProjectSettings.get_setting("display/window/size/viewport_width") == 1920, "viewport width is not Java original 1920")
    _require(ProjectSettings.get_setting("display/window/size/viewport_height") == 1120, "viewport height is not Java original 1120")

    var packed: PackedScene = load("res://scenes/main/Main.tscn")
    _require(packed != null, "Main.tscn did not load")
    var scene: Node = packed.instantiate()
    root.add_child(scene)
    await process_frame

    var player: CharacterBody2D = scene.get_node_or_null("Player") as CharacterBody2D
    var enemy: CharacterBody2D = scene.get_node_or_null("RoninEnemy") as CharacterBody2D
    var level: Node = scene.get_node_or_null("LegacyMistyLevel")
    _require(player != null, "missing Player")
    _require(enemy != null, "missing RoninEnemy")
    _require(level != null, "missing LegacyMistyLevel")

    scene.reset_fight()
    _require(player.global_position == Vector2(100, 200), "player reset does not match Java hitbox start")
    _require(enemy.global_position == Vector2(1719, 200), "enemy reset does not match Java hitbox start")

    var player_shape_node: CollisionShape2D = player.get_node("CollisionShape2D") as CollisionShape2D
    var enemy_shape_node: CollisionShape2D = enemy.get_node("CollisionShape2D") as CollisionShape2D
    _require(player_shape_node.shape is RectangleShape2D, "player shape is not rectangle")
    _require(enemy_shape_node.shape is RectangleShape2D, "enemy shape is not rectangle")
    _require((player_shape_node.shape as RectangleShape2D).size == Vector2(100, 150), "player hitbox is not Java 20x30 scaled by 5")
    _require((enemy_shape_node.shape as RectangleShape2D).size == Vector2(100, 150), "enemy hitbox is not Java 20x30 scaled by 5")

    var sprite: Sprite2D = player.get_node("Sprite2D") as Sprite2D
    _require(sprite != null, "player sprite missing")
    _require(sprite.scale == Vector2(5, 5), "player atlas scale is not Java scale 5")
    _require(sprite.position == Vector2(60, 0), "player atlas offset does not match Java draw offset")
    _require(sprite.texture != null, "player legacy samurai atlas did not load")

    _require(is_equal_approx(player.atlas_animator.FRAME_SECONDS[5], 0.060), "hit animation speed does not match Java aniSpeed 12")
    _require(is_equal_approx(player.atlas_animator.FRAME_SECONDS[6], 0.060), "death animation speed does not match Java aniSpeed 12")
    _require(is_equal_approx(player.atlas_animator.FRAME_SECONDS[7], 0.100), "light attack animation speed does not match Java aniSpeed 20")
    _require(is_equal_approx(player.atlas_animator.FRAME_SECONDS[8], 0.105), "heavy attack animation speed does not match Java aniSpeed 21")
    _require(is_equal_approx(player.light_attack.startup_seconds, 0.40), "light attack hit timing does not match Java hit frame 4")
    _require(is_equal_approx(player.heavy_attack.startup_seconds, 0.525), "heavy attack hit timing does not match Java hit frame 5")
    _require(is_equal_approx(enemy.detection_range, 400.0), "enemy chase keep range does not match Java 400")
    _require(is_equal_approx(enemy.attack_range, 60.0), "enemy attack entry range does not match Java ATTACK_RANGE*1.2")
    _require(is_equal_approx(enemy.attack_cooldown, 1.50), "enemy attack cooldown does not match Java 1500ms baseline")

    player.take_damage(10, enemy, Vector2.ZERO)
    await process_frame
    await physics_frame
    _require(player.hit_reacting, "player did not enter hit reaction after damage")
    _require(player.current_visual_action == 5, "player did not keep Java HIT animation row after damage")
    await create_timer(player.hit_reaction_seconds + 0.05).timeout
    await process_frame
    _require(not player.hit_reacting, "player hit reaction did not clear after hit animation time")
    scene.reset_fight()

    _require(player.collision_layer == 2, "player should be on actor collision layer while walking/running")
    _require(player.collision_mask == 3, "player should collide with world and actors while walking/running")

    player._roll()
    await process_frame
    _require(player.rolling, "roll did not start")
    _require(player.collision_layer == 0, "rolling should remove actor layer so enemies can pass through")
    _require(player.collision_mask == 1, "rolling should collide with world only")

    await create_timer(player.roll_seconds + 0.05).timeout
    await process_frame
    _require(not player.rolling, "roll did not finish")
    _require(player.collision_layer == 2, "player actor layer was not restored after roll")
    _require(player.collision_mask == 3, "player actor/world mask was not restored after roll")

    scene.queue_free()
    print("port_baseline_smoke=PASS viewport=1920x1120 spriteScale=5 hitAnimation=row5 javaAnimTiming=PASS javaAttackTiming=PASS actorCollision=walk_blocks roll_passes")
    quit(0)
