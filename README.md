The **JFractalizer** is a fractal explorer, written in Java. The inspiration, and the idea for the name, comes from the freeware [Fractalizer](http://www.fractalizer.de/en), and the long-time goal is to include all of its features.
The JFractalizer is developed by me, Lucas Werkmeister, as a free-time training and for-fun project, but anyone can feel free to contribute. It is published under the GNU GPLv3.

How to run
----------

Go to [my website](http://code.lucaswerkmeister.de/jfractalizer) for compiled and runnable versions.

Features
--------

Explore the Mandelbrot or Julia Set and enjoy high-speed calculation (tries to utilize all processor cores, by dividing the image into parts - 2x2 on a quad-core processor) and high image quality (SuperSampling AntiAliasing).
Save and load your favourite setups, including the color palette.
Save the image you're seeing in various formats.

Screenshots following soon!

Plugins
-------

The JFractalizer is split into two parts:

1. The *Core* is the program that is started when running the JFractalizer. It provides the GUI and loads plugins.
2. The *Default Plugin* contains the implementation of the Mandelbrot fractal, as well as two ColorPalette implementations (ColorPalettes are used by fractals to color the images).

Everyone can write other plugins. I will write a more detailed guide on how to do this later; for now I'll just tell you to implement the `Fractal` or `ColorPalette` interfaces and prepare them for the Java ServiceLoader.

Planned features
------------------

* Movie support
  * AutoMovie for the Mandelbrot fractal
* More fractals
  * Buddhabrot
* Possibly rewrite the GUI for JavaFX once Java 8 is out

Current state
-------------

This project is currently on hold; I do want to come back to it at some point, but at the moment I don’t have the time (mostly because I’m involved in other projects). I definitely want to add a GUI for the filming feature (at the moment, it’s only accessible through the CLI), and I’d love to have AutoMovie, as well more fractals.

If you want to help out, feel free to do so! (For example, screenshots are “coming soon” for over two years now :) )
