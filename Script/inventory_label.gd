extends Label

func _ready() -> void:
	_aggiorna_testo()

	if not Inventario.inventario_cambiato.is_connected(_on_inventario_cambiato):
		Inventario.inventario_cambiato.connect(_on_inventario_cambiato)


func _on_inventario_cambiato() -> void:
	_aggiorna_testo()


func _aggiorna_testo() -> void:
	var salmone: int = int(Inventario.ingredienti.get("Salmone", 0))
	var gambero: int = int(Inventario.ingredienti.get("Gambero", 0))
	var piatti: int = Inventario.piatti_pronti.size()

	text = "Inventario\nSalmone: %d\nGambero: %d\nPiatti: %d" % [salmone, gambero, piatti]
