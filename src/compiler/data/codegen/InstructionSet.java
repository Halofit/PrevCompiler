package compiler.data.codegen;

import compiler.data.frg.CodeFragment;
import compiler.data.liveness.InterferenceGraph;
import compiler.phase.codegen.CodeGen;

import java.util.*;

/**
 * Created by gregor on 21. 05. 2016.
 */
public class InstructionSet {
	public String imcode;

	public LinkedList<Instruction> instrs;
	public Register ret;

	public HashSet<VirtualRegister> registers;

	public long mnemonicCount;

	public InstructionSet(String imcode) {
		this.imcode = imcode;
		this.instrs = new LinkedList<>();
		mnemonicCount = 0;
	}

	public void add(Instruction i) {
		instrs.add(i);
	}

	public void add(InstructionSet i) {
		instrs.addAll(i.instrs);
	}

	public void set(Register ret) {
		this.ret = ret;
	}

	public Instruction popLast() {
		Iterator<Instruction> it = this.instrs.descendingIterator();

		Instruction last = null;
		while (it.hasNext()) {
			last = it.next();
			if (!(last instanceof Comment)) {
				it.remove();
				break;
			}
		}

		return last;
	}


	public void countRegisters() {
		this.registers = new HashSet<>();

		for (Instruction i : instrs) {
			if (i instanceof Mnemonic) {
				for (Operand op : ((Mnemonic) i).operands) {
					if (op instanceof VirtualRegister) registers.add((VirtualRegister) op);
				}
				mnemonicCount++;
			}
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("---").append(imcode).append("---\n");
		for (Instruction i : instrs) {
			sb.append(i).append('\n');
		}
		sb.append("---").append(imcode).append("---");

		return sb.toString();
	}

	public void spillVirtualRegister(VirtualRegister reg, long spillLoc) {
		ListIterator<Instruction> it = instrs.listIterator();
		while (it.hasNext()) {
			Instruction i = it.next();
			if (i.usesVirtualRegister(reg)) {
				it.remove();
				Mnemonic m = (Mnemonic) i;
				VirtualRegister newreg = VirtualRegister.create();
				VirtualRegister addreg = VirtualRegister.create();
				Mnemonic newM = m.getCopy(reg, newreg);

				boolean isSrc = newM.isSrc(newreg);
				boolean isDst = newM.isDest(newreg);

				if (isSrc) {
					//load offset, and load from the fp+offset (offset is negative, spillLoc is positive)
					it.add(new Mnemonic("NEG", addreg, new ConstantOperand(spillLoc)));
					it.add(new Mnemonic("LDO", newreg, CodeGen.fp, addreg));
				}
				it.add(newM);
				if (isDst) {
					if (!isSrc) {
						//if you haven't allready load the address here
						it.add(new Mnemonic("NEG", addreg, new ConstantOperand(spillLoc)));
					}
					it.add(new Mnemonic("STO", newreg, CodeGen.fp, addreg));
				}
			}
		}
	}

	public void mapRegisters(HashMap<VirtualRegister, InterferenceGraph.Node> nodeMap) {
		for (Instruction i : instrs) {
			i.mapRegisters(nodeMap);
		}
	}

	public void injectEntryAndExit(CodeFragment frag) {
		String label = frag.label;
		PhysicalRegister r0 = PhysicalRegister.get(0);
		PhysicalRegister r1 = PhysicalRegister.get(1);

		LinkedList<Instruction> prolog = new LinkedList<>();
		prolog.add(Label.get(label));

		//Move FP&SP.
		prolog.add(new Mnemonic("ADD", r0, CodeGen.fp, CodeGen.const0));
		prolog.add(new Mnemonic("ADD", CodeGen.fp, CodeGen.sp, CodeGen.const0));
		prolog.add(new Mnemonic("SET", r1, new ConstantOperand(frag.frame.size)));
		prolog.add(new Mnemonic("SUB", CodeGen.sp, CodeGen.sp, r1));

		//Save FP into the stack frame.
		prolog.add(new Mnemonic("SET", r1, new ConstantOperand(frag.frame.getOldFPOffset())));
		prolog.add(new Mnemonic("SUB", r1, CodeGen.fp, r1));
		prolog.add(new Mnemonic("STO", r0, r1, CodeGen.const0));

		//Save return address.
		prolog.add(new Mnemonic("GET", r0, CodeGen.returnJumpReg));
		prolog.add(new Mnemonic("SUB", r1, r1, new ConstantOperand(8)));
		prolog.add(new Mnemonic("STO", r0, r1, CodeGen.const0));
		prolog.add(new Comment(null));

		this.instrs.addAll(0, prolog);


		LinkedList<Instruction> epilog = new LinkedList<>();
		//epilog.add(Label.get("epilog_" + label)); uneeded

		//Save RV to stack.
		epilog.add(new Mnemonic("STO", CodeGen.rv, CodeGen.fp, CodeGen.const0));

		//Get old FP into register $0.
		epilog.add(new Mnemonic("SET", r1, new ConstantOperand(frag.frame.getOldFPOffset())));
		epilog.add(new Mnemonic("SUB", r1, CodeGen.fp, r1));
		epilog.add(new Mnemonic("STO", r0, r1, CodeGen.const0));

		//Restore rJ.
		epilog.add(new Mnemonic("SUB", r1, r1, new ConstantOperand(8)));
		epilog.add(new Mnemonic("STO", r1, r1, CodeGen.const0));
		epilog.add(new Mnemonic("PUT", CodeGen.returnJumpReg, r1));

		//Restore FP&SP
		epilog.add(new Mnemonic("ADD", CodeGen.sp, CodeGen.fp, CodeGen.const0));
		epilog.add(new Mnemonic("ADD", CodeGen.fp, r0, CodeGen.const0));

		//Return
		epilog.add(new Mnemonic("POP", CodeGen.const0, CodeGen.const0));

		this.instrs.addAll(epilog);
	}
}
