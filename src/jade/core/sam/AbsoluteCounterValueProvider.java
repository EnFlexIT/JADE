package jade.core.sam;

//#DOTNET_EXCLUDE_FILE

public abstract class AbsoluteCounterValueProvider implements CounterValueProvider {

	@Override
	public boolean isDifferential() {
		return false;
	}

}
