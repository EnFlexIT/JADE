package jade.lang;

// for StreamTokenizer and various Reader classes
import java.io.*;

// Parser interface
public interface Parser {

  public void parse(Reader textSource);

}

