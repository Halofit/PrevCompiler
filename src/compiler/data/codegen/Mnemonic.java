package compiler.data.codegen;

import compiler.common.report.InternalCompilerError;

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
				return new VirtualRegister[]{(VirtualRegister) operands[0]};

			case "LDB":
			case "LDW":
			case "LDT":
			case "LDO":
				if(operands[2] instanceof VirtualRegister){
					return new VirtualRegister[]{(VirtualRegister) operands[1], (VirtualRegister) operands[2]};
				}else{
					return new VirtualRegister[]{(VirtualRegister) operands[1]};
				}

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
				if(operands[2] instanceof VirtualRegister){
					return new VirtualRegister[]{(VirtualRegister) operands[0],(VirtualRegister) operands[1], (VirtualRegister) operands[2]};
				}else{
					return new VirtualRegister[]{(VirtualRegister) operands[0],(VirtualRegister) operands[1]};
				}

			case "NEG":
				if(operands[2] instanceof VirtualRegister){
					return new VirtualRegister[]{(VirtualRegister) operands[2]};
				}else{
					return new VirtualRegister[]{};
				}


			default:
				System.out.println("Unrecognised mnemonic: " + mnemonic);
				throw new InternalCompilerError();
		}
	}
}
