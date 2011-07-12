package test.bob.beans;

import jade.content.Concept;

public class BeanWithArrays implements Concept {
	private boolean[] booleans;
	private ClassOne[] classOnes;

	public boolean[] getBooleans() {
		return booleans;
	}

	public ClassOne[] getClassOnes() {
		return classOnes;
	}

	public void setBooleans(boolean[] booleans) {
		this.booleans = booleans;
	}

	public void setClassOnes(ClassOne[] classOnes) {
		this.classOnes = classOnes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("BeanWithArrays {booleans=");
		if (booleans == null) {
			sb.append("(null)");
		} else {
			sb.append('[');
			for (boolean b: booleans) {
				sb.append(b);
				sb.append(' ');
			}
			sb.append(']');
		}
		sb.append(" classOnes=");
		if (classOnes == null) {
			sb.append("(null)");
		} else {
			sb.append('[');
			for (ClassOne co: classOnes) {
				sb.append(co);
				sb.append(' ');
			}
			sb.append(']');
		}
		sb.append('}');
		return sb.toString();
	}
}
