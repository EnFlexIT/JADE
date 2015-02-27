package jade.content.onto;

//#APIDOC_EXCLUDE_FILE

public class NotASpecialType extends OntologyException {

	public NotASpecialType() {
		super("");
	}
	
	@Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
