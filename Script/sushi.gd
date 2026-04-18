extends Node

const ClienteScene = preload("res://Scene/cliente.tscn")

@export var spawn_cliente: Vector2 = Vector2(200, -50)
@export var pos_tavolo: Vector2 = Vector2(200, 150)
@export var pos_uscita: Vector2 = Vector2(200, 500)

var cliente_attivo: Node = null

func _ready() -> void:
	await get_tree().create_timer(2.0).timeout
	_spawna_cliente()

func _spawna_cliente() -> void:
	if cliente_attivo != null:
		return
	var c = ClienteScene.instantiate()
	get_parent().add_child(c)                        # ← era get_parent().add_child(c)
	c.global_position = spawn_cliente
	c.inizia(pos_tavolo, pos_uscita)
	c.ordine_consegnato.connect(_on_ordine_consegnato)
	cliente_attivo = c

func _on_ordine_consegnato(_nome: String) -> void:
	await get_tree().create_timer(3.0).timeout
	cliente_attivo = null
	_spawna_cliente()
