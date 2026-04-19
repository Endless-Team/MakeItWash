extends Node

@onready var cliente: CharacterBody2D = $Cliente
@onready var spawn_cliente: Node2D = $SpawnCliente
@onready var punto_tavolo_cliente: Node2D = $PuntoTavoloCliente
@onready var uscita_cliente: Node2D = $UscitaCliente

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

	cliente_attivo = true
	cliente.visible = true
	cliente.process_mode = Node.PROCESS_MODE_INHERIT
	cliente.attiva(
		spawn_cliente.global_position,
		punto_tavolo_cliente.global_position,
		uscita_cliente.global_position
	)


func _on_ordine_consegnato(_nome: String) -> void:
	await get_tree().create_timer(3.0).timeout

	cliente.reset_cliente()
	cliente.visible = false
	cliente.process_mode = Node.PROCESS_MODE_DISABLED
	cliente_attivo = false

	_spawna_cliente()
