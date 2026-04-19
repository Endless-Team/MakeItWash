extends Node

var ingredienti: Dictionary = {}
var piatti_pronti: Array = []

var soldi: int = 100

const COSTI_INGREDIENTI := {
	"salmone": 4,
	"gambero": 5
}

const RICAVI_PIATTI := {
	"Nigiri salmone": 12,
	"Nigiri gambero": 14
}

signal inventario_cambiato
signal piatto_aggiunto(nome: String)
signal soldi_cambiati(valore: int)


func _ready() -> void:
	emit_signal("soldi_cambiati", soldi)


func aggiungi_ingrediente(nome: String, quantita: int = 1) -> bool:
	var costo_unitario = COSTI_INGREDIENTI.get(nome, 0)
	var costo_totale = costo_unitario * quantita

	if soldi < costo_totale:
		return false

	soldi -= costo_totale
	ingredienti[nome] = ingredienti.get(nome, 0) + quantita
	emit_signal("inventario_cambiato")
	emit_signal("soldi_cambiati", soldi)
	return true


func rimuovi_ingrediente(nome: String, quantita: int = 1) -> bool:
	if ingredienti.get(nome, 0) >= quantita:
		ingredienti[nome] -= quantita
		if ingredienti[nome] == 0:
			ingredienti.erase(nome)
		emit_signal("inventario_cambiato")
		return true
	return false


func ha_ingrediente(nome: String, quantita: int = 1) -> bool:
	return ingredienti.get(nome, 0) >= quantita


func aggiungi_piatto(nome: String) -> void:
	piatti_pronti.append(nome)
	emit_signal("piatto_aggiunto", nome)


func ritira_piatto() -> String:
	if piatti_pronti.size() > 0:
		return piatti_pronti.pop_front()
	return ""


func accredita_vendita(nome_piatto: String) -> void:
	soldi += RICAVI_PIATTI.get(nome_piatto, 0)
	emit_signal("soldi_cambiati", soldi)


func get_soldi() -> int:
	return soldi
