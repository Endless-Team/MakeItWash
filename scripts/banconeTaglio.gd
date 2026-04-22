extends Area2D

@export var tipo: String = "taglio"  # oppure "assemblaggio"

func _ready():
	pass

func _on_body_entered(body):
	if body.has_method("set_vicino_bancone"):
		body.set_vicino_bancone(true, tipo)  # passa anche il tipo

func _on_body_exited(body):
	if body.has_method("set_vicino_bancone"):
		body.set_vicino_bancone(false, "")
