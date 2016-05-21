package compiler.data.codegen;

/**
 * Created by gregor on 21. 05. 2016.
 */
public class OperandLabel extends Operand {
	String label;

	public OperandLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}
}
