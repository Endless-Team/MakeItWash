extends CharacterBody2D

# Velocità di movimento del player in pixel/secondo
const SPEED = 400.0

# Riferimento allo sprite animato del player
@onready var sprite = $AnimatedSprite2D
# Riferimento al pannello UI (inventario/bancone) nel CanvasLayer
@onready var ui = $"../CanvasLayer/PanelContainer"

# Ultima direzione in cui il player si è mosso (usata per gli idle)
var last_direction = Vector2.DOWN
# True quando il player è nell'area di interazione di un bancone
var vicino_bancone = false
# True durante l'animazione di taglio (blocca il movimento)
var sta_tagliando = false

# Decommentare per nascondere la GUI all'avvio della scena
func _ready():
	add_to_group("player")
	ui.visible = false

# Chiamato ogni frame fisico — gestisce movimento e animazioni
func _physics_process(_delta):
	# Se il player sta tagliando, blocca ogni movimento
	if sta_tagliando:
		velocity = Vector2.ZERO
		move_and_slide()
		return

	# Raccoglie l'input direzionale da tastiera (WASD + frecce)
	var direction = Vector2.ZERO
	if Input.is_key_pressed(KEY_W) or Input.is_key_pressed(KEY_UP):
		direction.y -= 1
	if Input.is_key_pressed(KEY_S) or Input.is_key_pressed(KEY_DOWN):
		direction.y += 1
	if Input.is_key_pressed(KEY_A) or Input.is_key_pressed(KEY_LEFT):
		direction.x -= 1
	if Input.is_key_pressed(KEY_D) or Input.is_key_pressed(KEY_RIGHT):
		direction.x += 1

	# Normalizza la direzione per evitare velocità maggiore in diagonale
	# e aggiorna last_direction solo se ci si sta muovendo
	if direction != Vector2.ZERO:
		direction = direction.normalized()
		last_direction = direction

	# Sceglie l'animazione di camminata in base alla direzione corrente,
	# oppure l'animazione idle basata sull'ultima direzione se fermo
	if direction.x > 0:
		sprite.play("walk_right")
	elif direction.x < 0:
		sprite.play("walk_left")
	elif direction.y < 0:
		sprite.play("walk_up")
	elif direction.y > 0:
		sprite.play("walk_down")
	else:
		# Player fermo: riproduce l'idle nella direzione in cui guardava
		if last_direction.y > 0:
			sprite.play("idle_down")
		elif last_direction.y < 0:
			sprite.play("idle_up")
		elif last_direction.x > 0:
			sprite.play("idle_right")
		elif last_direction.x < 0:
			sprite.play("idle_left")

	# Applica la velocità e gestisce le collisioni
	velocity = direction * SPEED
	move_and_slide()

# Gestisce i tasti premuti che non sono già stati consumati da altri nodi
# (es. bottoni UI). Viene chiamato una volta per evento, non ogni frame.
func _unhandled_input(event):
	# Ignora eventi che non sono tastiera o mouse
	if not (event is InputEventKey or event is InputEventMouseButton):
		return

	# Apre la GUI del bancone solo se il player è nell'area di interazione
	if event.is_action_pressed("ui_accept") and vicino_bancone:
		ui.visible = true

	# Chiude la GUI se è aperta
	if event.is_action_pressed("ui_cancel") and ui.visible:
		ui.visible = false

# Avvia l'animazione di taglio per un ingrediente specifico.
# Blocca il movimento per tutta la durata dell'animazione.
# Chiamata esternamente dalla GUI del bancone.
func taglia_ingrediente(nome: String):
	ui.visible = false
	sta_tagliando = true
	#sprite.play("chop")
	print("Stai tagliando: " + nome)
	# Attende il completamento dell'animazione prima di sbloccare il movimento
	await sprite.animation_finished
	sta_tagliando = false

# Setter chiamato da bancone.gd tramite Area2D
# quando il player entra (valore = true) o esce (valore = false) dalla zona
func set_vicino_bancone(valore: bool):
	vicino_bancone = valore
