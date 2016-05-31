package compiler.data.codegen;

import compiler.data.liveness.InterferenceGraph;

import java.util.HashMap;

/**
 * Created by gregor on 20.5.2016.
 */
public abstract class Instruction {
	public abstract String toString();
	public abstract boolean usesVirtualRegister(VirtualRegister reg);
	public abstract void mapRegisters(HashMap<VirtualRegister, InterferenceGraph.Node> nodeMap);
}
