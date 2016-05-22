package compiler.data.liveness;

import compiler.data.codegen.Instruction;
import compiler.data.codegen.Mnemonic;
import compiler.data.codegen.VirtualRegister;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by gregor on 22. 05. 2016.
 */
public class InstrAnnotations {
	Mnemonic instr;

	VirtualRegister def;
	VirtualRegister[] use;

	HashSet<VirtualRegister> in;
	HashSet<VirtualRegister> out;

	public Instruction[] followers;

	public InstrAnnotations(Mnemonic instr) {
		this.instr = instr;
		def = instr.def();
		use = instr.use();

		in = new HashSet<>();
		out = new HashSet<>();

		in.addAll(Arrays.asList(use));
	}

	public boolean addIn(){
		int start = in.size();
		in.addAll(out);
		in.remove(def);

		return start == in.size();
	}

	public boolean addOut(HashSet<VirtualRegister> succSet){
		int start = out.size();
		out.addAll(succSet);
		return start == out.size();
	}
}
