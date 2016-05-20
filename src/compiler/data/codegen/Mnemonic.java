package compiler.data.codegen;

/**
 * Created by gregor on 20.5.2016.
 */
public class Mnemonic extends Instruction {

	String mnemonic;
	Operand[] operands;

	public Mnemonic(String mnemonic, Operand[] operands) {
		this.mnemonic = mnemonic;
		this.operands = operands;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(mnemonic);
		for (Operand operand : operands) {
			sb.append(operand).append(',');
		}

		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
}
