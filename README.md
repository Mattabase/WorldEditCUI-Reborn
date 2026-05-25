# WorldEditCUI Reborn

A modern, multiloader (NeoForge & Fabric) client-side mod that provides visual selection rendering and interactive gizmo handles for WorldEdit.

## Features
* **Visual Selection Box**: Shows a highlighted, translucent box over your WorldEdit selection with a 1x1 grid to easily see what blocks are inside.
* **Interactive Gizmos**: Axiom-style drag handles on each face of the selection for easy resizing.
* **Center Gizmo**: A center handle to move the entire selection.
* **Focus-Based Input**: Right-click a handle to focus it, then use your scroll wheel or arrow keys to expand/contract/shift the selection.
* **See-Through Rendering**: Gizmos and selection edges render through terrain, so you can always see and interact with them even if they are buried underground.
* **In-Game Configuration**: Customize colors, invert scroll/arrow key directions, and toggle see-through rendering via the in-game mod menu (requires Mod Menu on Fabric).

## Installation
1. Install [WorldEdit](https://modrinth.com/mod/worldedit) on your server or singleplayer world.
2. Drop the `WorldEditCUI-Reborn` jar into your `mods` folder.
3. (Fabric only) Install [Mod Menu](https://modrinth.com/mod/modmenu) to access the in-game configuration screen.

## Usage
1. Make a selection using your WorldEdit wand (wooden axe) or commands (`//pos1`, `//pos2`).
2. The visual selection box and gizmo handles will appear.
3. Look at a gizmo handle and **Right-Click** to focus it.
4. **Scroll Up/Down** or use **Up/Down Arrow Keys** to adjust the selection.
5. **Right-Click** again or look away and right-click to unfocus.

## Configuration
You can configure the mod in-game via the NeoForge Mods menu or Fabric Mod Menu. Alternatively, you can edit the `config/worldeditcui.json` file directly:
* `invertResizeScrollDirection`: Reverses the scroll wheel movement direction for resizing (expanding/contracting).
* `invertResizeArrowKeys`: Reverses the arrow key movement direction for resizing.
* `invertMoveScrollDirection`: Reverses the scroll wheel movement direction for moving the selection (center gizmo).
* `invertMoveArrowKeys`: Reverses the arrow key movement direction for moving the selection.
* `selectionFillColor`: ARGB hex color for the selection box fill.
* `selectionEdgeColor`: ARGB hex color for the selection box edges.
* `selectionGridColor`: ARGB hex color for the selection box grid.
* `renderSelectionEdgesThroughTerrain`: Toggles whether the selection box edges and grid render through terrain.
* `hideGizmoChatFeedback`: Hides the chat spam from WorldEdit when using the gizmos to adjust the selection.

## Building from Source
This project uses a multiloader setup.
```bash
# Build NeoForge
./gradlew :neoforge:build

# Build Fabric
./gradlew :fabric:build
```
The compiled jars will be located in `neoforge/build/libs/` and `fabric/build/libs/`.
