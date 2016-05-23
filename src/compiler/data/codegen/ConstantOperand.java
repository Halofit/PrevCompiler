package compiler.data.codegen;

import compiler.common.report.InternalCompilerError;

/**
 * Created by gregor on 20.5.2016.
 */
public class ConstantOperand extends Operand {
	//Should be byte if it could be unsigned
	int value;

	public ConstantOperand(int value) {
		this((long) value);
	}

	public ConstantOperand(long value) {
		if(value > 0xFFFF || value < 0) {
			System.out.println("Invalid operand value: " + value);
			throw new InternalCompilerError();
		}

		this.value = (short) value;
	}


	@Override
	public String toString() {
		return Integer.toString(value);
	}
}
