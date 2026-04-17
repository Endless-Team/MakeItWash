extends Area2D

# Esporta il tipo così lo setti dall'Ispettore per ogni bancone
@export var tipo: String = "taglio"  # oppure "assemblaggio"

func _ready():
	body_entered.connect(_on_body_entered)
	body_exited.connect(_on_body_exited)

func _on_body_entered(body):
	if body.has_method("set_vicino_bancone"):
		body.set_vicino_bancone(true, tipo)  # passa anche il tipo

func _on_body_exited(body):
	if body.has_method("set_vicino_bancone"):
		body.set_vicino_bancone(false, "")
