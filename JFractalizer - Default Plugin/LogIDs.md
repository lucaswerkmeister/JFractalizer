Log IDs for the Default Plugin (0x00) by Lucas Werkmeister (0x00)
=================================================================

(The prefix `de.lucaswerkmeister.jfractalizer.defaultPlugin.` is implicit.)

Bit distribution in the class byte: `0bpppccccc`, where `p` stands for the package and `c` stands for the class in the package.

* 0: cif
  * 0x00: CifFractal
  * 0x01: CifFractXmlLoader
  * 0x02: CifImageMaker
  * 0x03: CifCanvas
  * 0x04: CifMenuListener
  * 0x05: CifMouseListener
  * 0x06: CifParams
  * 0x07: History
  * 0x08: WaitForCalcThreads
  * 0x09: MandelbrotSet
  * 0x0A: MandelbrotMenuListener
  * 0x0B: MandelbrotImageMaker_CalcAll
  * 0x0C: MandelbrotImageMaker_NoHoles
  * 0x0D: JuliaSet
  * 0x0E: JuliaImageMaker_CalcAll
* 1-3: reserved for future fractals
* 4: palettes
  * 0x00: EditDialogPalette
  * 0x01: PaletteEditDialog
  * 0x02: SelectableColor
  * 0x03: SimplePalette
  * 0x04: SimplePaletteEditDialog
  * 0x05: FractXmlSimplePaletteLoader
  * 0x06: NodePalette
  * 0x07: NodePaletteEditDialog
  * 0x08: FractXmlNodePaletteLoader
  * 0x09: ColorNode
  * 0x0A: HsbRotatePalette
  * 0x0B: HsbRotatePaletteEditDialog
  * 0x0C: FractXmlHsbRotatePaletteLoader
* 5: cameras
  * 0x00: Steadicam
* 6-7: reserved for future other components