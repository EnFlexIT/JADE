package jade.core.sam;

public abstract class AbsoluteCounterValueProvider implements CounterValueProvider {

	@Override
	public boolean isDifferential() {
		return false;
	}

}
