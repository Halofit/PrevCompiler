package compiler.data.codegen;

/**
 * Created by gregor on 20.5.2016.
 */
public class ConstantValue extends Operand {
	int value;

	public ConstantValue(int value) {
		this.value = value;
	}
}
