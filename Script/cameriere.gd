extends CharacterBody2D

const SPEED = 180.0
const DIST_STOP = 6.0

@onready var sprite: AnimatedSprite2D = $AnimatedSprite2D

@export var bancone_consegna_path: NodePath
@export var punto_bancone_path: NodePath
@export var punto_tavolo_path: NodePath

enum Stato { IDLE, VAI_BANCONE, VAI_TAVOLO, TORNA }

var stato := Stato.IDLE
var piatto_in_mano := ""
var pos_iniziale := Vector2.ZERO
var target := Vector2.ZERO
var cliente_target: Node = null


func _ready() -> void:
	add_to_group("cameriere")
	pos_iniziale = global_position
	sprite.play("idle_down")

	var bancone = get_node_or_null(bancone_consegna_path)
	if bancone:
		bancone.piatto_depositato.connect(_on_piatto_depositato)


func _on_piatto_depositato(nome: String) -> void:
	if stato != Stato.IDLE:
		return

	cliente_target = _trova_cliente_per_piatto(nome)
	if cliente_target == null:
		return

	piatto_in_mano = nome
	stato = Stato.VAI_BANCONE

	var punto_bancone = get_node_or_null(punto_bancone_path)
	if punto_bancone:
		target = punto_bancone.global_position
	else:
		target = global_position


func _physics_process(_delta: float) -> void:
	match stato:
		Stato.IDLE:
			velocity = Vector2.ZERO
			move_and_slide()

		Stato.VAI_BANCONE:
			_muoviti_verso(target)
			if global_position.distance_to(target) <= DIST_STOP:
				var bancone = get_node_or_null(bancone_consegna_path)
				if bancone and bancone.has_method("ritira_piatto_visivo"):
					bancone.ritira_piatto_visivo()

				if is_instance_valid(cliente_target):
					target = cliente_target.global_position
					stato = Stato.VAI_TAVOLO
				else:
					_reset_stato()

		Stato.VAI_TAVOLO:
			if not is_instance_valid(cliente_target):
				_reset_stato()
				return

			target = cliente_target.global_position
			_muoviti_verso(target)

			if global_position.distance_to(target) <= DIST_STOP + 8.0:
				var consegna_ok := false
				if cliente_target.has_method("consegna_piatto"):
					consegna_ok = await cliente_target.consegna_piatto(piatto_in_mano)

				if consegna_ok:
					piatto_in_mano = ""

				cliente_target = null
				stato = Stato.TORNA
				target = pos_iniziale

		Stato.TORNA:
			_muoviti_verso(target)
			if global_position.distance_to(target) <= DIST_STOP:
				velocity = Vector2.ZERO
				move_and_slide()
				sprite.play("idle_down")
				stato = Stato.IDLE


func _trova_cliente_per_piatto(nome_piatto: String) -> Node:
	var clienti = get_tree().get_nodes_in_group("cliente")
	var migliore: Node = null
	var distanza_migliore := INF
	var punto_tavolo = get_node_or_null(punto_tavolo_path)
	var riferimento := global_position

	if punto_tavolo:
		riferimento = punto_tavolo.global_position

	for cliente in clienti:
		if cliente == null:
			continue
		if not is_instance_valid(cliente):
			continue
		if not cliente.has_method("sta_attendendo"):
			continue
		if not cliente.has_method("get_ordine"):
			continue
		if not cliente.sta_attendendo():
			continue
		if cliente.get_ordine() != nome_piatto:
			continue

		var distanza = riferimento.distance_to(cliente.global_position)
		if distanza < distanza_migliore:
			distanza_migliore = distanza
			migliore = cliente

	return migliore


func _reset_stato() -> void:
	piatto_in_mano = ""
	cliente_target = null
	stato = Stato.TORNA
	target = pos_iniziale


func _muoviti_verso(dest: Vector2) -> void:
	var dir := dest - global_position
	if dir.length() <= DIST_STOP:
		velocity = Vector2.ZERO
		return

	dir = dir.normalized()
	velocity = dir * SPEED
	move_and_slide()

	if abs(dir.x) > abs(dir.y):
		sprite.play("walk_right" if dir.x > 0 else "walk_left")
	else:
		sprite.play("walk_down" if dir.y > 0 else "walk_up")
