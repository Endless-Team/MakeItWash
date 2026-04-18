extends PanelContainer

var player

func _ready():
	visible = false
	player = get_tree().get_first_node_in_group("player")
	add_to_group("ui_taglio")

func _on_salmone_pressed():
	player.taglia_ingrediente("Salmone")

func _on_gambero_pressed():
	player.taglia_ingrediente("Gambero")

func _on_chiudi_pressed():
	visible = false
