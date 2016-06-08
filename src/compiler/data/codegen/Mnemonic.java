package compiler.data.codegen;

import compiler.common.report.InternalCompilerError;
import compiler.data.liveness.InterferenceGraph;

import java.util.Arrays;
import java.util.HashMap;

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
		sb.append(' ');
		for (Operand operand : operands) {
			sb.append(operand).append(',');
		}

		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	@Override
	public boolean usesVirtualRegister(VirtualRegister reg) {
		for (Operand op : operands) {
			if(op.equals(reg)) return true;
		}
		return false;
	}

	@Override
	public void mapRegisters(HashMap<VirtualRegister, InterferenceGraph.Node> nodeMap) {
		for (int i = 0; i < operands.length; i++) {
			if(operands[i] instanceof VirtualRegister){
				VirtualRegister vr = (VirtualRegister) operands[i];
				InterferenceGraph.Node n = nodeMap.get(vr);
				operands[i] = PhysicalRegister.get(n.phyRegName);
			}
		}
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
			case "LDA":

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

			case "ZSZ":
			case "ZSNZ":
			case "ZSN":
			case "ZSNN":
			case "ZSP":
			case "ZSNP":
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
			case "LDA":
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

			case "ADD":
			case "SUB":
			case "MUL":
			case "DIV":
			case "CMP":
			case "OR":
			case "AND":

			case "ZSZ":
			case "ZSNZ":
			case "ZSN":
			case "ZSNN":
			case "ZSP":
			case "ZSNP":
				return filterNonVirtual(operands[1], operands[2]);

			case "STB":
			case "STW":
			case "STT":
			case "STO":
				return filterNonVirtual(operands[0], operands[1], operands[2]);

			case "NEG":
				//this one is special, since it takes a variable ammount of opeands
				if(operands.length == 2){
					return filterNonVirtual(operands[1]);
				}else{
					return filterNonVirtual(operands[1], operands[2]);
				}



			default:
				System.out.println("Unrecognised mnemonic: " + mnemonic);
				throw new InternalCompilerError();
		}
	}

	private static VirtualRegister[] filterNonVirtual(Operand... ops) {
		return Arrays.stream(ops).filter(op -> op instanceof VirtualRegister).map(op -> (VirtualRegister) op).toArray(VirtualRegister[]::new);
	}

	public boolean isMove() {
		return (mnemonic.equals("ADD") &&
				(operands[2] instanceof ConstantOperand) &&
				((ConstantOperand) operands[2]).value == 0 &&
				operands[1] instanceof VirtualRegister);
	}

	public Mnemonic getCopy(VirtualRegister oldreg, VirtualRegister newreg){
		Operand[] ops = Arrays.stream(this.operands).map(op ->  oldreg.equals(op) ? newreg : op).toArray(Operand[]::new);
		return new Mnemonic(this.mnemonic, ops);
	}

	public boolean isDest(VirtualRegister reg){
		return ((this.operands.length > 0) && reg.equals(operands[0]));
	}

	public boolean isSrc(VirtualRegister reg){
		for (int i = 1; i < this.operands.length; i++) {
			if(reg.equals(operands[i])) return true;
		}
		return false;
	}
}
