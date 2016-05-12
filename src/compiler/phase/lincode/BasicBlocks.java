package compiler.phase.lincode;

import compiler.Task;
import compiler.common.report.InternalCompilerError;
import compiler.data.frg.CodeFragment;
import compiler.data.frg.Fragment;
import compiler.data.imc.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;


public class BasicBlocks {

	Task task;

	public BasicBlocks(Task task) {
		this.task = task;
	}

	public void transform() {
		for (Fragment fragment : task.fragments.values()) {
			if ((fragment instanceof CodeFragment)) {
				transform((CodeFragment) fragment);
			}
		}
	}

	private void transform(CodeFragment fragment) {

		if (fragment.linCode == null) {
			System.err.println("Fragment has a null linearised code.");
			throw new InternalCompilerError();
		}


		LinkedList<Block> blocks = extractBlocks(fragment.linCode);

		for (Block b : blocks) {
			System.out.println(b.toString());
		}
		System.out.println();

		blocks = removeTrivialBlocks(blocks);

		for (Block b : blocks) {
			System.out.println(b.toString());
		}
		System.out.println();

		Block exitBlock = blocks.pollLast();

		ArrayList<Block> ordBlk = new ArrayList<>();
		ordBlk.add(blocks.pollFirst());

		while (blocks.size() != 0) {
			Block lastBlock = ordBlk.get(ordBlk.size() - 1);
			Block nextBlock = null;
			String prefExit = lastBlock.getPrefferedExitLabel();

			Iterator it = blocks.iterator();

			while (it.hasNext()) {
				Block b = (Block) it.next();
				//if this block's entry label is the same as the last preffered exit
				if (b.entryLabels.contains(prefExit)) {
					nextBlock = b;
					it.remove();
					break;
				}
			}

			if (nextBlock == null) {
				while (it.hasNext()) {
					Block b = (Block) it.next();
					//if this block's entry label is the same as the last preffered exit

					if (b.entryLabels.stream().anyMatch(prefExit::contains)) {
						nextBlock = b;
						it.remove();
						break;
					}
				}
			}

			if (nextBlock == null) {
				nextBlock = blocks.pollFirst();
			}

			ordBlk.add(nextBlock);
		}

		ordBlk.add(exitBlock);

		for (Block b : ordBlk) {
			System.out.println(b.toString());
		}

		fragment.linCode = reserialiseBlocks(ordBlk);
	}

	private LinkedList<Block> removeTrivialBlocks(LinkedList<Block> blocks) {
		Iterator<Block> it = blocks.iterator();
		while (it.hasNext()) {
			Block b = it.next();

			if (b.isTrivial()) {
				//get entry and exit label
				ArrayList<String> entryLabels = b.entryLabels;
				String exitLabel = b.label;

				//now replace all the entry labels with the exit label
				for (Block searchB : blocks) {
					for (String el : entryLabels) {
						searchB.replaceLabel(el, exitLabel);
					}
				}

				//finally remove the trivial block
				it.remove();
			}
		}

		return blocks;
	}

	
	private STMTS reserialiseBlocks(ArrayList<Block> blocks) {
		Vector<IMCStmt> retStmst = new Vector<>();

		for (Block b : blocks) {
			if (retStmst.size() != 0) {
				IMCStmt lastStmt = retStmst.lastElement();
				LABEL l = (LABEL) b.stmts.get(0);

				if (lastStmt instanceof CJUMP) {
					if (l.label.equals(((CJUMP) lastStmt).negLabel)) {
						//This is good
					} else {
						//Otherwise we need to insert a new LABEL and JUMP
						LABEL newNegLabel = new LABEL(LABEL.newLabelName());
						String oldNegJump = ((CJUMP) lastStmt).negLabel;
						((CJUMP) lastStmt).negLabel = newNegLabel.label;
						retStmst.add(newNegLabel);
						retStmst.add(new JUMP(oldNegJump));
					}

				} else if (lastStmt instanceof JUMP) {
					//remove the rednundant jump
					if (l.label.equals(((JUMP) lastStmt).label)) {
						retStmst.setSize(retStmst.size() - 1);
					}
				} else {
					throw new InternalCompilerError();
				}
			}

			//copy the statements
			for (IMCStmt s : b.stmts) {
				retStmst.add(s);
			}
		}

		return new STMTS(retStmst);
	}

	//Extract basic blocks
	//First one will be the entry block, and the last one will probably just be an exit label
	private LinkedList<Block> extractBlocks(STMTS code) {
		LinkedList<Block> blocks = new LinkedList<>();

		LABEL start = new LABEL(LABEL.newLabelName());
		LABEL end = new LABEL(LABEL.newLabelName());

		//Add starting and ending labels + end jump -> just in case
		code.stmts.add(0, start);
		code.stmts.add(new JUMP(end.label));
		code.stmts.add(end);


		boolean lastWasLabel = false;
		boolean lastWasJump = true;

		for (IMCStmt s : code.stmts) {
			Block lastBlock;

			if (blocks.size() != 0) {
				lastBlock = blocks.getLast();
			} else {
				lastBlock = null;
			}

			if (s instanceof LABEL) {
				if (lastWasJump) {
					blocks.add(new Block((LABEL) s));
				} else if (lastWasLabel) {
					assert lastBlock != null;
					lastBlock.addLabel((LABEL) s);
				} else { //is on label, but last one was not a jump (or a label)
					assert lastBlock != null;
					lastBlock.addJump(new JUMP(((LABEL) s).label));
					blocks.add(new Block((LABEL) s));
				}

				lastWasLabel = true;
				lastWasJump = false;
			} else if (s instanceof CJUMP) {
				assert lastBlock != null;
				lastBlock.addCjump((CJUMP) s);

				lastWasLabel = false;
				lastWasJump = true;
			} else if (s instanceof JUMP) {
				assert lastBlock != null;
				lastBlock.addJump((JUMP) s);

				lastWasLabel = false;
				lastWasJump = true;
			} else {
				if (!lastWasJump) {
					assert lastBlock != null;
					lastBlock.addStatement(s);

					lastWasLabel = false;
					lastWasJump = false;
				} /*else {
					//last was jump, but this one is not a label -> dead code
					//This removes the dead code
				}*/
			}
		}

		return blocks;
	}

	static class Block {
		public ArrayList<IMCStmt> stmts;
		public ArrayList<String> entryLabels;

		String label;
		String posLabel;

		public char exitType;

		public Block(LABEL l) {
			this.stmts = new ArrayList<>();
			entryLabels = new ArrayList<>();

			addLabel(l);
			exitType = 0;
		}

		public boolean isTrivial() {
			//if there are only a few labels and a JUMP/CJUMP
			// the final block is size one, so it should get flagged by this
			// 		-> since it doesn't have a JUMP instruction
			if ((stmts.get(stmts.size() - 1) instanceof JUMP) && (stmts.size() == entryLabels.size() + 1)) System.out.println(this.toString() + " is trivial");
			return (stmts.get(stmts.size() - 1) instanceof JUMP) && (stmts.size() == entryLabels.size() + 1);
		}

		public void addLabel(LABEL l) {
			entryLabels.add(l.label);
			stmts.add(l);
		}


		public void addCjump(CJUMP j) {
			label = j.negLabel;
			posLabel = j.posLabel;
			stmts.add(j);

			if (exitType == 0) {
				exitType = 'c';
			} else {
				throw new InternalCompilerError();
			}
		}

		public void addJump(JUMP j) {
			label = j.label;
			stmts.add(j);

			if (exitType == 0) {
				exitType = 'j';
			} else {
				throw new InternalCompilerError();
			}
		}

		public void addStatement(IMCStmt s) {
			stmts.add(s);
		}

		public String getPrefferedExitLabel() {
			return label;
		}

		public IMCStmt getLast() {
			return stmts.get(stmts.size() - 1);
		}

		public void replaceLabel(String oldLabel, String newLabel) {
			IMCStmt jump = this.getLast();

			if (oldLabel.equals(label)) {
				label = newLabel;

				if (jump instanceof JUMP) {
					((JUMP) jump).label = newLabel;
				} else if (jump instanceof CJUMP) {
					((CJUMP) jump).negLabel = newLabel;
				} else {
					throw new InternalCompilerError();
				}
			}

			if (oldLabel.equals(posLabel)) {
				posLabel = newLabel;
				if(!(jump instanceof CJUMP)) throw new InternalCompilerError();
				((CJUMP) jump).posLabel = newLabel;
			}
		}

		@Override
		public String toString() {
			String res = "[";
			for (int i = 0; i < entryLabels.size(); i++) {
				res += stmts.get(i).toString();
				res += ",";
			}
			res += " ... , ";
			res += getLast().toString();
			res += "]";
			return res;
		}
	}
}
