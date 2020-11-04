package jade.core.sam;

public class MediatedMeasureProvider extends AverageMeasureProviderImpl {
	
	private MeasureProvider realProvider; 
	
	public MediatedMeasureProvider(MeasureProvider realProvider) {
		this.realProvider = realProvider;
	}
	
	void collectNewValue() {
		Number v = realProvider.getValue();
		if (v != null && !Double.isNaN(v.doubleValue())) {
			addSample(realProvider.getValue());
		}
	}
}
