package compiler.phase.liveness;

import compiler.Task;
import compiler.data.codegen.InstructionSet;
import compiler.data.frg.CodeFragment;
import compiler.data.liveness.InterferenceGraph;
import compiler.phase.Phase;

import java.util.HashMap;

/**
 * Created by gregor on 22. 05. 2016.
 */
public class Liveness extends Phase {

	public HashMap<CodeFragment, InstructionSet> fragInstrs;
	public HashMap<CodeFragment, InterferenceGraph> intfGraph;

	public Liveness(Task task) {
		super(task, "liveness");

		this.intfGraph = task.intfGraph;
		this.fragInstrs = task.fragInstrs;
	}


	public void analyse() {
		for (CodeFragment frag : fragInstrs.keySet()) {
			InstructionSet instr = fragInstrs.get(frag);

			intfGraph.put(frag, new InterferenceGraph(instr));
		}
	}
}
