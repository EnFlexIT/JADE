package jade.lang;

import jade.onto.Frame;
import jade.onto.Action;
import jade.onto.Predicate;

/**
   Interface for Content Language encoders/decoders. This interface
   declares two methods that must convert between text and frame
   representations according to a specific content language.
 */
public interface Codec {

  String encodeConcept(Frame f);
  Frame decodeConcept(String s);

  String encodeAction(Action a);
  Action decodeAction(String s);

  String encodeProposition(Predicate p);
  Predicate decodeProposition(String s);

}
