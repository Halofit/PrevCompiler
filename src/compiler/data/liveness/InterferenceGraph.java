package compiler.data.liveness;

import compiler.data.codegen.*;

import java.util.*;

/**
 * Created by gregor on 22. 05. 2016.
 */
public class InterferenceGraph {

	private Node[] nodes;
	private HashMap<VirtualRegister, Node> nodeMap;
	private HashMap<Mnemonic, InstrAnnotations> annotations;

	public InterferenceGraph(InstructionSet instrs) {
		annotations = new HashMap<>();

		nodes = new Node[instrs.registers.size()];
		Iterator<VirtualRegister> it = instrs.registers.iterator();
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = new Node(it.next());
			nodeMap.put(nodes[i].reg, nodes[i]);
		}


		annotateInstructions(instrs);
		propagateUsage(instrs);
		checkInterferance();
	}

	private void checkInterferance() {
		for (InstrAnnotations a : annotations.values()) {
			HashSet<VirtualRegister> intersection = new HashSet<>(a.in); // use the copy constructor
			intersection.retainAll(a.out);
			connect(intersection);
		}
	}

	private void connect(HashSet<VirtualRegister> regs) {
		for (VirtualRegister r : regs) {
			Node n = nodeMap.get(r);
			n.addEdges(regs);
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


	private void propagateUsage(InstructionSet instrs) {
		boolean change = true;
		int iterations = 0;
		while (change) {
			change = false;

			LinkedList<Instruction> instrLst = instrs.instrs;
			ListIterator<Instruction> it = instrLst.listIterator(instrLst.size() - 1);

			while (it.hasNext()) {
				Instruction i = it.next();
				if (i instanceof Mnemonic) {
					Mnemonic mne = (Mnemonic) i;
					switch (mne.mnemonic) {
						case "BZ":
						case "PBZ":
						case "BNZ":
						case "PBNZ":
						case "BN":
						case "PBN":
						case "BNN":
						case "PBNN":
						case "BP":
						case "PBP":
						case "BNP":
						case "PBNP": {
							InstrAnnotations annot = annotations.get(mne);

							//FOLLOW the jump
							Label l = ((OperandLabel) mne.operands[1]).label;
							int loc = instrs.labelLocations.get(l);
							ListIterator<Instruction> searchIt = instrLst.listIterator(loc);

							Instruction follow;
							while (!((follow = searchIt.next()) instanceof Mnemonic)) ;

							InstrAnnotations followAnnot = annotations.get((Mnemonic) follow);
							change |= annot.addOut(followAnnot.in);
							change |= annot.addIn();

							//FOLLOW through
							while (!((follow = it.next()) instanceof Mnemonic)) ;

							followAnnot = annotations.get((Mnemonic) follow);
							change |= annot.addOut(followAnnot.in);
							change |= annot.addIn();

							it.previous();
						}
						break;

						case "JMP": {
							//FOLLOW the jump
							InstrAnnotations annot = annotations.get(mne);

							Label l = ((OperandLabel) mne.operands[0]).label;
							int loc = instrs.labelLocations.get(l);
							ListIterator<Instruction> searchIt = instrLst.listIterator(loc);

							Instruction follow;
							while (!((follow = searchIt.next()) instanceof Mnemonic)) ;

							InstrAnnotations followAnnot = annotations.get((Mnemonic) follow);
							change |= annot.addOut(followAnnot.in);
							change |= annot.addIn();
						}

						break;
						default: {
							//FOLLOW through
							InstrAnnotations annot = annotations.get(mne);
							Instruction follow;
							while (!((follow = it.next()) instanceof Mnemonic)) ;

							InstrAnnotations followAnnot = annotations.get((Mnemonic) follow);
							change |= annot.addOut(followAnnot.in);
							change |= annot.addIn();

							it.previous();
						}
						break;
					}

				} else if (i instanceof Label) {

				} else {

				}
			}
			iterations++;
		}
		System.out.println("Iterations: " + iterations);
	}


	class Node {
		VirtualRegister reg;
		LinkedList<Node> edges;

		public Node(VirtualRegister reg) {
			this.reg = reg;
			this.edges = new LinkedList<>();
		}

		public void addEdges(HashSet<VirtualRegister> regs){
			for (VirtualRegister r : regs) {
				if(r != reg) edges.add(nodeMap.get(r));
			}
		}
	}
}
