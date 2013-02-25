package de.lucaswerkmeister.jfractalizer.defaultPlugin.cameras;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.lucaswerkmeister.jfractalizer.Camera;
import de.lucaswerkmeister.jfractalizer.IllegalCommandLineException;
import de.lucaswerkmeister.jfractalizer.Output;
import de.lucaswerkmeister.jfractalizer.ZoomableFractal;

public class Steadicam implements Camera {
	private final Set<Output>	outputs	= new HashSet<>();
	private double				zoom	= 1.05;
	private volatile boolean	working	= false;

	@Override
	public String getName() {
		return "Steadicam";
	}

	@Override
	public void handleCommandLineOption(String option, String optionName, String optionContent) {
		if (optionName.equals("zoom"))
			zoom = Double.parseDouble(optionContent);
		else
			throw new IllegalCommandLineException("Unknown option \"" + option + "\" for Steadicam!");
	}

	@Override
	public void addOutput(Output output) {
		outputs.add(output);
	}

	@Override
	public void startFilming(final ZoomableFractal fractal) {
		final BlockingQueue<BufferedImage> images = new LinkedBlockingQueue<>();

		// The Steadicam uses three threads:
		// The zoomer zooms on the fractal and puts images into the images queue.
		// The sender takes images from the queue and sends them to the outputs.
		// The waiter resets the "working" flag, which is read by the sender, after the zoomer has finished.
		final Thread zoomer = new Thread() {
			@Override
			public void run() {
				final int framesCount = (int) Math.ceil(Math.log(fractal.getZoomFactor()) / Math.log(zoom));
				for (Output o : outputs)
					o.setNumbers(getCountdown(framesCount));
				BufferedImage img = fractal.getImage();
				int centerX = img.getWidth() / 2;
				int centerY = img.getHeight() / 2;
				Iterator<Integer> countdown = getCountdown(framesCount);
				while (countdown.hasNext()) {
					fractal.zoom(centerX, centerY, zoom);
					fractal.startCalculation();
					fractal.awaitCalculation();
					try {
						images.put(fractal.getImage());
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					countdown.next();
				}
			}
		};
		final Thread sender = new Thread() {
			@Override
			public void run() {
				while (working || !images.isEmpty()) {
					BufferedImage image;
					try {
						image = images.take();
						for (Output o : outputs)
							try {
								o.writeImage(image);
							}
							catch (IOException e) {
								e.printStackTrace();
							}
					}
					catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			};
		};
		final Thread waiter = new Thread() {
			@Override
			public void run() {
				working = true;
				try {
					zoomer.join();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				working = false;
			};
		};

		zoomer.start();
		waiter.start();
		sender.start();
	}

	private Iterator<Integer> getCountdown(final int start) {
		return new Iterator<Integer>() {
			int	current	= start;

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Integer next() {
				return current--;
			}

			@Override
			public boolean hasNext() {
				return current > 1;
			}
		};
	}
}
