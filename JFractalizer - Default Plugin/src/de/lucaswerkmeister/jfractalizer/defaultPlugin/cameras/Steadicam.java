package de.lucaswerkmeister.jfractalizer.defaultPlugin.cameras;

import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.lucaswerkmeister.jfractalizer.framework.Camera;
import de.lucaswerkmeister.jfractalizer.framework.IllegalCommandLineException;
import de.lucaswerkmeister.jfractalizer.framework.Output;
import de.lucaswerkmeister.jfractalizer.framework.ZoomableFractal;

public class Steadicam implements Camera {
	private final Set<Output>	outputs	= new HashSet<>();
	private double				zoom	= 1.05;
	private int					frame	= 0;
	private int					modulus	= 1;
	private Thread				zoomer;

	@Override
	public String getName() {
		return "Steadicam";
	}

	@Override
	public void handleCommandLineOption(String option, String optionName, String optionContent) {
		switch (optionName) {
			case "zoom":
				try {
					zoom = Double.parseDouble(optionContent);
					return;
				}
				catch (NumberFormatException e) {
					throw new IllegalCommandLineException("Illegal value \"" + optionContent
							+ "\"for option \"zoom\", was expecting a number!", e);
				}
			case "frames":
				if (optionContent.contains("%")) {
					String[] parts = optionContent.split("%");
					if (parts.length > 2)
						throw new IllegalCommandLineException(
								"Wrong usage of option \"frames\", must contain exactly one '%' character!");
					try {
						frame = Integer.parseInt(parts[0]);
						modulus = Integer.parseInt(parts[1]);
						break;
					}
					catch (NumberFormatException e) {
						throw new IllegalCommandLineException("Could not parse \"frames\" option!", e);
					}
				}
				else
					throw new IllegalCommandLineException(
							"Wrong usage of option \"frames\", must contain a '%' character!");
			default:
				throw new IllegalCommandLineException("Unknown option \"" + option + "\" for Steadicam!");
		}
	}

	@Override
	public void addOutput(Output output) {
		outputs.add(output);
	}

	@Override
	public void startFilming(final ZoomableFractal fractal) {
		final BlockingQueue<BufferedImage> images = new LinkedBlockingQueue<>();
		final BufferedImage lastImage = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);

		zoomer = new Thread("zoomer") {
			@Override
			public void run() {
				final int framesCount = (int) Math.ceil(Math.log(fractal.getZoomFactor()) / Math.log(zoom));
				for (Output o : outputs)
					o.setNumbers(getCountdown(framesCount, frame, modulus));
				fractal.startCalculation();
				fractal.stopCalculation();
				BufferedImage img = fractal.getImage();
				int centerX = img.getWidth() / 2;
				int centerY = img.getHeight() / 2;
				for (int i = 0; i < frame; i++)
					fractal.zoomToStart(centerX, centerY, zoom);
				double myZoom = Math.pow(zoom, modulus);
				Iterator<Integer> countdown = getCountdown(framesCount, frame, modulus);
				while (countdown.hasNext()) {
					try {
						images.put(fractal.getImage());
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					fractal.zoomToStart(centerX, centerY, myZoom);
					fractal.startCalculation();
					fractal.awaitCalculation();
					countdown.next();
				}
				try {
					images.put(lastImage);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		final Thread sender = new Thread("sender") {
			@Override
			public void run() {
				while (true) {
					BufferedImage image;
					try {
						image = images.take();
						if (image == lastImage)
							return;
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

		zoomer.start();
		sender.start();
	}

	private static Iterator<Integer> getCountdown(final int start, final int offset, final int modulus) {
		return new Iterator<Integer>() {
			int	current	= start - offset + modulus;

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Integer next() {
				return current -= modulus;
			}

			@Override
			public boolean hasNext() {
				return current > modulus;
			}
		};
	}

	@Override
	public void awaitCalculation() {
		if (zoomer != null)
			try {
				zoomer.join();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void addCalculationFinishedListener(final ActionListener listener) {
		new Thread() {
			@Override
			public void run() {
				awaitCalculation();
				listener.actionPerformed(null);
			}
		}.start();
	}
}
