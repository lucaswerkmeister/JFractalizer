Log IDs for the Default Plugin (0x01) by Lucas Werkmeister (0x00)
=================================================================

(The prefix package `de.lucaswerkmeister.jfractalizer.defaultPlugin` is implicit.)

Bit distribution in the class byte: `0bpppccccc`, where `p` stands for the package and `c` stands for the class in the package.

* 0: cif
  * 0x00: CifFractal
    * 0x00: Changed image type
    * 0x01: Saving
    * 0x02: Set color palette
    * 0x03: Start calculation
    * 0x04: Stop calculation
    * 0x05: Initialized menu
    * 0x06: Zoom
    * 0x07: Zoom to start
    * 0x08: Shutdown
  * 0x01: CifFractXmlLoader
    * None yet
  * 0x02: CifImageMaker
    * None yet
  * 0x03: CifCanvas
    * 0x00: Go to selected area
  * 0x04: CifMenuListener
    * 0x00: Edit boundaries
	* 0x01: Edit additional parameters
	* 0x02: Recalculate
	* 0x03: Undo
	* 0x04: Redo
  * 0x05: CifMouseListener
    * None yet
  * 0x06: CifParams
    * None yet
  * 0x07: History
    * None yet
  * 0x08: [Removed]
  * 0x09: MandelbrotSet
    * 0x00: Initialized context menu
  * 0x0A: MandelbrotMenuListener
    * 0x00: Switch to Julia set
  * 0x0B: MandelbrotImageMaker_CalcAll
    * None yet
  * 0x0C: MandelbrotImageMaker_NoHoles
    * None yet
  * 0x0D: JuliaSet
    * 0x00: Switched to Julia set
  * 0x0E: JuliaImageMaker_CalcAll
    * None yet
* 1-3: reserved for future fractals
* 4: palettes
  * 0x00: EditDialogPalette
    * 0x00: Initialized menu
    * 0x01: Showing edit dialog
    * 0x02: Edited palette
  * 0x01: PaletteEditDialog
  * 0x02: SelectableColor
    * 0x00: Showing edit dialog
    * 0x01: Edited color
  * 0x03: SimplePalette
    * 0x00: Saving
  * 0x04: SimplePaletteEditDialog
    * None yet
  * 0x05: FractXmlSimplePaletteLoader
    * None yet
  * 0x06: NodePalette
    * 0x00: Saving
  * 0x07: NodePaletteEditDialog
    * None yet
  * 0x08: FractXmlNodePaletteLoader
    * None yet
  * 0x09: ColorNode
    * 0x00: Updated
  * 0x0A: HsbRotatePalette
    * 0x00: Saving
  * 0x0B: HsbRotatePaletteEditDialog
    * None yet
  * 0x0C: FractXmlHsbRotatePaletteLoader
    * None yet
* 5: cameras
  * 0x00: Steadicam
    * 0x00: Added output
    * 0x01: Start filming
    * 0x02: Start calculation of frame
    * 0x03: End calculation of frame
    * 0x04: Start write of frame
    * 0x05: End write of frame
* 6-7: reserved for future other components