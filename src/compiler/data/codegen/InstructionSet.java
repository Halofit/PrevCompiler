package compiler.data.codegen;

import compiler.phase.codegen.CodeGen;

import java.util.LinkedList;

/**
 * Created by gregor on 21. 05. 2016.
 */
public class InstructionSet {
	public String imcode;

	public LinkedList<Instruction> instrs;
	public VirtualRegister ret;

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

	public void set(VirtualRegister ret){
		this.ret = ret;
	}
}
