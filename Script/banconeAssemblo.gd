extends Area2D

func _ready() -> void:
	add_to_group("bancone_assemblo")
	# Debug: conferma che il nodo è pronto
	print("ZonaAssemblo pronta - layer:", collision_layer, " mask:", collision_mask)

func _on_body_entered(body: Node2D) -> void:
	print("ZonaAssemblo: body_entered ->", body.name)
	if body.is_in_group("player"):
		body.set_vicino_bancone(true, "assemblaggio")

func _on_body_exited(body: Node2D) -> void:
	print("ZonaAssemblo: body_exited ->", body.name)
	if body.is_in_group("player") and body.tipo_bancone == "assemblaggio":
		body.set_vicino_bancone(false, "")

func _on_area_entered(_area: Area2D) -> void:
	pass
