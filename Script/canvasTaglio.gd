extends PanelContainer

func _ready() -> void:
	visible = false
	add_to_group("ui_taglio")

func _on_salmone_pressed() -> void:
	var player = get_tree().get_first_node_in_group("player")
	if player:
		player.taglia_ingrediente("Salmone")

func _on_gambero_pressed() -> void:
	var player = get_tree().get_first_node_in_group("player")
	if player:
		player.taglia_ingrediente("Gambero")

func _on_chiudi_pressed() -> void:
	visible = false
