package jade.lang;

import jade.onto.Frame;

/**
   Interface for Content Language encoders/decoders. This interface
   declares two methods that must convert between text and frame
   representations according to a specific content language.
 */
public interface Codec {

  String encode(Frame f);
  Frame decode(String s);

}
