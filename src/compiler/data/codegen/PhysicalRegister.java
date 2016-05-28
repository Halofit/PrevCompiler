package compiler.data.codegen;

import compiler.phase.regalloc.RegisterAlloc;

import java.util.HashMap;

/**
 * Created by gregor on 28. 05. 2016.
 */
public class PhysicalRegister extends Register {

	private static HashMap<Integer, PhysicalRegister> registers = new HashMap<>(RegisterAlloc.physicalRegisters);

	public int name;
	private PhysicalRegister(int name) {
		this.name = name;
	}

	public static PhysicalRegister get(int name){
		PhysicalRegister pr = registers.get(name);
		if(pr == null){
			pr = new PhysicalRegister(name);
			registers.put(name, pr);
		}
		return pr;
	}

	@Override
	public String toString() {
		return "r"+name;
	}
}
