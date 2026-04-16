extends CharacterBody2D

const SPEED = 500.0

@onready var sprite = $AnimatedSprite2D
@onready var ui = $"../CanvasLayer/PanelContainer"

var last_direction = Vector2.DOWN
var vicino_bancone = false
var sta_tagliando = false

func _ready():
	ui.visible = false  # la GUI parte nascosta

func _physics_process(delta):
	if sta_tagliando:
		velocity = Vector2.ZERO
		move_and_slide()
		return

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
		last_direction = direction

	if direction.x > 0:
		sprite.play("walk_right")
	elif direction.x < 0:
		sprite.play("walk_left")
	elif direction.y < 0:
		sprite.play("walk_up")
	elif direction.y > 0:
		sprite.play("walk_down")
	else:
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

# _unhandled_input viene chiamato da Godot automaticamente ad ogni tasto premuto.
# A differenza di _physics_process, reagisce solo agli eventi (tasti),
# non gira ogni frame. "unhandled" significa che lo riceve solo se
# nessun altro nodo (es. un Button nella GUI) lo ha già consumato.
func _unhandled_input(event):
	# event.is_action_just_pressed controlla se in QUESTO frame è stato premuto E
	if event.is_action_just_pressed("ui_accept") and vicino_bancone:
		ui.visible = true   # mostra il PanelContainer
	
	# Chiudi la GUI con Escape
	if event.is_action_just_pressed("ui_cancel") and ui.visible:
		ui.visible = false

func taglia_ingrediente(nome: String):
	ui.visible = false
	sta_tagliando = true
	sprite.play("chop")
	print("Stai tagliando: " + nome)
	await sprite.animation_finished
	sta_tagliando = false

# Questo viene chiamato da bancone.gd quando il player entra/esce dall'Area2D
func set_vicino_bancone(valore: bool):
	vicino_bancone = valore
