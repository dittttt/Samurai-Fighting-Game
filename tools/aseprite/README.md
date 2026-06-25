# Aseprite Pipeline

Detected likely Aseprite install:

```text
E:\Steam\steamapps\common\Aseprite\Aseprite.exe
```

Put source art here:

```text
art/aseprite/*.aseprite
art/aseprite/*.ase
```

Export to Godot:

```bash
bash tools/aseprite/export_aseprite.sh
```

If Aseprite is somewhere else:

```bash
ASEPRITE_PATH="E:/Steam/steamapps/common/Aseprite/Aseprite.exe" bash tools/aseprite/export_aseprite.sh
```

Recommended Wago tags:

```text
idle
walk
run
jump
fall
roll
light_attack
heavy_attack
parry
hit
death
bhunt
```
