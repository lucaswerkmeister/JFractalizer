#!/bin/sh
videofile=$1
[ "$videofile" = "" ] && videofile="video.mp4"
rm -Rf /tmp/jfractalizer
mkdir /tmp/jfractalizer
#use taskset -c 0,1 java ... to run java on two cores
java -Xmx512m -cp "bin:../JFractalizer - Default Plugin/bin" de.lucaswerkmeister.jfractalizer.core.Core --ui no-gui --input file=tmp.fractXml --film camera=de.lucaswerkmeister.jfractalizer.defaultPlugin.cameras.Steadicam --output format=raw-BGR files=/tmp/jfractalizer/tmp?.raw --log stdout hide=all show=106499 --log file=log show=all
find /tmp/jfractalizer/tmp*.raw -exec cat {} + > /tmp/jfractalizer/tmpfile
x264 --demuxer raw --input-csp bgr --input-res 1920x1080 --input-depth 8 --profile high -o $videofile /tmp/jfractalizer/tmpfile
#rm -Rf /tmp/jfractalizer
#rm -Rf /tmp/jfractalizer/tmpfile
read -n1 -r -p "Finished."
