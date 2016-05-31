package compiler.data.codegen;

import compiler.data.liveness.InterferenceGraph;

import java.util.HashMap;

/**
 * Created by gregor on 21. 05. 2016.
 */
public class Comment extends Instruction {

	public String content;

	public Comment(String content) {
		this.content = content;
	}

	public boolean startsWith(String prefix){
		return (content != null && content.startsWith(prefix));
	}

	@Override
	public String toString() {
		if(content == null){
			return "";
		}else{
			return "%" + content;
		}
	}

	@Override
	public boolean usesVirtualRegister(VirtualRegister reg) {
		return false;
	}

	@Override
	public void mapRegisters(HashMap<VirtualRegister, InterferenceGraph.Node> nodeMap) {

	}
}
