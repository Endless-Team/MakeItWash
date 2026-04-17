extends CharacterBody2D

const SPEED = 400.0

@onready var sprite = $AnimatedSprite2D
@onready var ui_taglio = preload("res://UI/Taglio/panelSceltaTaglio.tscn").instantiate()
@onready var ui_assemblaggio = preload("res://UI/Taglio/panelSceltaAssembla.tscn").instantiate()

var last_direction = Vector2.DOWN
var vicino_bancone = false
var tipo_bancone = ""
var sta_tagliando = false

func _ready():
	add_to_group("player")
	get_tree().root.find_child("CanvasLayer", true, false).add_child(ui_taglio)
	get_tree().root.find_child("CanvasLayer", true, false).add_child(ui_assemblaggio)
	ui_taglio.visible = false
	ui_assemblaggio.visible = false

func _physics_process(_delta):
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

func _unhandled_input(event):
	if not (event is InputEventKey or event is InputEventMouseButton):
		return

	if event.is_action_pressed("ui_accept") and vicino_bancone:
		if tipo_bancone == "taglio":
			ui_taglio.visible = true
		elif tipo_bancone == "assemblaggio":
			ui_assemblaggio.visible = true

	if event.is_action_pressed("ui_cancel"):
		ui_taglio.visible = false
		ui_assemblaggio.visible = false

func set_vicino_bancone(valore: bool, tipo: String):
	vicino_bancone = valore
	tipo_bancone = tipo

func taglia_ingrediente(nome: String):
	ui_taglio.visible = false
	sta_tagliando = true
	# sprite.play("chop")
	print("Stai tagliando: " + nome)
	# await sprite.animation_finished
	sta_tagliando = false
	
func assembla_piatto(nome: String):
	ui_assemblaggio.visible = false
	print("Stai assemblando: " + nome)
