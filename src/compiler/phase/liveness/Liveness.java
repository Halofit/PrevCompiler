package compiler.phase.liveness;

import compiler.Task;
import compiler.data.codegen.InstructionSet;
import compiler.data.frg.CodeFragment;
import compiler.data.liveness.InterferenceGraph;
import compiler.phase.Phase;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by gregor on 22. 05. 2016.
 */
public class Liveness extends Phase {
	public static boolean report = true;

	public HashMap<CodeFragment, InstructionSet> fragInstrs;
	public HashMap<CodeFragment, InterferenceGraph> intfGraph;


	public Liveness(Task task) {
		super(task, "liveness", false);

		this.intfGraph = task.intfGraph;
		this.fragInstrs = task.fragInstrs;
	}


	public void analyse() {
		for (CodeFragment frag : fragInstrs.keySet()) {
			InstructionSet instr = fragInstrs.get(frag);

			intfGraph.put(frag, new InterferenceGraph(instr, frag));

			if(report){
				System.out.println(frag.label + " uses " + instr.registers.size() + " registers.");
				System.out.println(frag.label + " uses " + instr.mnemonicCount + " instructions.");
			}
		}
	}


	@Override
	public void close() {
		super.close();

		if(this.logging){
			PrintWriter writer;
			try {
				writer = new PrintWriter(task.srcFName + ".graph", "US-ASCII");

				for (InterferenceGraph g : intfGraph.values()) {
					writer.println(g);
					g.printInOuts();
				}

				writer.close();
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
}
