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

/**
 * Created by gregor on 11. 05. 2016.
 */
public class BasicBlocks {

	Task task;

	public BasicBlocks(Task task) {
		this.task = task;
	}

	public void transform(){
		for (Fragment fragment : task.fragments.values()) {
			if ((fragment instanceof CodeFragment)) {
				transform((CodeFragment) fragment);
			}
		}
	}

	private void transform(CodeFragment fragment) {

		if(fragment.linCode == null) {
			System.err.println("Fragment has a null linearised code.");
			throw new InternalCompilerError();
		}

		LinkedList<Block> blocks = extractBlocks(fragment.linCode);

		Block exitBlock = blocks.pollLast();

		ArrayList<Block> ordBlk = new ArrayList<>();
		ordBlk.add(blocks.pollFirst());

		while(blocks.size() != 0){
			Block lastBlock = ordBlk.get(ordBlk.size() - 1);
			Block nextBlock = null;
			String prefExit = lastBlock.getPrefferedExitLabel();

			Iterator it = blocks.iterator();

			while(it.hasNext()){
				Block b = (Block) it.next();
				//if this block's entry label is the same as the last preffered exit
				if (b.entryLabels.contains(prefExit)) {
					nextBlock = b;
					it.remove();
					break;
				}
			}

			if(nextBlock == null){
				while(it.hasNext()){
					Block b = (Block) it.next();
					//if this block's entry label is the same as the last preffered exit

					if (b.entryLabels.stream().anyMatch(prefExit::contains)) {
						nextBlock = b;
						it.remove();
						break;
					}
				}
			}

			if(nextBlock == null){
				nextBlock = blocks.pollFirst();
			}

			ordBlk.add(nextBlock);
		}

		ordBlk.add(exitBlock);

		fragment.linCode = reserialiseBlocks(ordBlk);
	}

	private STMTS reserialiseBlocks(ArrayList<Block> blocks) {
		Vector<IMCStmt> stmts = new Vector<>();

		for (Block b : blocks) {
			for (IMCStmt s : b.stmts) {
				stmts.add(s);
			}
		}

		return new STMTS(stmts);
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

		for(IMCStmt s : code.stmts){
			Block lastBlock;

			if(blocks.size() != 0) {
				lastBlock = blocks.getLast();
			}else{
				lastBlock = null;
			}

			if(s instanceof LABEL){
				if(lastWasJump){
					blocks.add(new Block((LABEL) s));
				}else if(lastWasLabel){
					lastBlock.addLabel((LABEL) s);
				}else{ //is on label, but last one was not a jump (or a label)
					lastBlock.addJump(new JUMP(((LABEL) s).label));
					blocks.add(new Block((LABEL) s));
				}

				lastWasLabel = true;
				lastWasJump = false;
			}else if(s instanceof CJUMP){
				lastBlock.addCjump((CJUMP) s);

				lastWasLabel = false;
				lastWasJump = true;
			}else if(s instanceof JUMP){
				lastBlock.addJump((JUMP) s);

				lastWasLabel = false;
				lastWasJump = true;
			}else{
				if(lastWasJump) {
					//TODO last was jump, but this one is not a label -> dead code?
					//This removes the dead code
				}else{
					lastBlock.addStatement(s);

					lastWasLabel = false;
					lastWasJump = false;
				}
			}
		}

		return blocks;
	}

	static class Block{
		public ArrayList<IMCStmt> stmts;
		public ArrayList<String> entryLabels;
		public ArrayList<String> exitLabels;

		public Block(LABEL l) {
			this.stmts = new ArrayList<>();

			entryLabels = new ArrayList<>();
			exitLabels = new ArrayList<>();

			addLabel(l);
		}

		public void addLabel(LABEL l){
			entryLabels.add(l.label);
			stmts.add(l);
		}


		public void addCjump(CJUMP j){
			exitLabels.add(j.posLabel);
			exitLabels.add(j.negLabel);
			stmts.add(j);
		}

		public void addJump(JUMP j){
			exitLabels.add(j.label);
			stmts.add(j);
		}

		public void addStatement(IMCStmt s){
			stmts.add(s);
		}

		public String getPrefferedExitLabel(){
			return exitLabels.get(0);
		}
	}
}
