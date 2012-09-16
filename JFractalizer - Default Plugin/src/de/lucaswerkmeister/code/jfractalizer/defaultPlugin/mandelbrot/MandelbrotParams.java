package de.lucaswerkmeister.code.jfractalizer.defaultPlugin.mandelbrot;

final class MandelbrotParams
{
	public final double	minReal, maxReal, minImag, maxImag;
	public final byte	superSamplingFactor;
	public final int	maxPasses;

	/**
	 * Creates a new MandelbrotParams with the specified arguments.
	 * 
	 * @param minReal
	 *            The left boundary.
	 * @param maxReal
	 *            The right boundary.
	 * @param minImag
	 *            The bottom boundary.
	 * @param maxImag
	 *            The top boundary.
	 * @param maxPasses
	 *            The number of iterations after which a point is considered part of the Mandelbrot set.
	 * @param superSamplingFactor
	 *            The SuperSampling-factor.
	 */
	MandelbrotParams(double minReal, double maxReal, double minImag, double maxImag, int maxPasses, byte superSamplingFactor)
	{
		this.minReal = minReal;
		this.maxReal = maxReal;
		this.minImag = minImag;
		this.maxImag = maxImag;
		this.maxPasses = maxPasses;
		this.superSamplingFactor = superSamplingFactor;
	}
}
