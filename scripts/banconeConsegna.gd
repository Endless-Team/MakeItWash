# Script/banconeConsegna.gd
extends Area2D

signal piatto_depositato(nome: String)

@onready var sprite_piatto: Sprite2D = $SpritePiatto

const TEXTURE_PIATTI = {
	"Nigiri salmone": preload("res://Tiles/Sushi/r_1653.png"),
	"Nigiri gambero":  preload("res://Tiles/Sushi/r_1643.png"),
	"Osomaki": preload("res://Tiles/Sushi/r_1652.png")
}

func _ready() -> void:
	add_to_group("bancone_consegna")
	sprite_piatto.visible = false

func _on_body_entered(body) -> void:
	if body.has_method("set_vicino_bancone"):
		body.set_vicino_bancone(true, "consegna")

func _on_body_exited(body) -> void:
	if body.has_method("set_vicino_bancone"):
		body.set_vicino_bancone(false, "")

func deposita_piatto(nome: String) -> void:
	if TEXTURE_PIATTI.has(nome):
		sprite_piatto.texture = TEXTURE_PIATTI[nome]
		sprite_piatto.visible = true
	emit_signal("piatto_depositato", nome)

func ritira_piatto_visivo() -> void:
	sprite_piatto.visible = false
