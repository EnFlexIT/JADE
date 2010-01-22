package test.bob.beans;

import jade.content.Concept;
import jade.content.onto.BOUtils;
import jade.content.onto.annotations.AggregateSlot;
import jade.util.leap.List;

public class TestSubBean implements Concept {
	private static final long serialVersionUID = 1L;

	private Double capitalDoubleField;
	private List listField;

	public Double getCapitalDoubleField() {
		return capitalDoubleField;
	}

	public void setCapitalDoubleField(Double capitalDoubleField) {
		this.capitalDoubleField = capitalDoubleField;
	}

	@AggregateSlot(type=java.lang.String.class)
	public List getListField() {
		return listField;
	}

	public void setListField(List listField) {
		this.listField = listField;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TestSubBean)) {
			return false;
		}

		TestSubBean tsb = (TestSubBean)obj;

		boolean result = true;

		if (capitalDoubleField == null) {
			if (tsb.getCapitalDoubleField() != null) {
				return false;
			}
		} else {
			result = capitalDoubleField.equals(tsb.getCapitalDoubleField());
		}

		if (result) {
			result = BOUtils.leapListsAreEqual(listField, tsb.getListField());
		}
		return result;
	}

	@Override
	public int hashCode() {
		int hashCode = 137;
		if (capitalDoubleField != null) {
			hashCode ^= capitalDoubleField.hashCode();
		}
		if (listField != null) {
			hashCode ^= listField.hashCode();
		}
				
		return hashCode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TestSubBean {");
		sb.append("capitalDoubleField=");
		sb.append(capitalDoubleField);
		sb.append(" listField=");
		sb.append(listField);
		sb.append('}');
		return sb.toString();
	}
}
