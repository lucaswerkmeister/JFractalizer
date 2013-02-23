package de.lucaswerkmeister.jfractalizer;

public interface ZoomableFractal extends Fractal {

	/**
	 * Zooms the fractal to the specified center with the specified zoom factor.
	 * 
	 * @param x
	 *            The x coordinate of the point that is to become the new center, in pixels.
	 * @param y
	 *            The y coordinate of the point that is to become the new center, in pixels.
	 * @param factor
	 *            The zoom factor as (width of new area) / (width of old area). <code>1</code> means no zoom (the image
	 *            is centered on the new center), a greater factor means zoom out, a smaller factor means zoom in.
	 */
	public void zoom(int x, int y, double factor);

	/**
	 * Determines how much the fractal is currently zoomed in from a starting image (not necessarily "the" starting
	 * image because of aspect ratios etc.).
	 * <p>
	 * Calling <code>fractal.zoom(centerX, centerY, fractal.getZoomFactor())</code> should, rounding errors aside,
	 * always yield a starting image.
	 * 
	 * @return The zoom factor as (width/height of start area) / (width/height of current area).
	 */
	public double getZoomFactor();
}
