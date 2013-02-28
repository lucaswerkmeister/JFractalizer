java -Xmx1024m -cp "bin;..\JFractalizer - Default Plugin\bin" de.lucaswerkmeister.jfractalizer.core.Core --ui no-gui --input file=tmp.fractXml --film camera=de.lucaswerkmeister.jfractalizer.defaultPlugin.cameras.Steadicam --output format=raw-BGR file=tmp
x264 --demuxer raw --input-csp bgr --input-res 960x540 --input-depth 8 -o test.mp4 tmp
pause