package compiler.data.liveness;

import compiler.data.codegen.*;

import java.util.*;

/**
 * Created by gregor on 22. 05. 2016.
 */
public class InterferenceGraph {

	private String functionLabel;
	private HashMap<VirtualRegister, Node> nodeMap;
	private HashMap<Mnemonic, InstrAnnotations> annotations;
	private InstrFlowGraph flow;

	public InterferenceGraph(InstructionSet instrs, String functionLabel) {
		this.functionLabel = functionLabel;
		annotations = new HashMap<>();
		nodeMap = new HashMap<>();

		//Create flow graph
		flow = new InstrFlowGraph(instrs);

		annotateInstructions(instrs);
		//instrs.indexLabels();
		instrs.countRegisters();


		propagateUsage();
		createInterferanceGraph(instrs);
		checkInterferance();
	}


	private void createInterferanceGraph(InstructionSet instrs) {
		int size = instrs.registers.size();
		Iterator<VirtualRegister> it = instrs.registers.iterator();
		for (int i = 0; i < size; i++) {
			Node n = new Node(it.next());
			nodeMap.put(n.reg, n);
		}
	}

	private void checkInterferance() {
		for (InstrAnnotations ann : annotations.values()) {
			//For every register in in[] add all other registers contained in in[]
			for (VirtualRegister r : ann.in) {
				Node n = nodeMap.get(r);
				n.addEdges(ann.in);
			}

			//repeat for out[]
			for (VirtualRegister r : ann.out) {
				Node n = nodeMap.get(r);
				n.addEdges(ann.out);
			}
		}
	}


	private void annotateInstructions(InstructionSet instrs) {
		for (Instruction in : instrs.instrs) {
			if (in instanceof Mnemonic) {
				Mnemonic i = (Mnemonic) in;
				annotations.put(i, new InstrAnnotations(i));
			}
		}
	}


	private void propagateUsage() {
		int changes = 1;
		int iterations = 0;
		while (changes != 0) {
			changes = 0;

			InstrFlowGraph.FlowIterator it = flow.iterator();
			while (it.hasNext()) {
				FlowNode n = it.next();

				InstrAnnotations annot = annotations.get(n.m);
				InstrAnnotations followAnnot;

				if (n.follow != null) {
					followAnnot = annotations.get(n.follow.m);
					changes += annot.addOut(followAnnot.in);
					changes += annot.addIn();

				}

				if (n.isCjump && n.follow2 != null) {
					followAnnot = annotations.get(n.follow2.m);
					changes += annot.addOut(followAnnot.in);
					changes += annot.addIn();
				}
			}

			System.out.println("[" + iterations + "]:" + changes);
			iterations++;

		}
		System.out.println("Iterations: " + iterations);
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.functionLabel).append('\n');

		for (Node n : this.nodeMap.values()) {
			sb.append('\t').append(n).append('\n');
		}

		return sb.toString();
	}

	class Node {
		VirtualRegister reg;
		HashSet<Node> edges;

		public Node(VirtualRegister reg) {
			this.reg = reg;
			this.edges = new HashSet<>();
		}

		public void addEdges(HashSet<VirtualRegister> regs) {
			for (VirtualRegister r : regs) {
				if (r != reg) edges.add(nodeMap.get(r));
			}
		}

		@Override
		public String toString() {
			return reg + " interferes with: " + Arrays.toString(edges.stream().map(node -> node.reg.toString()).toArray(String[]::new));
		}
	}
}
