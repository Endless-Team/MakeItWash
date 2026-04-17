extends PanelContainer

var player

func _ready():
	visible = false
	player = get_tree().get_first_node_in_group("player")

func _on_button_pressed():   # Nigiri salmone
	player.assembla_piatto("Nigiri salmone")

func _on_button_2_pressed():  # Nigiri gambero
	player.assembla_piatto("Nigiri gambero")

func _on_chiudi_pressed():
	visible = false
