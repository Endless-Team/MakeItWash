extends Panel

func _ready() -> void:
	var style := StyleBoxFlat.new()
	style.bg_color = Color("1b1b1be6")
	style.border_color = Color("6fcf97")
	style.set_border_width_all(2)
	style.set_corner_radius_all(16)
	style.shadow_color = Color("00000088")
	style.shadow_size = 6
	add_theme_stylebox_override("panel", style)
