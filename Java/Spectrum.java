import java.applet.Applet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

/**
 * <tt>SlidingSpectrum</tt> displays a scrolling rainbow
 * @author Bethy Diakabana
 * @since 11/30/2015
 *
 */
public class Spectrum extends Applet implements Runnable {

	private static final long serialVersionUID = 4060259661814647186L;

	Thread runner; // thread to produce the animation

	Image OSC; // the off-screen canvas

	public void start() {
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		} // end if
	} // end method start

	public void paint(Graphics g) { // paint method just copies canvas to applet
		if (OSC != null)
			g.drawImage(OSC, 0, 0, this);
	} // end method paint

	public void update(Graphics g) { 
		paint(g);
	} // end method update

	public void run() { // run method for animation thread
		int w = getWidth(); // get the width and height of the applet
		int h = getHeight();
		OSC = createImage(w, h); // create an off screen canvas of the same size
		Graphics g = OSC.getGraphics(); // a graphics context for drawing to
										// canvas

		Color[] colors = new Color[100]; // the colors of the spectrum
		int c = 0; // for indexing the color array; c goes from 0 to 99 then
					// back to 0

		for (int i = 0; i < 100; i++) {
			// create the colors
			colors[i] = Color.getHSBColor((float) (i * 0.01), (float) 1.0, (float) 1.0);
		} // end for

		for (int i = w - 1; i >= 0; i--) { // fill the canvas with a spectrum
			g.setColor(colors[c]);
			c++;
			if (c >= 99)
				c = 0;
			g.drawLine(i, 0, i, h);
		} // end for

		while (true) {
			g.copyArea(0, 0, w - 1, h, 1, 0); // move contents of canvas one pixel right
			g.setColor(colors[c]); // add a line of the next color, on the left
			c++;
			if (c >= 99)
				c = 0;
			g.drawLine(0, 0, 0, h);
			repaint(); // schedule the applet area for repainting
			try {
				Thread.sleep(50);
			} // wait 1/20 second
			catch (InterruptedException e) {
			} // end catch
		} // end while
	} // end method run
} // end class SlidingSpectrum
