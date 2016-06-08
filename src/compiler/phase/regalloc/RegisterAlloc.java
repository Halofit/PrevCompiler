package compiler.phase.regalloc;

import compiler.Task;
import compiler.data.codegen.Instruction;
import compiler.data.codegen.InstructionSet;
import compiler.data.frg.CodeFragment;
import compiler.data.liveness.InterferenceGraph;
import compiler.phase.Phase;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by gregor on 27.5.2016.
 */
public class RegisterAlloc extends Phase {
	public static int physicalRegisters = 4;

	private HashMap<CodeFragment, InstructionSet> fragInstrs;
	private HashMap<CodeFragment, InterferenceGraph> coloredGraphs;

	public RegisterAlloc(Task task) {
		super(task, "regalloc", false);
		fragInstrs = task.fragInstrs;
		coloredGraphs = new HashMap<>();
	}

	public void allocate(){
		for (CodeFragment frag : fragInstrs.keySet()) {
			InterferenceGraph graph;
			while(true){
				//System.out.println(frag.label);
				InstructionSet instrs = fragInstrs.get(frag);

				//Build:
				graph = new InterferenceGraph(instrs, frag);

				while(true){
					//Simplify
					boolean anyToSpill = graph.simplify();

					if(anyToSpill){
						//spill
						graph.spill();
					}else{
						break;
					}
				}

				//Select
				boolean anySpilled = graph.select();

				if(!anySpilled){
					break;
				}else{
					//start over -> we need to fix the code
					//do the actual spill of uncolored nodes
					//and modify the code
					graph.startOver();
				}
			}
			//We have finished -> save the graph
			coloredGraphs.put(frag, graph);
		}
	}

	public void mapRegisters() {
		for (CodeFragment frag : fragInstrs.keySet()) {
			InterferenceGraph g = coloredGraphs.get(frag);
			InstructionSet instrs = fragInstrs.get(frag);

			instrs.mapRegisters(g.nodeMap);
		}
	}

	@Override
	public void close() {
		if(this.logging){
			PrintWriter writer;
			try {
				writer = new PrintWriter(task.srcFName + ".mmix", "US-ASCII");

				String indent = "\t";

				for (CodeFragment codeFragment : fragInstrs.keySet()) {
					writer.println("");
					writer.println(codeFragment.frame.label + ":");
					writer.println("... (prolog)");
					InstructionSet instrs = fragInstrs.get(codeFragment);
					for (Instruction instr : instrs.instrs) {
						writer.print(indent);
						writer.println(instr);
					}
					writer.println("... (epilog)");
					writer.println("");
					writer.println("");
				}

				writer.println("");
				writer.println("");
				writer.close();


				//print the interferance graph as well
				writer = new PrintWriter(task.srcFName + ".graph", "US-ASCII");
				for (InterferenceGraph g : this.coloredGraphs.values()) {
					writer.println(g);
				}
				writer.close();

			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		super.close();
	}
}
