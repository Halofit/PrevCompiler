package compiler.data.codegen;

/**
 * Created by gregor on 20.5.2016.
 */
public class ConstantOperand extends Operand {
	int value;

	public ConstantOperand(int value) {
		this.value = value;
	}
}
