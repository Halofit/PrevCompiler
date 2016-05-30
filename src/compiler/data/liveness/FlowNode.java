package compiler.data.liveness;

import compiler.data.codegen.Mnemonic;

public class FlowNode {
	protected Mnemonic m;

	protected FlowNode follow;
	protected FlowNode follow2;

	public boolean isCjump;

	public FlowNode(Mnemonic instr, boolean isCjump) {
		this.m = instr;
		this.isCjump = isCjump;
	}

	@Override
	public String toString() {
		return m + " -> " + (follow == null ? "nullfollow" : follow.m) + ((follow2 == null) ? "" : " ( + " + follow2.m + ")");
	}
}
