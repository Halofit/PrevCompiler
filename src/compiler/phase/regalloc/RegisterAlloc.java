package compiler.phase.regalloc;

import compiler.Task;
import compiler.data.codegen.*;
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
	public static int physicalRegisters = 16;

	private HashMap<CodeFragment, InstructionSet> fragInstrs;
	private HashMap<CodeFragment, InterferenceGraph> coloredGraphs;

	public RegisterAlloc(Task task) {
		super(task, "regalloc");
		fragInstrs = task.fragInstrs;
		coloredGraphs = new HashMap<>();
	}

	public void allocate(){
		for (CodeFragment frag : fragInstrs.keySet()) {
			while(true){
				System.out.println(frag.label);
				InstructionSet instrs = fragInstrs.get(frag);

				//Build:
				InterferenceGraph graph = new InterferenceGraph(instrs, frag, physicalRegisters);

				while(true){
					//Simplify
					System.out.println("Simplify");
					boolean anyToSpill = graph.simplify();

					//spill
					System.out.println("Spill");
					if(anyToSpill){
						graph.spill();
					}else{
						break;
					}
				}

				//Select
				System.out.println("Select");
				boolean done = graph.select();

				if(done){
					break;
				}else{
					System.out.println("Start over");
					//start over -> we need to fix the code
					//do the actual spill of uncolored nodes
					//and modify the code
					graph.startOver();
				}

				//We have finished -> save the graph
				coloredGraphs.put(frag, graph);
			}
		}
	}

	public void mapRegisters() {
		for (CodeFragment frag : fragInstrs.keySet()) {
			InterferenceGraph g = coloredGraphs.get(frag);
			InstructionSet instrs = fragInstrs.get(frag);

			for (Instruction instr : instrs.instrs) {
				if(instr instanceof Mnemonic){
					Mnemonic m = (Mnemonic) instr;
					Operand[] operands = m.operands;
					for (int i = 0; i < operands.length; i++) {
						if (operands[i] instanceof VirtualRegister) {
							VirtualRegister vr = (VirtualRegister) operands[i];
							InterferenceGraph.Node node = g.nodeMap.get(vr);
							operands[i] = PhysicalRegister.get(node.phyRegName);
						}
					}
				}
			}
		}
	}

	@Override
	public void close() {
		if(logger != null){
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
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		super.close();
	}
}
