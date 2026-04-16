extends CharacterBody2D

const SPEED = 500.0

@onready var sprite = $AnimatedSprite2D

var last_direction = Vector2.DOWN  # direzione iniziale: guarda in basso

func _physics_process(delta):
	var direction = Vector2.ZERO

	if Input.is_key_pressed(KEY_W) or Input.is_key_pressed(KEY_UP):
		direction.y -= 1
	if Input.is_key_pressed(KEY_S) or Input.is_key_pressed(KEY_DOWN):
		direction.y += 1
	if Input.is_key_pressed(KEY_A) or Input.is_key_pressed(KEY_LEFT):
		direction.x -= 1
	if Input.is_key_pressed(KEY_D) or Input.is_key_pressed(KEY_RIGHT):
		direction.x += 1

	if direction != Vector2.ZERO:
		direction = direction.normalized()
		last_direction = direction  # salva l'ultima direzione

	# Animazione movimento
	if direction.x > 0:
		sprite.play("walk_right")
	elif direction.x < 0:
		sprite.play("walk_left")
	elif direction.y < 0:
		sprite.play("walk_up")
	elif direction.y > 0:
		sprite.play("walk_down")
	else:
		# Idle: usa l'ultima direzione
		if last_direction.y > 0:
			sprite.play("idle_down")
		elif last_direction.y < 0:
			sprite.play("idle_up")
		elif last_direction.x > 0:
			sprite.play("idle_right")
		elif last_direction.x < 0:
			sprite.play("idle_left")

	velocity = direction * SPEED
	move_and_slide()
