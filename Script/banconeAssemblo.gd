extends Area2D

@export var tipo: String = "assemblo"

func _ready() -> void:
	add_to_group("bancone_assemblo")
	pass

func _on_body_entered(body) -> void:
	if body.has_method("set_vicino_bancone"):
		body.set_vicino_bancone(true, "assemblaggio")

func _on_body_exited(body) -> void:
	if body.has_method("set_vicino_bancone"):
		body.set_vicino_bancone(false, "")


func _on_area_entered(area: Area2D) -> void:
	pass # Replace with function body.
