package compiler.data.codegen;

import java.util.LinkedList;

/**
 * Created by gregor on 21. 05. 2016.
 */
public class InstructionSet {
	public LinkedList<Instruction> instrs;
	public VirtualRegister ret;

	public InstructionSet() {
		this.instrs = new LinkedList<>();
	}

	public void add(Instruction i){
		instrs.add(i);
	}

	public void add(LinkedList<Instruction> i){
		instrs.addAll(i);
	}

	public void add(InstructionSet i){
		instrs.addAll(i.instrs);
	}

	public void set(VirtualRegister ret){
		this.ret = ret;
	}
}
