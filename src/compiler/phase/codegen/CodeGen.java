package compiler.phase.codegen;

import compiler.Task;
import compiler.common.report.InternalCompilerError;
import compiler.data.ast.attr.Attribute;
import compiler.data.codegen.Instruction;
import compiler.data.codegen.Label;
import compiler.data.codegen.Mnemonic;
import compiler.data.codegen.Operand;
import compiler.data.frg.CodeFragment;
import compiler.data.frg.Fragment;
import compiler.data.imc.*;
import compiler.phase.Phase;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by gregor on 20.5.2016.
 */
public class CodeGen extends Phase {

	private LinkedList<Instruction> instrs;
	private HashMap<IMC, Instruction[]> map;

	public CodeGen(Task task) {
		super(task, "codegen");

		instrs = new LinkedList<>();
		map = new HashMap<>();
	}

	public void generateCode(){
		for (Fragment frag : task.fragments.values()) {
			if(frag instanceof CodeFragment) generateFragmentCode((CodeFragment) frag);
		}
	}

	public void generateFragmentCode(CodeFragment frag){
		tile(frag.stmt);
	}


	public static Instruction CreateInstruction(String mnemonic, Operand... ops){
		switch(ops.length){
			case 0: return new Label(mnemonic);
			case 1:
			case 2:
			case 3:
				return new Mnemonic(mnemonic, ops);
			default:
				throw new InternalCompilerError();
		}
	}

	private void tile(BINOP binop){

	}

	private void tile(CALL call){

	}

	private void tile(CJUMP){

	}


	private void tile(CONST){

	}


	private void tile(ESTMT){

	}


	private void tile(JUMP){

	}


	private void tile(LABEL){

	}


	private void tile(MEM){

	}


	private void tile(MOVE){

	}


	private void tile(NAME){

	}


	private void tile(NOP){

	}


	private void tile(SEXPR){

	}


	private void tile(STMTS){

	}


	private void tile(TEMP){

	}


	private void tile(UNOP){

	}


}
