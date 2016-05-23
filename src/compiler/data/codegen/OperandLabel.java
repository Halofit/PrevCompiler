package compiler.data.codegen;

/**
 * Created by gregor on 21. 05. 2016.
 */
public class OperandLabel extends Operand {
	public Label label;

	public OperandLabel(String label) {
		this.label = Label.get(label);
	}

	@Override
	public String toString() {
		String s = label.toString();
		return s.substring(0, s.length()-2); //remove the last :
	}
}
