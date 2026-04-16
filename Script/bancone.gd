extends Area2D

#func _ready():
	#body_entered.connect(_on_body_entered)
	#body_exited.connect(_on_body_exited)

func _on_body_entered(body):
	if body.has_method("set_vicino_bancone"):
		body.set_vicino_bancone(true)

func _on_body_exited(body):
	if body.has_method("set_vicino_bancone"):
		body.set_vicino_bancone(false)


func _on_area_entered(area: Area2D) -> void:
	pass # Replace with function body.
