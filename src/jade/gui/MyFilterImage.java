package jade.gui;

import java.awt.image.RGBImageFilter;
import java.awt.Color;
import java.awt.image.ImageFilter;

/**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date$ $Revision$
 */


public class MyFilterImage extends RGBImageFilter {

  Color colorPixel;
  Color colorNewPixel;


  public MyFilterImage() {
     canFilterIndexColorModel = true;
  }

  public int filterRGB(int x, int y, int rgb) {
   int intensity;
   int alpha=128;
   int redComponent,greenComponent,bluComponent;

   redComponent=(rgb & 0xFF0000) >> 16;
   greenComponent=(rgb & 0xff00) >> 8;
   bluComponent=rgb & 0xFF;
   intensity=(int) (redComponent * 0.299 + greenComponent * 0.587 +bluComponent * 0.114) ;
   redComponent= intensity << 16;
   greenComponent=intensity << 8;
   bluComponent=intensity;
   alpha=alpha << 24;

   return (alpha + redComponent + greenComponent + bluComponent);
  }

} // End Of MyFilterImage




