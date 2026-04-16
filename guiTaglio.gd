extends PanelContainer

@onready var player = $"../../CharacterBody2D"  # aggiusta il path

func _ready():
	visible = false  # nascosta all'inizio

func _on_salmone_pressed():
	player.taglia_ingrediente("Salmone")

func _on_tonno_pressed():
	player.taglia_ingrediente("Tonno")

func _on_chiudi_pressed():
	visible = false
