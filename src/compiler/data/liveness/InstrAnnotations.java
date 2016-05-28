package compiler.data.liveness;

import compiler.common.report.InternalCompilerError;
import compiler.data.codegen.Mnemonic;
import compiler.data.codegen.VirtualRegister;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by gregor on 22. 05. 2016.
 */
public class InstrAnnotations {
	Mnemonic instr;
	boolean isMove;

	VirtualRegister def;
	VirtualRegister[] use;

	HashSet<VirtualRegister> in;
	HashSet<VirtualRegister> out;

	public InstrAnnotations(Mnemonic instr) {
		this.instr = instr;
		this.isMove = this.instr.isMove();

		def = instr.def();
		use = instr.use();

		in = new HashSet<>();
		out = new HashSet<>();

		in.addAll(Arrays.asList(use));
	}

	public int addIn() {
		//System.out.println("In: " + " D:" + def + " U:" + Arrays.toString(use) + " in():" + in);
		int start = in.size();
		in.addAll(out);
		in.remove(def);
		in.addAll(Arrays.asList(use));

		//System.out.println("Add in: " + (in.size() - start));
		if (in.size() - start < 0) {
			System.out.println("error in IN()");
			System.out.println(this);
			throw new InternalCompilerError();
		}
		return in.size() - start;
	}

	public int addOut(HashSet<VirtualRegister> succSet) {
		//System.out.println("Out: " + " D:" + def + " U:" + Arrays.toString(use) + " out():" + out);
		int start = out.size();
		out.addAll(succSet);
		//System.out.println("Add out: " + (out.size() - start));
		if (out.size() - start < 0) {
			System.out.println("error in OUT()");
			System.out.println(this);
			throw new InternalCompilerError();
		}
		return out.size() - start;
	}


	@Override
	public String toString() {
		return "[" + instr + "] Def:" + def + " Use:" + Arrays.toString(use) + " in:" + in + " out:" + out;
	}
}
