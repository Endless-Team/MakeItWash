extends CharacterBody2D

const SPEED = 120.0

@onready var sprite: AnimatedSprite2D = $AnimatedSprite2D
@onready var icona_ordine: Sprite2D = $IconaOrdine

const TEXTURE_ORDINI = {
	"Nigiri salmone": preload("res://Tiles/Sushi/r_1653.png"),
	"Nigiri gambero": preload("res://Tiles/Sushi/r_1643.png"),
}
const ORDINI_DISPONIBILI = ["Nigiri salmone", "Nigiri gambero"]

signal ordine_consegnato(nome: String)

enum Stato { ENTRA, ATTENDE, ESCE }

var stato := Stato.ENTRA
var ordine := ""
var pos_tavolo := Vector2.ZERO
var pos_uscita := Vector2.ZERO

func _ready() -> void:
	add_to_group("cliente")
	icona_ordine.visible = false

func inizia(tavolo: Vector2, uscita: Vector2) -> void:
	pos_tavolo = tavolo
	pos_uscita = uscita
	stato = Stato.ENTRA

func _physics_process(_delta: float) -> void:
	match stato:
		Stato.ENTRA:
			_muoviti_verso(pos_tavolo)
			if position.distance_to(pos_tavolo) < 3.0:
				velocity = Vector2.ZERO
				move_and_slide()
				stato = Stato.ATTENDE
				_scegli_ordine()

		Stato.ATTENDE:
			velocity = Vector2.ZERO
			move_and_slide()

		Stato.ESCE:
			_muoviti_verso(pos_uscita)
			if position.distance_to(pos_uscita) < 3.0:
				queue_free()

func _scegli_ordine() -> void:
	ordine = ORDINI_DISPONIBILI[randi() % ORDINI_DISPONIBILI.size()]
	if TEXTURE_ORDINI.has(ordine):
		icona_ordine.texture = TEXTURE_ORDINI[ordine]
	icona_ordine.visible = true
	sprite.stop()

func consegna_piatto(nome: String) -> void:
	if nome == ordine and stato == Stato.ATTENDE:
		emit_signal("ordine_consegnato", nome)
		icona_ordine.visible = false
		sprite.stop()
		await get_tree().create_timer(0.8).timeout
		stato = Stato.ESCE

func _muoviti_verso(dest: Vector2) -> void:
	var dir = (dest - position)
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
