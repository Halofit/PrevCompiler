package compiler.data.liveness;

import compiler.common.report.InternalCompilerError;
import compiler.data.codegen.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by gregor on 23. 05. 2016.
 */
public class InstrFlowGraph {
	private static final List<String> cjumps = Arrays.asList("BZ", "PBZ", "BNZ", "PBNZ", "BN", "PBN", "BNN", "PBNN", "BP", "PBP", "BNP", "PBNP");

	private ArrayList<FlowNode> instructions;

	public InstrFlowGraph(InstructionSet is) {
		instructions = new ArrayList<>(is.instrs.size());
		HashMap<Label, FlowNode> labelPts = new HashMap<>();

		FlowNode prev = null;
		Label lab = null;
		for (Instruction instr : is.instrs) {
			if (instr instanceof Comment) continue;
			if (instr instanceof Label) {
				lab = (Label) instr;
			} else {
				Mnemonic m = (Mnemonic) instr;
				FlowNode n = new FlowNode(m, cjumps.contains(m.mnemonic));
				instructions.add(n);
				if (lab != null) {
					labelPts.put(lab, n);
					lab = null;
				}

				if (prev != null) {
					if (prev.m.mnemonic.equals("JMP")) {
						//if it's likely nonlinear flow
						prev.follow = null;
					} else {
						prev.follow = n;
					}
				}

				prev = n;
			}
		}

		//Now fixup the jumps
		for (FlowNode n : instructions) {
			if (n.m.mnemonic.equals("JMP")) {
				n.follow = labelPts.get(((OperandLabel) n.m.operands[0]).label);
			} else if (cjumps.contains(n.m.mnemonic)) {
				n.follow2 = labelPts.get(((OperandLabel) n.m.operands[1]).label);
				if(n.follow == null || n.follow2 == null) throw new InternalCompilerError();
			}
		}
	}

	public FlowIterator iterator() {
		return new FlowIterator();
	}


	class FlowIterator implements Iterator<FlowNode> {
		private ListIterator<FlowNode> iter;

		public FlowIterator() {
			iter = instructions.listIterator(instructions.size());
		}

		@Override
		public boolean hasNext() {
			return iter.hasPrevious();
		}

		@Override
		public FlowNode next() {
			return iter.previous();
		}
	}


	public void printFollowersToFile(String label) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(label+"flowgraph.graph", "US-ASCII");

			Stack<FlowNode> stack = new Stack<>();
			FlowIterator it = this.iterator();
			while (it.hasNext()){
				FlowNode n = it.next();
				stack.push(n);
			}

			while(!stack.empty()){
				writer.println(stack.pop());
			}

			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}


