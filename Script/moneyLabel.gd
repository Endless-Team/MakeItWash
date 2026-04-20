extends Label

func _ready() -> void:
	text = "€ " + str(Inventario.get_soldi())
	if not Inventario.soldi_cambiati.is_connected(_on_soldi_cambiati):
		Inventario.soldi_cambiati.connect(_on_soldi_cambiati)


func _on_soldi_cambiati(valore: int) -> void:
	text = "€ " + str(valore)
