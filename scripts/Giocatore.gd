# fix: consegna robusta e supporto completo a nigiri e osomaki nel flusso inventario
extends CharacterBody2D

const SPEED = 500.0

@onready var sprite = $AnimatedSprite2D

var ui_taglio: Node = null
var ui_assemblaggio: Node = null
var last_direction := Vector2.DOWN
var vicino_bancone := false
var tipo_bancone := ""
var sta_tagliando := false


func _ready() -> void:
	add_to_group("player")
	await get_tree().process_frame
	ui_taglio = get_tree().get_first_node_in_group("ui_taglio")
	ui_assemblaggio = get_tree().get_first_node_in_group("ui_assemblaggio")


func _physics_process(_delta: float) -> void:
	if Input.is_action_just_pressed("interagisci") and vicino_bancone:
		if tipo_bancone == "taglio" and ui_taglio:
			ui_taglio.visible = true
			if ui_taglio.has_method("aggiorna_bottoni"):
				ui_taglio.aggiorna_bottoni()

		elif tipo_bancone == "assemblaggio" and ui_assemblaggio:
			ui_assemblaggio.visible = true
			if ui_assemblaggio.has_method("aggiorna_bottoni"):
				ui_assemblaggio.aggiorna_bottoni()

		elif tipo_bancone == "consegna" and Inventario.piatti_pronti.size() > 0:
			var bancone = get_tree().get_first_node_in_group("bancone_consegna")
			if bancone and bancone.has_method("deposita_piatto"):
				var piatto = Inventario.ritira_piatto()
				var depositato: bool = bancone.deposita_piatto(piatto)
				if not depositato and piatto != "":
					Inventario.piatti_pronti.push_front(piatto)
					Inventario.emit_signal("inventario_cambiato")

	if Input.is_action_just_pressed("ui_cancel"):
		if ui_taglio:
			ui_taglio.visible = false
		if ui_assemblaggio:
			ui_assemblaggio.visible = false

	if sta_tagliando:
		velocity = Vector2.ZERO
		move_and_slide()
		return

	var direction := Vector2.ZERO

	if Input.is_action_pressed("ui_up") or Input.is_key_pressed(KEY_W):
		direction.y -= 1
	if Input.is_action_pressed("ui_down") or Input.is_key_pressed(KEY_S):
		direction.y += 1
	if Input.is_action_pressed("ui_left") or Input.is_key_pressed(KEY_A):
		direction.x -= 1
	if Input.is_action_pressed("ui_right") or Input.is_key_pressed(KEY_D):
		direction.x += 1

	var touch_ui = get_tree().get_first_node_in_group("touch_ui")
	if touch_ui and touch_ui.has_method("get_input_vector"):
		var touch_dir: Vector2 = touch_ui.get_input_vector()
		if touch_dir.length() > 0.15:
			direction = touch_dir

	if direction.length() > 1.0:
		direction = direction.normalized()

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


func set_vicino_bancone(valore: bool, tipo: String) -> void:
	vicino_bancone = valore
	tipo_bancone = tipo


func taglia_ingrediente(nome: String) -> void:
	sta_tagliando = true

	var ok := Inventario.aggiungi_ingrediente(nome)
	if ok:
		print("Tagliato: " + nome)
	else:
		print("Soldi insufficienti per: " + nome)

	sta_tagliando = false

	if ui_taglio:
		ui_taglio.visible = false
		if ui_taglio.has_method("aggiorna_bottoni"):
			ui_taglio.aggiorna_bottoni()


func taglia_osomaki() -> void:
	if not Inventario.ha_preparazione("Osomaki intero"):
		print("Non hai Osomaki intero")
		return

	sta_tagliando = true
	Inventario.rimuovi_preparazione("Osomaki intero")
	Inventario.aggiungi_piatto("Osomaki")
	print("Tagliato: Osomaki intero -> Osomaki")
	sta_tagliando = false

	if ui_taglio:
		ui_taglio.visible = false
		if ui_taglio.has_method("aggiorna_bottoni"):
			ui_taglio.aggiorna_bottoni()


func assembla_piatto(nome_piatto: String, ingrediente_richiesto: String) -> void:
	if not Inventario.ha_ingrediente(ingrediente_richiesto):
		print("Non hai: " + ingrediente_richiesto)
		return

	Inventario.rimuovi_ingrediente(ingrediente_richiesto)
	Inventario.aggiungi_piatto(nome_piatto)
	print("Assemblato: " + nome_piatto)

	if ui_assemblaggio:
		ui_assemblaggio.visible = false
		if ui_assemblaggio.has_method("aggiorna_bottoni"):
			ui_assemblaggio.aggiorna_bottoni()


func assembla_osomaki_intero() -> void:
	if not Inventario.ha_ingrediente("Salmone"):
		print("Non hai Salmone")
		return

	Inventario.rimuovi_ingrediente("Salmone")
	Inventario.aggiungi_preparazione("Osomaki intero")
	print("Assemblato: Osomaki intero")

	if ui_assemblaggio:
		ui_assemblaggio.visible = false
		if ui_assemblaggio.has_method("aggiorna_bottoni"):
			ui_assemblaggio.aggiorna_bottoni()
