package de.lucaswerkmeister.code.jfractalizer.defaultPlugin.cif;

import de.lucaswerkmeister.code.jfractalizer.FractXmlLoader;

public class MandelbrotProvider extends CifProvider
{
	public MandelbrotProvider()
	{
		canvas = new CifCanvas<>(this, MandelbrotImageMaker_NoHoles.class);
		menuListener = new CifMenuListener(this, canvas);
	}

	@Override
	public FractXmlLoader getFractXmlLoader()
	{
		return new CifFractXmlLoader(MandelbrotProvider.class);
	}

	@Override
	public String getName()
	{
		return "Mandelbrot Set";
	}
}
