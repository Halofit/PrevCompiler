package compiler.data.codegen;

import compiler.phase.codegen.CodeGen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by gregor on 21. 05. 2016.
 */
public class InstructionSet {
	public String imcode;

	public LinkedList<Instruction> instrs;
	public Register ret;

	public HashSet<VirtualRegister> registers;
	public HashMap<Label, Integer> labelLocations;

	public InstructionSet(String imcode) {
		this.imcode = imcode;
		this.instrs = new LinkedList<>();
		this.registers = new HashSet<>();
		this.labelLocations = new HashMap<>();
	}

	public void add(Instruction i){
		instrs.add(i);
		if(i instanceof Mnemonic){
			for (Operand op : ((Mnemonic) i).operands) {
				if(op instanceof VirtualRegister) registers.add((VirtualRegister) op);
			}
		}else if(i instanceof Label){
			labelLocations.put((Label) i, instrs.size() - 1);
		}

	}

	public void add(InstructionSet i){
		if(CodeGen.commentAnnotations){
			this.instrs.add(new Comment("BEG: " + i.imcode));
		}
		instrs.addAll(i.instrs);
		if(CodeGen.commentAnnotations){
			this.instrs.add(new Comment("END: " + i.imcode));
		}

		this.registers.addAll(i.registers);
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


	public String toString(){
		StringBuilder sb = new StringBuilder();

		sb.append("---").append(imcode).append("---").append('\n');
		for (Instruction i : instrs) {
			sb.append(i).append('\n');
		}
		sb.append("---").append(imcode).append("---");

		return sb.toString();
	}
}
