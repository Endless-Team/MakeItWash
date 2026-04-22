extends PanelContainer

@onready var btn_salmone: Button = $VBoxContainer/Salmone
@onready var btn_gambero: Button = $VBoxContainer/Gambero
@onready var btn_osomaki: Button = $VBoxContainer/Osomaki


func _ready() -> void:
	visible = false
	add_to_group("ui_taglio")

	if not Inventario.soldi_cambiati.is_connected(_on_soldi_cambiati):
		Inventario.soldi_cambiati.connect(_on_soldi_cambiati)

	if not Inventario.inventario_cambiato.is_connected(_on_inventario_cambiato):
		Inventario.inventario_cambiato.connect(_on_inventario_cambiato)

	aggiorna_bottoni()


func aggiorna_bottoni() -> void:
	btn_salmone.disabled = Inventario.get_soldi() < Inventario.COSTI_INGREDIENTI["Salmone"]
	btn_gambero.disabled = Inventario.get_soldi() < Inventario.COSTI_INGREDIENTI["Gambero"]
	btn_osomaki.disabled = not Inventario.ha_preparazione("Osomaki intero")


func _on_salmone_pressed() -> void:
	var player = get_tree().get_first_node_in_group("player")
	if player:
		player.taglia_ingrediente("Salmone")


func _on_gambero_pressed() -> void:
	var player = get_tree().get_first_node_in_group("player")
	if player:
		player.taglia_ingrediente("Gambero")


func _on_osomaki_pressed() -> void:
	var player = get_tree().get_first_node_in_group("player")
	if player:
		player.taglia_osomaki()


func _on_chiudi_pressed() -> void:
	visible = false


func _on_soldi_cambiati(_valore: int) -> void:
	aggiorna_bottoni()


func _on_inventario_cambiato() -> void:
	aggiorna_bottoni()
