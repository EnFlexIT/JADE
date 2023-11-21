package test.bob.beans;


public class TestBeanEx extends TestBean {
	private static final long serialVersionUID = 1L;

	private String stringField;
	private int intField;
	private long longField;
	private float floatField;
	private double doubleField;
	private Integer capitalIntegerField;
	private Long capitalLongField;
	private Float capitalFloatField;
	private Double capitalDoubleField;
	private TestSubBean testSubBeanField;
	private TestSubBean testSubBeanTwoField;

	public String getStringField() {
		return stringField;
	}
	public void setStringField(String stringField) {
		this.stringField = stringField;
	}
	public int getIntField() {
		return intField;
	}
	public void setIntField(int intField) {
		this.intField = intField;
	}
	public long getLongField() {
		return longField;
	}
	public void setLongField(long longField) {
		this.longField = longField;
	}
	public float getFloatField() {
		return floatField;
	}
	public void setFloatField(float floatField) {
		this.floatField = floatField;
	}
	public double getDoubleField() {
		return doubleField;
	}
	public void setDoubleField(double doubleField) {
		this.doubleField = doubleField;
	}
	public Integer getCapitalIntegerField() {
		return capitalIntegerField;
	}
	public void setCapitalIntegerField(Integer capitalIntegerField) {
		this.capitalIntegerField = capitalIntegerField;
	}
	public Long getCapitalLongField() {
		return capitalLongField;
	}
	public void setCapitalLongField(Long capitalLongField) {
		this.capitalLongField = capitalLongField;
	}
	public Float getCapitalFloatField() {
		return capitalFloatField;
	}
	public void setCapitalFloatField(Float capitalFloatField) {
		this.capitalFloatField = capitalFloatField;
	}
	public Double getCapitalDoubleField() {
		return capitalDoubleField;
	}
	public void setCapitalDoubleField(Double capitalDoubleField) {
		this.capitalDoubleField = capitalDoubleField;
	}
	public TestSubBean getTestSubBeanField() {
		return testSubBeanField;
	}
	public void setTestSubBeanField(TestSubBean testSubBeanField) {
		this.testSubBeanField = testSubBeanField;
	}
	public TestSubBean getTestSubBeanTwoField() {
		return testSubBeanTwoField;
	}
	public void setTestSubBeanTwoField(TestSubBean testSubBeanTwoField) {
		this.testSubBeanTwoField = testSubBeanTwoField;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TestBeanEx {");
		sb.append("stringField=");
		sb.append(stringField);
		sb.append(" intField=");
		sb.append(intField);
		sb.append(" longField=");
		sb.append(longField);
		sb.append(" floatField=");
		sb.append(floatField);
		sb.append(" doubleField=");
		sb.append(doubleField);
		sb.append(" capitalIntegerField=");
		sb.append(capitalIntegerField);
		sb.append(" capitalLongField=");
		sb.append(capitalLongField);
		sb.append(" capitalFloatField=");
		sb.append(capitalFloatField);
		sb.append(" capitalDoubleField=");
		sb.append(capitalDoubleField);
		sb.append(" testSubBeanField=");
		sb.append(testSubBeanField);
		sb.append(" testSubBeanTwoField=");
		sb.append(testSubBeanTwoField);
		sb.append('}');
		return sb.toString();
	}
}
