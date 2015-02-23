package jade.core.sam;

public abstract class DifferentialCounterValueProvider implements CounterValueProvider {

	@Override
	public boolean isDifferential() {
		return true;
	}

}
