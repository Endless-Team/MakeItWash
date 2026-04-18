extends PanelContainer

var player

func _ready() -> void:
	visible = false
	add_to_group("ui_assemblaggio")
	player = get_tree().get_first_node_in_group("player")

func aggiorna_bottoni() -> void:
	# Adatta i nomi dei nodi Button ai tuoi reali nell'editor
	$PanelContainer/VBoxContainer/NigiriSalmone.disabled = not Inventario.ha_ingrediente("Salmone")
	$PanelContainer/VBoxContainer/NigiriGambero.disabled = not Inventario.ha_ingrediente("Gambero")

func _on_button_pressed() -> void:      # Nigiri salmone
	player.assembla_piatto("Nigiri salmone", "Salmone")

func _on_button_2_pressed() -> void:    # Nigiri gambero
	player.assembla_piatto("Nigiri gambero", "Gambero")

func _on_chiudi_pressed() -> void:
	visible = false
