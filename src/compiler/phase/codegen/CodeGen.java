package compiler.phase.codegen;

import compiler.Task;
import compiler.common.report.InternalCompilerError;
import compiler.data.codegen.*;
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

	private HashMap<IMC, InstructionSet> map;

	public CodeGen(Task task) {
		super(task, "codegen");

		map = new HashMap<>();
	}

	public void generateCode() {
		for (Fragment frag : task.fragments.values()) {
			if (frag instanceof CodeFragment) generateFragmentCode((CodeFragment) frag);
		}
	}

	public void generateFragmentCode(CodeFragment frag) {
		frag.stmt.visit(this);
	}

	private InstructionSet getInstrs(IMC imcode){
		InstructionSet instrs = map.get(imcode);
		if(instrs == null) throw new InternalCompilerError();
		return instrs;
	}

	private void setInstrs(IMC imcode, InstructionSet instrs){
		map.put(imcode, instrs);
	}


	public static Instruction CreateInstruction(String mnemonic, Operand... ops) {
		switch (ops.length) {
			case 0:
				return new Label(mnemonic);
			case 1:
			case 2:
			case 3:
				return new Mnemonic(mnemonic, ops);
			default:
				throw new InternalCompilerError();
		}
	}


	public void tile(BINOP binop) {
		binop.expr1.visit(this);
		binop.expr2.visit(this);

		InstructionSet is1 = getInstrs(binop.expr1);
		InstructionSet is2 = getInstrs(binop.expr2);

		InstructionSet ownis = new InstructionSet();
		ownis.add(is1);
		ownis.add(is2);

		ownis.set(new VirtualRegister());
		ownis.add(new Mnemonic("ADD", is1.returnRegister, is2.returnRegister));

		setInstrs(binop, ownis);
	}

	public void tile(CALL call) {
		for (IMCExpr arg : call.args) {
			arg.visit(this);
		}

	}

	public void tile(CJUMP cjump) {
		cjump.cond.visit(this);
	}


	public void tile(CONST constant) {

	}


	public void tile(ESTMT estmt) {
		estmt.expr.visit(this);
	}


	public void tile(JUMP jump) {

	}


	public void tile(LABEL label) {

	}


	public void tile(MEM mem) {
		mem.addr.visit(this);
	}


	public void tile(MOVE move) {
		move.src.visit(this);
		move.dst.visit(this);
	}


	public void tile(NAME name) {

	}


	public void tile(NOP nop) {

	}


	public void tile(SEXPR sexpr) {
		sexpr.stmt.visit(this);
		sexpr.expr.visit(this);
	}


	public void tile(STMTS stmts) {
		for (IMCStmt stmt : stmts.stmts) {
			stmt.visit(this);
		}
	}


	public void tile(TEMP temp) {

	}


	public void tile(UNOP unop) {
		unop.expr.visit(this);
	}


}
