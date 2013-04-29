set videofile=%1
if "%1"=="" set videofile=video.mp4
rmdir /S /Q tmp
mkdir tmp
java -Xmx2048m -cp "bin;..\JFractalizer - Default Plugin\bin" de.lucaswerkmeister.jfractalizer.core.Core --ui no-gui --input file=tmp.fractXml --film camera=de.lucaswerkmeister.jfractalizer.defaultPlugin.cameras.Steadicam --fractArgs superSamplingFactor=10 --camArgs zoom=1.02 --output format=raw-BGR files=tmp\tmp?.raw --log stdout hide=all show=106499 --log file=log show=all
find ./tmp/tmp*.raw -exec cat {} + > tmpfile
x264 --demuxer raw --input-csp bgr --input-res 960x540 --input-depth 8 -o %videofile% tmpfile
rmdir /S /Q tmp
rm tmpfile
pause
