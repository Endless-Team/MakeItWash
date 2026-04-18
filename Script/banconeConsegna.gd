# Script/banconeConsegna.gd
extends Area2D

signal piatto_depositato(nome: String)

func _ready() -> void:
	add_to_group("bancone_consegna") 
	pass

func _on_body_entered(body) -> void:
	if body.has_method("set_vicino_bancone"):
		body.set_vicino_bancone(true, "consegna")

func _on_body_exited(body) -> void:
	if body.has_method("set_vicino_bancone"):
		body.set_vicino_bancone(false, "")

# Chiamato dal giocatore quando preme "interagisci" sul bancone consegna
func deposita_piatto(nome: String) -> void:
	emit_signal("piatto_depositato", nome)
	print("Piatto sul bancone: " + nome)
