package compiler.data.codegen;

import compiler.data.liveness.InterferenceGraph;

import java.util.HashMap;

/**
 * Created by gregor on 20.5.2016.
 */
public class Label extends Instruction {
	public String label;

	private Label(String label) {
		this.label = label;
	}

	private static HashMap<String, Label> labels = new HashMap<>();

	public static Label get(String label) {
		Label laObj = labels.get(label);
		if (laObj == null) {
			laObj = new Label(label);
			labels.put(label, laObj);
		}
		return laObj;
	}

	@Override
	public String toString() {
		return label + "\t";
	}

	@Override
	public boolean usesVirtualRegister(VirtualRegister reg) {
		return false;
	}

	@Override
	public void mapRegisters(HashMap<VirtualRegister, InterferenceGraph.Node> nodeMap) {

	}

	@Override
	public int hashCode() {
		return label.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return label.equals(obj);
	}
}
