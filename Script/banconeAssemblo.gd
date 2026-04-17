extends Area2D

@export var tipo: String = "assemblo"

func _ready():
	pass

func _on_body_entered(body):
	if body.has_method("set_vicino_bancone"):
		body.set_vicino_bancone(true, "assemblaggio")


func _on_body_exited(body):
	if body.has_method("set_vicino_bancone"):
		body.set_vicino_bancone(false, "")


func _on_area_entered(_area: Area2D) -> void:
	pass # Replace with function body.
