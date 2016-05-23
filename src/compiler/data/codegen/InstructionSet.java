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
	}

	public void add(Instruction i){
		instrs.add(i);
	}

	public void add(InstructionSet i){
		if(CodeGen.commentAnnotations){
			this.instrs.add(new Comment("BEG: " + i.imcode));
		}
		instrs.addAll(i.instrs);
		if(CodeGen.commentAnnotations){
			this.instrs.add(new Comment("END: " + i.imcode));
		}
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
}
