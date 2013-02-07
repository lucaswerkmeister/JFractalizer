#!/bin/sh
java -cp "bin:../JFractalizer - Default Plugin/bin" de.lucaswerkmeister.jfractalizer.Core --input fractal=de.lucaswerkmeister.jfractalizer.defaultPlugin.cif.MandelbrotProvider palette=de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes.NodePalette --output format=raw-BGR stdout | x264 --demuxer raw --input-csp bgr --input-res 960x540 --input-depth 8 -o test.mp4 /dev/stdin 
