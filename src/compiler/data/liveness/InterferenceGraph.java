package compiler.data.liveness;

import compiler.data.codegen.Instruction;
import compiler.data.codegen.InstructionSet;
import compiler.data.codegen.Mnemonic;
import compiler.data.codegen.VirtualRegister;
import compiler.data.frg.CodeFragment;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by gregor on 22. 05. 2016.
 */
public class InterferenceGraph {

	private CodeFragment frag;
	public HashMap<VirtualRegister, Node> nodeMap;
	public HashMap<Mnemonic, InstrAnnotations> annotations;
	private InstrFlowGraph flow;
	private InstructionSet instrs;

	//For register allocation
	private int phyRegs;
	private Stack<InterferenceGraph.Node> nodeStack;

	public InterferenceGraph(InstructionSet instrs, CodeFragment frag, int physicalRegisters) {
		this.frag = frag;
		annotations = new HashMap<>();
		nodeMap = new HashMap<>();
		this.instrs = instrs;

		//Create flow graph
		flow = new InstrFlowGraph(instrs);

		annotateInstructions();
		//instrs.indexLabels();
		instrs.countRegisters();


		propagateUsage();
		createInterferanceGraph();
		checkInterferance();

		phyRegs = physicalRegisters;
		nodeStack = new Stack<>();

		printInOuts();
	}


	private void createInterferanceGraph() {
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


	private void annotateInstructions() {
		for (Instruction in : instrs.instrs) {
			if (in instanceof Mnemonic) {
				Mnemonic i = (Mnemonic) in;
				annotations.put(i, new InstrAnnotations(i));
			}
		}
	}


	private void propagateUsage() {
		int changes = 1;
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
		}
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.frag.label).append('\n');

		for (Node n : this.nodeMap.values()) {
			sb.append('\t').append(n).append('\n');
		}

		return sb.toString();
	}

	public void printInOuts(){
		PrintWriter writer;
		try {
			writer = new PrintWriter(frag.label+ "inout.graph", "US-ASCII");

			for (Instruction i : this.instrs.instrs) {
				if(i instanceof Mnemonic){
					InstrAnnotations ann = annotations.get(i);
					writer.print(" ");
					writer.print(ann);
				}else{
					writer.print(i);
				}

				writer.println();
			}

			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public class Node {
		boolean spill;
		boolean visible;
		int level;

		VirtualRegister reg;
		HashSet<Node> edges;
		public int phyRegName;

		public Node(VirtualRegister reg) {
			this.reg = reg;
			this.edges = new HashSet<>();
			phyRegName = -1;
			this.visible = true;
		}

		public void addEdges(HashSet<VirtualRegister> regs) {
			for (VirtualRegister r : regs) {
				if (r != reg) edges.add(nodeMap.get(r));
			}

			level = edges.size();
		}


		public boolean color() {
			boolean[] takenColors = new boolean[phyRegs];

			for (Node edge : edges) {
				if (edge.visible && edge.phyRegName >= 0) {
					takenColors[edge.phyRegName] = true;
				}
			}

			for (int i = 0; i < takenColors.length; i++) {
				if (!takenColors[i]) {
					this.phyRegName = i;
					return true;
				}
			}
			return false;
		}

		public void show() {
			this.visible = true;
			for (Node n : edges) {
				n.level++;
			}
		}

		public void hide() {
			this.visible = false;
			for (Node n : edges) {
				n.level--;
			}
		}

		@Override
		public String toString() {
			return reg + " interferes with: " + Arrays.toString(edges.stream().map(node -> node.reg.toString()).toArray(String[]::new));
		}

	}

	//===========================================
	//	Register allocation
	//===========================================

	public boolean simplify() {

		boolean change;
		boolean anyToSpill;

		int simplified = 0;

		do {
			change = false;
			anyToSpill = false;

			for (Node n : nodeMap.values()) {
				if (n.visible && n.level < phyRegs) {
					n.spill = false;
					nodeStack.push(n);
					n.hide();
					change = true;
					simplified++;
				} else {
					anyToSpill = true;
				}
			}
		} while (change);

		System.out.println(nodeMap.size());
		for (Node n : nodeMap.values()) {
			System.out.println(n.level);
		}
		System.out.println("Simplified: " + simplified);
		System.exit(1);

		return anyToSpill;
	}


	public void spill() {
		for (Node n : nodeMap.values()) {
			if (n.visible && n.level >= phyRegs) {
				n.spill = true;
				nodeStack.push(n);
				n.hide();
				break;
			}
		}
	}


	public boolean select() {
		boolean anySpilled = false;

		while (!nodeStack.empty()) {
			Node n = nodeStack.pop();
			if (n.spill) {
				n.show();
				if (!n.color()) {
					anySpilled = true;
				}
			} else {
				n.show();
				n.color();
			}
		}

		return anySpilled;
	}

	public void startOver() {
		for (Node n : nodeMap.values()) {
			//-1 specifies uncolored
			if (n.phyRegName == -1) {
				long tempL = this.frag.frame.addTemp();
				long offset = this.frag.frame.getTempsOffset(tempL);
				instrs.spillVirtualRegister(n.reg, offset);
			}
		}
	}
}
