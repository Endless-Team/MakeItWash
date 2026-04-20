extends PanelContainer

@onready var btn_salmone: Button = $VBoxContainer/Salmone
@onready var btn_gambero: Button = $VBoxContainer/Gambero


func _ready() -> void:
	visible = false
	add_to_group("ui_taglio")

	if not Inventario.soldi_cambiati.is_connected(aggiorna_bottoni):
		Inventario.soldi_cambiati.connect(_on_soldi_cambiati)

	aggiorna_bottoni()


func aggiorna_bottoni() -> void:
	btn_salmone.disabled = Inventario.get_soldi() < Inventario.COSTI_INGREDIENTI["Salmone"]
	btn_gambero.disabled = Inventario.get_soldi() < Inventario.COSTI_INGREDIENTI["Gambero"]


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


func _on_soldi_cambiati(_valore: int) -> void:
	aggiorna_bottoni()
