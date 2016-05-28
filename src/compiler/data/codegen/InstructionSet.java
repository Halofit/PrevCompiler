package compiler.data.codegen;

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
	public HashMap<Label, Integer> labelLocations;

	public long mnemonicCount;

	public InstructionSet(String imcode) {
		this.imcode = imcode;
		this.instrs = new LinkedList<>();
		mnemonicCount = 0;
	}

	public void add(Instruction i){
		instrs.add(i);
	}

	public void add(InstructionSet i){
		instrs.addAll(i.instrs);
	}

	public void set(Register ret){
		this.ret = ret;
	}

	public Instruction popLast(){
		Iterator<Instruction> it = this.instrs.descendingIterator();

		Instruction last = null;
		while(it.hasNext()){
			last = it.next();
			if(!(last instanceof Comment)){
				it.remove();
				break;
			}
		}

		return last;
	}

	public void indexLabels(){
		this.labelLocations = new HashMap<>();

		int loc = 0;
		Iterator<Instruction> it = this.instrs.descendingIterator();
		while(it.hasNext()){
			Instruction next = it.next();
			if(next instanceof Label){
				labelLocations.put((Label) next, loc);
			}
			loc++;
		}
	}

	public void countRegisters(){
		this.registers = new HashSet<>();

		for (Instruction i : instrs) {
			if(i instanceof Mnemonic){
				for (Operand op : ((Mnemonic) i).operands) {
					if(op instanceof VirtualRegister) registers.add((VirtualRegister) op);
				}
				mnemonicCount++;
			}
		}
	}

	public String toString(){
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
		while(it.hasNext()){
			Instruction i = it.next();
			if(i.usesVirtualRegister(reg)){
				it.remove();
				Mnemonic m = (Mnemonic) i;
				VirtualRegister newreg = VirtualRegister.create();
				VirtualRegister addreg = VirtualRegister.create();
				Mnemonic newM = m.getCopy(reg, newreg);

				boolean isSrc = newM.isSrc(newreg);
				boolean isDst = newM.isDest(newreg);

				if(isSrc){
					//load offset, and load from the fp+offset (offset is negative, spillLoc is positive)
					it.add(new Mnemonic("NEG", addreg, new ConstantOperand(spillLoc)));
					it.add(new Mnemonic("LDO", newreg, CodeGen.fp, addreg));
				}
				it.add(newM);
				if(isDst){
					if(!isSrc){
						//if you haven't allready load the address here
						it.add(new Mnemonic("NEG", addreg, new ConstantOperand(-spillLoc)));
					}
					it.add(new Mnemonic("STO", newreg, CodeGen.fp, addreg));
				}
			}
		}
	}
}
