extends CharacterBody2D

const SPEED = 120.0
const DIST_STOP = 4.0

@onready var sprite: AnimatedSprite2D = $AnimatedSprite2D
@onready var icona_ordine: Sprite2D = $IconaOrdine
@onready var collision: CollisionShape2D = $CollisionShape2D

const TEXTURE_ORDINI = {
	"Nigiri salmone": preload("res://Tiles/Sushi/r_1653.png"),
	"Nigiri gambero": preload("res://Tiles/Sushi/r_1643.png"),
}

const ORDINI_DISPONIBILI = ["Nigiri salmone", "Nigiri gambero"]

signal ordine_consegnato(nome: String)

enum Stato { INATTIVO, ENTRA, ATTENDE, ESCE }

var stato := Stato.INATTIVO
var ordine := ""
var pos_tavolo := Vector2.ZERO
var pos_uscita := Vector2.ZERO


func _ready() -> void:
	add_to_group("cliente")
	reset_cliente()


func attiva(spawn_pos: Vector2, tavolo_pos: Vector2, uscita_pos: Vector2) -> void:
	global_position = spawn_pos
	pos_tavolo = tavolo_pos
	pos_uscita = uscita_pos
	ordine = ""
	stato = Stato.ENTRA
	velocity = Vector2.ZERO
	icona_ordine.visible = false
	visible = true

	if collision:
		collision.disabled = false

	_set_idle()


func reset_cliente() -> void:
	stato = Stato.INATTIVO
	ordine = ""
	velocity = Vector2.ZERO
	icona_ordine.visible = false

	if collision:
		collision.disabled = true

	_set_idle()


func _physics_process(_delta: float) -> void:
	match stato:
		Stato.INATTIVO:
			velocity = Vector2.ZERO
			_set_idle()
			return

		Stato.ENTRA:
			_muoviti_verso(pos_tavolo)
			if global_position.distance_to(pos_tavolo) <= DIST_STOP:
				global_position = pos_tavolo
				velocity = Vector2.ZERO
				stato = Stato.ATTENDE
				_scegli_ordine()
				_set_idle()

		Stato.ATTENDE:
			velocity = Vector2.ZERO
			_set_idle()

		Stato.ESCE:
			_muoviti_verso(pos_uscita)
			if global_position.distance_to(pos_uscita) <= DIST_STOP:
				reset_cliente()
				visible = false


func _scegli_ordine() -> void:
	ordine = ORDINI_DISPONIBILI[randi() % ORDINI_DISPONIBILI.size()]
	if TEXTURE_ORDINI.has(ordine):
		icona_ordine.texture = TEXTURE_ORDINI[ordine]
	icona_ordine.visible = true


func consegna_piatto(nome: String) -> bool:
	if nome != ordine or stato != Stato.ATTENDE:
		return false

	emit_signal("ordine_consegnato", nome)
	icona_ordine.visible = false
	velocity = Vector2.ZERO
	_set_idle()
	await get_tree().create_timer(0.8).timeout
	stato = Stato.ESCE
	return true


func sta_attendendo() -> bool:
	return stato == Stato.ATTENDE


func get_ordine() -> String:
	return ordine


func _set_idle() -> void:
	if sprite and sprite.sprite_frames and sprite.sprite_frames.has_animation("idle"):
		sprite.play("idle")
	else:
		sprite.stop()


func _muoviti_verso(dest: Vector2) -> void:
	var dir := dest - global_position
	if dir.length() <= DIST_STOP:
		velocity = Vector2.ZERO
		_set_idle()
		return

	dir = dir.normalized()
	velocity = dir * SPEED
	move_and_slide()

	if abs(dir.x) > abs(dir.y):
		sprite.play("walk_right" if dir.x > 0 else "walk_left")
	else:
		sprite.play("walk_down" if dir.y > 0 else "walk_up")
