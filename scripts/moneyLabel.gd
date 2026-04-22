# feat: mostra anche preparazioni e dettaglio piatti nell'inventario
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
	var osomaki_intero: int = int(Inventario.preparazioni.get("Osomaki intero", 0))

	var nigiri_salmone := 0
	var nigiri_gambero := 0
	var osomaki := 0

	for piatto in Inventario.piatti_pronti:
		match piatto:
			"Nigiri salmone":
				nigiri_salmone += 1
			"Nigiri gambero":
				nigiri_gambero += 1
			"Osomaki":
				osomaki += 1

	var piatti_totali: int = Inventario.piatti_pronti.size()

	text = "Inventario\nSalmone: %d\nGambero: %d\nOsomaki intero: %d\n\nPiatti pronti: %d\nNigiri salmone: %d\nNigiri gambero: %d\nOsomaki: %d" % [
		salmone,
		gambero,
		osomaki_intero,
		piatti_totali,
		nigiri_salmone,
		nigiri_gambero,
		osomaki
	]
