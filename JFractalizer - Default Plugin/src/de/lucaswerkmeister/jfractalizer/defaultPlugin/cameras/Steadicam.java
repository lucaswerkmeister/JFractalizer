package de.lucaswerkmeister.jfractalizer.defaultPlugin.cameras;

import static de.lucaswerkmeister.jfractalizer.framework.Log.log;

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
import de.lucaswerkmeister.jfractalizer.framework.Log;
import de.lucaswerkmeister.jfractalizer.framework.Output;
import de.lucaswerkmeister.jfractalizer.framework.ZoomableFractal;

public class Steadicam implements Camera {
	private final Set<Output>	outputs				= new HashSet<>();
	private double				zoom				= 1.05;
	private int					frame				= 0;
	private int					modulus				= 1;
	private Thread				zoomer;
	public static final int		LOG_CLASS_PREFIX	= (0 << 24) + (1 << 16) + (((5 << 5) + (0 << 0)) << 8);
	public static final int		LOG_ADDED_OUTPUT	= LOG_CLASS_PREFIX + 0;
	public static final int		LOG_START_FILMING	= LOG_CLASS_PREFIX + 1;
	public static final int		LOG_START_FRAME		= LOG_CLASS_PREFIX + 2;
	public static final int		LOG_END_FRAME		= LOG_CLASS_PREFIX + 3;
	public static final int		LOG_START_WRITE		= LOG_CLASS_PREFIX + 4;
	public static final int		LOG_END_WRITE		= LOG_CLASS_PREFIX + 5;
	public static final int		LOG_GC				= LOG_CLASS_PREFIX + 6;

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
		Log.log(LOG_ADDED_OUTPUT, output);
	}

	@Override
	public void startFilming(final ZoomableFractal fractal) {
		final BlockingQueue<BufferedImage> images = new LinkedBlockingQueue<>();
		final BufferedImage lastImage = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		final int framesCount = (int) Math.ceil(Math.log(fractal.getZoomFactor()) / Math.log(zoom));

		zoomer = new Thread("zoomer") {
			@Override
			public void run() {
				for (Output o : outputs)
					o.setNumbers(getCountdown(framesCount, frame, modulus));
				fractal.startCalculation(); // just to get *some* image; we need the size
				fractal.stopCalculation();
				BufferedImage img = fractal.getImage();
				int centerX = img.getWidth() / 2;
				int centerY = img.getHeight() / 2;
				for (int i = 0; i < frame; i++)
					fractal.zoomToStart(centerX, centerY, zoom);
				double myZoom = Math.pow(zoom, modulus);
				Iterator<Integer> countdown = getCountdown(framesCount, frame, modulus);
				while (countdown.hasNext()) {
					int number = countdown.next();
					log(LOG_START_FRAME, number);
					fractal.startCalculation();
					fractal.awaitCalculation();
					log(LOG_END_FRAME, number);
					try {
						images.put(fractal.getImage());
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					fractal.zoomToStart(centerX, centerY, myZoom);
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
				Iterator<Integer> countdown = getCountdown(framesCount, frame, modulus);
				while (true) {
					BufferedImage image;
					try {
						image = images.take();
						int number = countdown.next();
						if (image == lastImage)
							return;
						log(LOG_START_WRITE, number);
						for (Output o : outputs)
							try {
								o.writeImage(image);
							}
							catch (IOException e) {
								e.printStackTrace();
							}
						log(LOG_END_WRITE, number);
						if (number % 100 == 0) {
							System.gc();
							log(LOG_GC);
						}
					}
					catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			};
		};

		zoomer.setPriority((int) Math.round((Thread.MAX_PRIORITY - Thread.MIN_PRIORITY) * 0.75 + Thread.MIN_PRIORITY));
		zoomer.start();
		log(LOG_START_FILMING, fractal);
		sender.setPriority((int) Math.round((Thread.MAX_PRIORITY - Thread.MIN_PRIORITY) * 0.1 + Thread.MIN_PRIORITY));
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
