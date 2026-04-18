# Script/inventario.gd
extends Node

# Dizionario: nome_ingrediente → quantità
var ingredienti: Dictionary = {}
# Lista di piatti pronti da consegnare
var piatti_pronti: Array = []

signal inventario_cambiato
signal piatto_aggiunto(nome: String)

func aggiungi_ingrediente(nome: String, quantita: int = 1) -> void:
	ingredienti[nome] = ingredienti.get(nome, 0) + quantita
	emit_signal("inventario_cambiato")
	print("Inventario: ", ingredienti)

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
	print("Piatto pronto: ", nome)

func ritira_piatto() -> String:
	if piatti_pronti.size() > 0:
		return piatti_pronti.pop_front()
	return ""
