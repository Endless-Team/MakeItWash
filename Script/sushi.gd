extends Node

const ClienteScene = preload("res://Scene/cliente.tscn")

@onready var tavolo_cliente: Node2D = $TavoloCliente

@export var spawn_cliente: Vector2 = Vector2(240, 210)
@export var offset_tavolo: Vector2 = Vector2(0, 18)
@export var offset_uscita: Vector2 = Vector2(210, 210)

var cliente_attivo: CharacterBody2D = null

func _ready() -> void:
	await get_tree().create_timer(2.0).timeout
	_spawna_cliente()

func _spawna_cliente() -> void:
	if cliente_attivo != null:
		return

	var c = ClienteScene.instantiate()
	add_child(c)

	c.position = spawn_cliente

	var pos_tavolo := tavolo_cliente.position + offset_tavolo
	var pos_uscita := offset_uscita

	c.inizia(pos_tavolo, pos_uscita)
	c.ordine_consegnato.connect(_on_ordine_consegnato)

	cliente_attivo = c

func _on_ordine_consegnato(_nome: String) -> void:
	await get_tree().create_timer(3.0).timeout
	cliente_attivo = null
	_spawna_cliente()
