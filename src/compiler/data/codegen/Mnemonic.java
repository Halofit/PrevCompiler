package compiler.data.codegen;

import compiler.common.report.InternalCompilerError;

import java.util.Arrays;

/**
 * Created by gregor on 20.5.2016.
 */
public class Mnemonic extends Instruction {

	public String mnemonic;
	public Operand[] operands;

	public Mnemonic(String mnemonic, Operand... operands) {
		this.mnemonic = mnemonic;
		this.operands = operands;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(mnemonic);
		for (Operand operand : operands) {
			sb.append(' ');
			sb.append(operand).append(',');
		}

		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	public VirtualRegister def() {
		if (operands.length == 0) return null;

		switch (mnemonic) {
			case "PUSHJ":
			case "JMP":
			case "PBNZ":
			case "BNZ":

			case "STB":
			case "STW":
			case "STT":
			case "STO":
				return null;


			case "LDB":
			case "LDW":
			case "LDT":
			case "LDO":

			case "GET":
			case "GETA":

			case "SETL":
			case "INCML":
			case "INCMH":
			case "INCH":

			case "ADD":
			case "SUB":
			case "NEG":
			case "MUL":
			case "DIV":
			case "CMP":
			case "OR":
			case "AND":
				return operands[0] instanceof VirtualRegister ? (VirtualRegister) operands[0] : null;


			default:
				System.out.println("Unrecognised mnemonic: " + mnemonic);
				throw new InternalCompilerError();
		}
	}

	public VirtualRegister[] use() {
		switch (mnemonic) {
			case "JMP":
			case "GET":
			case "GETA":
			case "SETL":
				return new VirtualRegister[]{};

			case "PUSHJ":
			case "PBNZ":
			case "BNZ":

			case "INCML":
			case "INCMH":
			case "INCH":
				return filterNonVirtual(operands[0]);

			case "LDB":
			case "LDW":
			case "LDT":
			case "LDO":
				return filterNonVirtual(operands[1], operands[2]);

			case "STB":
			case "STW":
			case "STT":
			case "STO":

			case "ADD":
			case "SUB":
			case "MUL":
			case "DIV":
			case "CMP":
			case "OR":
			case "AND":
				return filterNonVirtual(operands[0], operands[1], operands[2]);

			case "NEG":
				return filterNonVirtual(operands[2]);


			default:
				System.out.println("Unrecognised mnemonic: " + mnemonic);
				throw new InternalCompilerError();
		}
	}

	private static VirtualRegister[] filterNonVirtual(Operand... ops){
		return Arrays.stream(ops).filter(op -> op instanceof VirtualRegister).map(op -> (VirtualRegister)op).toArray(VirtualRegister[]::new);
	}
}
