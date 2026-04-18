extends CharacterBody2D

const SPEED = 120.0

@onready var sprite: AnimatedSprite2D = $AnimatedSprite2D
@onready var icona_ordine: Sprite2D = $IconaOrdine

const TEXTURE_ORDINI = {
	"Nigiri salmone": preload("res://Tiles/Sushi/r_1653.png"),
	"Nigiri gambero":  preload("res://Tiles/Sushi/r_1643.png"),
}
const ORDINI_DISPONIBILI = ["Nigiri salmone", "Nigiri gambero"]

signal ordine_consegnato(nome: String)

enum Stato { ENTRA, ATTENDE, RICEVE, ESCE }

var stato := Stato.ENTRA
var ordine := ""
var pos_tavolo := Vector2.ZERO
var pos_uscita := Vector2.ZERO

func _ready() -> void:
	add_to_group("cliente")
	icona_ordine.visible = false
	# Posizione di uscita = fuori schermo in basso (stessa X del tavolo)
	pos_uscita = Vector2(global_position.x, global_position.y + 400)

func inizia(tavolo: Vector2, uscita: Vector2) -> void:
	pos_tavolo = tavolo
	pos_uscita = uscita
	stato = Stato.ENTRA

func _physics_process(_delta: float) -> void:
	match stato:
		Stato.ENTRA:
			_muoviti_verso(pos_tavolo)
			if global_position.distance_to(pos_tavolo) < 6.0:
				velocity = Vector2.ZERO
				stato = Stato.ATTENDE
				_scegli_ordine()
		Stato.ATTENDE:
			pass  # aspetta che il cameriere arrivi
		Stato.RICEVE:
			# Animazione breve "mangia" poi se ne va
			icona_ordine.visible = false
			await get_tree().create_timer(0.8).timeout
			stato = Stato.ESCE
		Stato.ESCE:
			_muoviti_verso(pos_uscita)
			if global_position.distance_to(pos_uscita) < 6.0:
				queue_free()

func _scegli_ordine() -> void:
	ordine = ORDINI_DISPONIBILI[randi() % ORDINI_DISPONIBILI.size()]
	if TEXTURE_ORDINI.has(ordine):
		icona_ordine.texture = TEXTURE_ORDINI[ordine]
	icona_ordine.visible = true
	sprite.play("idle_down")
	print("Cliente vuole: " + ordine)

func consegna_piatto(nome: String) -> void:
	if nome == ordine and stato == Stato.ATTENDE:
		stato = Stato.RICEVE
		emit_signal("ordine_consegnato", nome)

func _muoviti_verso(dest: Vector2) -> void:
	var dir = (dest - global_position).normalized()
	velocity = dir * SPEED
	move_and_slide()
	if abs(dir.x) > abs(dir.y):
		sprite.play("walk_right" if dir.x > 0 else "walk_left")
	else:
		sprite.play("walk_down" if dir.y > 0 else "walk_up")
