extends Node

@onready var tavolo_cliente: Node2D = $TavoloCliente
@onready var cliente: CharacterBody2D = $Cliente

@export var spawn_cliente: Vector2 = Vector2(240, 210)
@export var offset_tavolo: Vector2 = Vector2(0, 18)
@export var offset_uscita: Vector2 = Vector2(210, 210)

var cliente_attivo := false


func _ready() -> void:
	cliente.visible = false
	cliente.process_mode = Node.PROCESS_MODE_DISABLED

	if cliente.has_signal("ordine_consegnato") and not cliente.ordine_consegnato.is_connected(_on_ordine_consegnato):
		cliente.ordine_consegnato.connect(_on_ordine_consegnato)

	await get_tree().create_timer(2.0).timeout
	_spawna_cliente()


func _spawna_cliente() -> void:
	if cliente_attivo:
		return

	var pos_tavolo := tavolo_cliente.global_position + offset_tavolo
	var pos_uscita := offset_uscita

	cliente_attivo = true
	cliente.visible = true
	cliente.process_mode = Node.PROCESS_MODE_INHERIT

	if cliente.has_method("attiva"):
		cliente.attiva(spawn_cliente, pos_tavolo, pos_uscita)
	else:
		cliente.global_position = spawn_cliente


func _on_ordine_consegnato(_nome: String) -> void:
	await get_tree().create_timer(3.0).timeout

	if cliente.has_method("reset_cliente"):
		cliente.reset_cliente()

	cliente.visible = false
	cliente.process_mode = Node.PROCESS_MODE_DISABLED
	cliente_attivo = false

	_spawna_cliente()
