# fix: money label mostra solo i soldi, senza stile nel codice
extends Label


func _ready() -> void:
	_aggiorna_testo()

	if not Inventario.inventario_cambiato.is_connected(_on_inventario_cambiato):
		Inventario.inventario_cambiato.connect(_on_inventario_cambiato)


func _on_inventario_cambiato() -> void:
	_aggiorna_testo()


func _aggiorna_testo() -> void:
	text = "Soldi: %d" % Inventario.soldi
