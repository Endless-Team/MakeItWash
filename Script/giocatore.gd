extends CharacterBody2D

const SPEED = 500.0

@onready var sprite = $AnimatedSprite2D

# Le UI vengono aggiunte come figli del CanvasLayer nella scena Sushi
# Il player non le gestisce direttamente — le cerca tramite group o get_node dal parent
var ui_taglio: Node = null
var ui_assemblaggio: Node = null

var last_direction := Vector2.DOWN
var vicino_bancone := false
var tipo_bancone := ""
var sta_tagliando := false

func _ready() -> void:
	add_to_group("player")
	# Aspetta che tutta la scena sia pronta prima di cercare i nodi UI
	await get_tree().process_frame
	ui_taglio = get_tree().get_first_node_in_group("ui_taglio")
	ui_assemblaggio = get_tree().get_first_node_in_group("ui_assemblaggio")

func _physics_process(_delta: float) -> void:
	if Input.is_action_just_pressed("interagisci") and vicino_bancone:
		if tipo_bancone == "taglio" and ui_taglio:
			ui_taglio.visible = true
		elif tipo_bancone == "assemblaggio" and ui_assemblaggio:
			ui_assemblaggio.visible = true

	if Input.is_action_just_pressed("ui_cancel"):
		if ui_taglio: ui_taglio.visible = false
		if ui_assemblaggio: ui_assemblaggio.visible = false

	if sta_tagliando:
		velocity = Vector2.ZERO
		move_and_slide()
		return

	var direction := Vector2.ZERO
	if Input.is_key_pressed(KEY_W) or Input.is_key_pressed(KEY_UP):    direction.y -= 1
	if Input.is_key_pressed(KEY_S) or Input.is_key_pressed(KEY_DOWN):  direction.y += 1
	if Input.is_key_pressed(KEY_A) or Input.is_key_pressed(KEY_LEFT):  direction.x -= 1
	if Input.is_key_pressed(KEY_D) or Input.is_key_pressed(KEY_RIGHT): direction.x += 1

	if direction != Vector2.ZERO:
		direction = direction.normalized()
		last_direction = direction

	if direction.x > 0:      sprite.play("walk_right")
	elif direction.x < 0:    sprite.play("walk_left")
	elif direction.y < 0:    sprite.play("walk_up")
	elif direction.y > 0:    sprite.play("walk_down")
	else:
		if last_direction.y > 0:      sprite.play("idle_down")
		elif last_direction.y < 0:    sprite.play("idle_up")
		elif last_direction.x > 0:    sprite.play("idle_right")
		elif last_direction.x < 0:    sprite.play("idle_left")

	velocity = direction * SPEED
	move_and_slide()

func set_vicino_bancone(valore: bool, tipo: String) -> void:
	vicino_bancone = valore
	tipo_bancone = tipo

func taglia_ingrediente(nome: String) -> void:
	if ui_taglio: ui_taglio.visible = false
	sta_tagliando = true
	print("Stai tagliando: " + nome)
	sta_tagliando = false

func assembla_piatto(nome: String) -> void:
	if ui_assemblaggio: ui_assemblaggio.visible = false
	print("Stai assemblando: " + nome)
