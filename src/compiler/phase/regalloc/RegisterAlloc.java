package compiler.phase.regalloc;

import compiler.Task;
import compiler.data.frg.CodeFragment;
import compiler.data.liveness.InterferenceGraph;
import compiler.phase.Phase;

import java.util.HashMap;

/**
 * Created by gregor on 27.5.2016.
 */
public class RegisterAlloc extends Phase {
	private HashMap<CodeFragment, InterferenceGraph> intfGraph;

	public RegisterAlloc(Task task) {
		super(task, "regalloc");
		intfGraph = task.intfGraph;
	}

	public void alloc(){
		for (CodeFragment frag : intfGraph.keySet()) {
			InterferenceGraph graph = intfGraph.get(frag);
		}
	}
}
