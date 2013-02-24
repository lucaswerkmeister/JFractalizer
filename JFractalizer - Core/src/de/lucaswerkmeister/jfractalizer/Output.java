package de.lucaswerkmeister.jfractalizer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;

public abstract class Output {
	protected final String		format;
	private Iterator<Integer>	numbers	= new Iterator<Integer>() {
											private int	current	= 1;

											@Override
											public boolean hasNext() {
												return true;
											}

											@Override
											public Integer next() {
												return current++;
											}

											@Override
											public void remove() {
												throw new UnsupportedOperationException();
											}
										};

	protected Output(String format) {
		if (!Arrays.asList("png", "jpg", "raw-ARGB", "raw-BGR").contains(format))
			throw new IllegalCommandLineException("Unknown output format \"" + format + "\"!");
		this.format = format;
	}

	protected Output(String format, Iterator<Integer> numbers) {
		this(format);
		this.numbers = numbers;
	}

	public abstract void writeImage(BufferedImage BufferedImage) throws IOException;

	protected void write(BufferedImage image, OutputStream stream) throws IOException {
		switch (format) {
			case "png":
			case "jpg":
				ImageIO.write(image, format, stream);
				break;
			case "raw-ARGB": {
				BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
				newImage.getGraphics().drawImage(image, 0, 0, null);
				DataBufferInt buffer = (DataBufferInt) newImage.getRaster().getDataBuffer();
				int[] data = buffer.getData();
				ByteBuffer bbuf = ByteBuffer.allocate(data.length * 4);
				bbuf.asIntBuffer().put(data);
				stream.write(bbuf.array());
				break;
			}
			case "raw-BGR": {
				BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(),
						BufferedImage.TYPE_3BYTE_BGR);
				newImage.getGraphics().drawImage(image, 0, 0, null);
				DataBufferByte buffer = (DataBufferByte) newImage.getRaster().getDataBuffer();
				byte[] data = buffer.getData();
				stream.write(data);
				break;
			}
		}
	}

	public Iterator<Integer> getNumbers() {
		return numbers;
	}

	public void setNumbers(Iterator<Integer> numbers) {
		this.numbers = numbers;
	}
}