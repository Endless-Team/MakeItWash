extends CharacterBody2D

const SPEED = 180.0

@onready var sprite: AnimatedSprite2D = $AnimatedSprite2D

@export var bancone_consegna_path: NodePath
@export var punto_bancone_path: NodePath
@export var punto_tavolo_path: NodePath

enum Stato { IDLE, VAI_BANCONE, VAI_TAVOLO, TORNA }

var stato := Stato.IDLE
var piatto_in_mano := ""
var pos_iniziale := Vector2.ZERO
var target := Vector2.ZERO

func _ready() -> void:
	add_to_group("cameriere")
	pos_iniziale = position
	sprite.play("idle_down")

	var bancone = get_node_or_null(bancone_consegna_path)
	if bancone:
		bancone.piatto_depositato.connect(_on_piatto_depositato)

func _on_piatto_depositato(nome: String) -> void:
	if stato != Stato.IDLE:
		return

	piatto_in_mano = nome
	stato = Stato.VAI_BANCONE

	var punto_bancone = get_node_or_null(punto_bancone_path)
	if punto_bancone:
		target = punto_bancone.position

func _physics_process(_delta: float) -> void:
	match stato:
		Stato.IDLE:
			velocity = Vector2.ZERO
			move_and_slide()

		Stato.VAI_BANCONE:
			_muoviti_verso(target)
			if position.distance_to(target) < 4.0:
				var bancone = get_node_or_null(bancone_consegna_path)
				if bancone and bancone.has_method("ritira_piatto_visivo"):
					bancone.ritira_piatto_visivo()

				stato = Stato.VAI_TAVOLO
				var punto_tavolo = get_node_or_null(punto_tavolo_path)
				if punto_tavolo:
					target = punto_tavolo.position

		Stato.VAI_TAVOLO:
			_muoviti_verso(target)
			if position.distance_to(target) < 4.0:
				var cliente = get_tree().get_first_node_in_group("cliente")
				if cliente and cliente.has_method("consegna_piatto"):
					cliente.consegna_piatto(piatto_in_mano)

				piatto_in_mano = ""
				stato = Stato.TORNA
				target = pos_iniziale

		Stato.TORNA:
			_muoviti_verso(target)
			if position.distance_to(target) < 4.0:
				velocity = Vector2.ZERO
				move_and_slide()
				sprite.play("idle_down")
				stato = Stato.IDLE

func _muoviti_verso(dest: Vector2) -> void:
	var dir = dest - position
	if dir.length() < 0.01:
		velocity = Vector2.ZERO
		return

	dir = dir.normalized()
	velocity = dir * SPEED
	move_and_slide()

	if abs(dir.x) > abs(dir.y):
		sprite.play("walk_right" if dir.x > 0 else "walk_left")
	else:
		sprite.play("walk_down" if dir.y > 0 else "walk_up")
