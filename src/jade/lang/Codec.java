package jade.lang;

import jade.onto.Frame;
import jade.onto.Ontology;

/**
   Interface for Content Language encoders/decoders. This interface
   declares two methods that must convert between text and frame
   representations according to a specific content language.
 */
public interface Codec {

  public static class CodecException extends Exception {

    private Throwable nested;

    public CodecException(String msg, Throwable t) {
      super(msg);
      nested = t;
    }

    public Throwable getNested() {
      return nested;
    }

  }

  /**
     Encodes a <code>Frame</code> object into a Jave
     <code>String</code>, according to this Content Language and
     looking up the given ontology for the role played by symbols
     (i.e. whether they are concepts, actions or predicates).
     @param f The frame to encode.
     @param o The ontology to use to lookup the roles for the various
     symbols.
     @return A Java string, representing the given frame according to
     this content language.
     @see jade.onto.Ontology
   */
  String encode(Frame f, Ontology o);

  /**
     Decodes a given <code>String</code>, according to the given
     Content Language and obtains a <code>Frame</code> object. This
     method uses the given ontology to distinguish among the different
     kinds of roles a symbol can play (i.e. Concept vs. Action
     vs. Predicate).
     @param s A string containing the representation of an ontological
     element, encoded according to this content language.
     @param o The ontology to use to lookup the roles for the various
     symbols.
     @return A frame, representing the given ontological element.
     @see jade.onto.Ontology
  */
  Frame decode(String s, Ontology o) throws CodecException;

}
