extends Node

signal line_started(speaker: String, text: String)
signal dialogue_finished(id: String)

var active_dialogue_id := ""
var active_lines: Array = []
var line_index := 0

func start_dialogue(dialogue_id: String, lines: Array) -> void:
    active_dialogue_id = dialogue_id
    active_lines = lines
    line_index = -1
    next_line()

func next_line() -> void:
    line_index += 1
    if line_index >= active_lines.size():
        dialogue_finished.emit(active_dialogue_id)
        active_dialogue_id = ""
        active_lines = []
        return
    var line: Dictionary = active_lines[line_index]
    line_started.emit(str(line.get("speaker", "")), str(line.get("text", "")))
