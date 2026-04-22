extends PanelContainer

var player

@onready var btn_nigiri_salmone: Button = $VBoxContainer/NigiriSalmone
@onready var btn_nigiri_gambero: Button = $VBoxContainer/NigiriGambero
@onready var btn_osomaki_intero: Button = $VBoxContainer/OsomakiIntero
@onready var btn_chiudi: Button = $VBoxContainer/Chiudi


func _ready() -> void:
	visible = false
	add_to_group("ui_assemblaggio")
	player = get_tree().get_first_node_in_group("player")

	if not Inventario.inventario_cambiato.is_connected(_on_inventario_cambiato):
		Inventario.inventario_cambiato.connect(_on_inventario_cambiato)

	aggiorna_bottoni()


func aggiorna_bottoni() -> void:
	btn_nigiri_salmone.disabled = not Inventario.ha_ingrediente("Salmone")
	btn_nigiri_gambero.disabled = not Inventario.ha_ingrediente("Gambero")
	btn_osomaki_intero.disabled = not Inventario.ha_ingrediente("Salmone")


func _on_nigiri_salmone_pressed() -> void:
	if player:
		player.assembla_piatto("Nigiri salmone", "Salmone")
	aggiorna_bottoni()


func _on_nigiri_gambero_pressed() -> void:
	if player:
		player.assembla_piatto("Nigiri gambero", "Gambero")
	aggiorna_bottoni()


func _on_osomaki_intero_pressed() -> void:
	if player:
		player.assembla_preparazione("Osomaki intero", "Salmone")
	aggiorna_bottoni()


func _on_chiudi_pressed() -> void:
	visible = false


func _on_inventario_cambiato() -> void:
	aggiorna_bottoni()
