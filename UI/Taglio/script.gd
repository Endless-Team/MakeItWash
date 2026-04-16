extends PanelContainer

@onready var player = get_node("/root/Sushi/Player")

func _ready():
	visible = false

func _on_salmone_pressed():
	player.taglia_ingrediente("Salmone")

func _on_tonno_pressed():
	player.taglia_ingrediente("Tonno")

func _on_chiudi_pressed():
	visible = false
