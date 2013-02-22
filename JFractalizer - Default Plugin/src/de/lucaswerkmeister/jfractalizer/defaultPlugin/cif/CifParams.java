package de.lucaswerkmeister.jfractalizer.defaultPlugin.cif;

public final class CifParams {
	public final double	minReal, maxReal, minImag, maxImag;
	public final byte	superSamplingFactor;
	public final int	maxPasses;

	/**
	 * Creates a new CIFParams with the specified arguments.
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
	 *            The number of iterations after which a point is considered part of the fractal.
	 * @param superSamplingFactor
	 *            The SuperSampling-factor.
	 */
	public CifParams(final double minReal, final double maxReal, final double minImag, final double maxImag,
			final int maxPasses, final byte superSamplingFactor) {
		this.minReal = minReal;
		this.maxReal = maxReal;
		this.minImag = minImag;
		this.maxImag = maxImag;
		this.maxPasses = maxPasses;
		this.superSamplingFactor = superSamplingFactor;
	}

	public CifParams copyChangeMinReal(double minReal) {
		return new CifParams(minReal, maxReal, minImag, maxImag, maxPasses, superSamplingFactor);
	}

	public CifParams copyChangeMaxReal(double maxReal) {
		return new CifParams(minReal, maxReal, minImag, maxImag, maxPasses, superSamplingFactor);
	}

	public CifParams copyChangeMinImag(double minImag) {
		return new CifParams(minReal, maxReal, minImag, maxImag, maxPasses, superSamplingFactor);
	}

	public CifParams copyChangeMaxImag(double maxImag) {
		return new CifParams(minReal, maxReal, minImag, maxImag, maxPasses, superSamplingFactor);
	}

	public CifParams copyChangeMaxPasses(int maxPasses) {
		return new CifParams(minReal, maxReal, minImag, maxImag, maxPasses, superSamplingFactor);
	}

	public CifParams copyChangeSuperSamplingFactor(byte superSamplingFactor) {
		return new CifParams(minReal, maxReal, minImag, maxImag, maxPasses, superSamplingFactor);
	}
}
