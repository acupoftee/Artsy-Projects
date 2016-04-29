package net.bethydiakabana.misc.asciiart;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.URL;

import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * <tt>ASCIIArt</tt> renders ASCII art with the following steps:
 * <ol>
 * <li>Loads an image from a URL.</li>
 * <li>Iterates through the pixels and calculates the average gray scale.</li>
 * <li>Selects the appropriate ASCII character depending on the gray value.</li>
 * </ol>
 * 
 * @author Bethy Diakabana
 * @since 9/15/2015
 * 
 */
public class ASCIIArt {
	private BufferedImage image;
	/**
	 * Instantiates a new object that will be converted into ASCII art
	 * <p>Note: If the URL contains a .png image, the compression will treat a whitespace
	 * as a dark colour.</p>
	 * 
	 * @param url
	 *            the URL containing the image file
	 * @throws IOException
	 *             if there is an error reading the file
	 */
	public ASCIIArt(String url) throws IOException {
		//image = loadImage(url);
		URL imageURL = new URL(url);
		image = ImageIO.read(imageURL);
	} // end constructor

	/**
	 * Calculates the gray scale of a pixel in the image to be converted and
	 * prints the appropriate ASCII character for the gray value. The RGB is
	 * calculated according to the average human translation of color.
	 */
	public boolean printASCIICharacter() {
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				Color pixelColor = new Color(image.getRGB(j, i));
				int pixelGrayValue = (int) ((pixelColor.getRed() * 0.3)
						+ (pixelColor.getGreen() * 0.59) + (pixelColor
						.getBlue() * 0.11));
				
				System.out.print(getASCIICharacter(pixelGrayValue));
			} // end for
			System.out.println();
		} // end for
		return true;
	} // end method getGrayScale

	/**
	 * Returns the ASCII character associated with the given gray value of a
	 * picture
	 * 
	 * @param pixelGrayValue
	 *            the gray value of a pixel in an image
	 * @return the ASCII color associated with the shade in the gray scale
	 */
	private String getASCIICharacter(int pixelGrayValue) {
		String[] characters = { "M", "N", "F", "V", "|", "*", ":", "." };
		String whitespace = " ";
		// " .:-=+*#%@" keep these in mind

		// refer to http://larc.unt.edu/ian/art/ascii/shader/
		// TODO find a more efficient algorithm for this!
		if (pixelGrayValue <= 144)
			return characters[0];

		else if (pixelGrayValue > 144 && pixelGrayValue <= 156)
			return characters[1];

		else if (pixelGrayValue > 156 && pixelGrayValue <= 168)
			return characters[2];

		else if (pixelGrayValue > 168 && pixelGrayValue <= 181)
			return characters[3];

		else if (pixelGrayValue > 181 && pixelGrayValue <= 197)
			return characters[4];

		else if (pixelGrayValue > 181 && pixelGrayValue <= 218)
			return characters[5];

		else if (pixelGrayValue > 218 && pixelGrayValue <= 237)
			return characters[6];

		else if (pixelGrayValue > 237 && pixelGrayValue <= 245)
			return characters[7];

		else
			return whitespace;
	} // end method getASCIICharacter
} // end class ASCIIArt
