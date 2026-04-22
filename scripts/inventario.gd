extends Node

var ingredienti: Dictionary = {}
var piatti_pronti: Array = []
var soldi: int = 100

const COSTI_INGREDIENTI = {
	"Salmone": 4,
	"Gambero": 5
}

const RICAVO_CONSEGNA = 10

signal inventario_cambiato
signal piatto_aggiunto(nome: String)
signal soldi_cambiati(valore: int)


func _ready() -> void:
	emit_signal("inventario_cambiato")
	emit_signal("soldi_cambiati", soldi)


func aggiungi_ingrediente(nome: String, quantita: int = 1) -> bool:
	if not COSTI_INGREDIENTI.has(nome):
		return false

	var costo_unitario = COSTI_INGREDIENTI[nome]
	var costo_totale = costo_unitario * quantita

	if soldi < costo_totale:
		return false

	soldi -= costo_totale
	ingredienti[nome] = ingredienti.get(nome, 0) + quantita

	emit_signal("inventario_cambiato")
	emit_signal("soldi_cambiati", soldi)
	return true


func rimuovi_ingrediente(nome: String, quantita: int = 1) -> bool:
	if ingredienti.get(nome, 0) < quantita:
		return false

	ingredienti[nome] -= quantita
	if ingredienti[nome] <= 0:
		ingredienti.erase(nome)

	emit_signal("inventario_cambiato")
	return true


func ha_ingrediente(nome: String, quantita: int = 1) -> bool:
	return ingredienti.get(nome, 0) >= quantita


func aggiungi_piatto(nome: String) -> void:
	piatti_pronti.append(nome)
	emit_signal("inventario_cambiato")
	emit_signal("piatto_aggiunto", nome)


func ritira_piatto() -> String:
	if piatti_pronti.size() == 0:
		return ""

	var piatto = piatti_pronti.pop_front()
	emit_signal("inventario_cambiato")
	return piatto


func accredita_vendita() -> void:
	soldi += RICAVO_CONSEGNA
	emit_signal("soldi_cambiati", soldi)


func get_soldi() -> int:
	return soldi
