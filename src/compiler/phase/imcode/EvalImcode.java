package compiler.phase.imcode;

import compiler.common.report.InternalCompilerError;
import compiler.common.report.Report;
import compiler.data.acc.Access;
import compiler.data.acc.OffsetAccess;
import compiler.data.acc.StaticAccess;
import compiler.data.ast.*;
import compiler.data.ast.attr.Attributes;
import compiler.data.ast.code.FullVisitor;
import compiler.data.frg.CodeFragment;
import compiler.data.frg.ConstFragment;
import compiler.data.frg.DataFragment;
import compiler.data.frg.Fragment;
import compiler.data.frm.Frame;
import compiler.data.imc.*;
import compiler.data.typ.PtrTyp;
import compiler.data.typ.Typ;
import compiler.data.typ.VoidTyp;
import compiler.phase.frames.EvalFrameOut;

import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

/**
 * Evaluates intermediate code.
 *
 * @author sliva
 */
public class EvalImcode extends FullVisitor {

	private final Attributes attrs;

	private final HashMap<String, Fragment> fragments;

	private Stack<CodeFragment> codeFragments = new Stack<>();

	private static long pointerSize = new PtrTyp(new VoidTyp()).size();

	public EvalImcode(Attributes attrs, HashMap<String, Fragment> fragments) {
		this.attrs = attrs;
		this.fragments = fragments;
	}


	@Override
	public void visit(Program program) {
		final String label = "_";

		Frame frame = new Frame(0, label, 0, 0, 0, 0, EvalFrameOut.globalProgramOutSize);
		CodeFragment tempFrag = new CodeFragment(frame, 0, 0, null);
		codeFragments.push(tempFrag);
		super.visit(program);
		codeFragments.pop();

		IMC globalProg = attrs.imcAttr.get(program.expr);
		if (globalProg == null) {
			throw new InternalCompilerError();
		}
		if (globalProg instanceof IMCExpr) {
			globalProg = new ESTMT((IMCExpr) globalProg);
		}

		CodeFragment frag = new CodeFragment(frame, 0, 0, (IMCStmt) globalProg);
		attrs.frgAttr.set(program, frag);
		attrs.imcAttr.set(program, globalProg);
		fragments.put(frag.label, frag);
	}

	@Override
	public void visit(FunDef funDef) {
		Frame frame = attrs.frmAttr.get(funDef);
		int FP = TEMP.newTempName();
		int RV = TEMP.newTempName();
		CodeFragment tmpFragment = new CodeFragment(frame, FP, RV, null);
		codeFragments.push(tmpFragment);

		for (int p = 0; p < funDef.numPars(); p++) {
			funDef.par(p).accept(this);
		}
		funDef.type.accept(this);
		funDef.body.accept(this);

		codeFragments.pop();
		IMC preExpr = attrs.imcAttr.get(funDef.body);
		IMCExpr expr;
		if (preExpr instanceof IMCStmt) {
			expr = new SEXPR((IMCStmt) preExpr, new NOP());
		} else {
			expr = (IMCExpr) preExpr;
		}

		MOVE move = new MOVE(new TEMP(RV), expr);
		Fragment fragment = new CodeFragment(tmpFragment.frame, tmpFragment.FP, tmpFragment.RV, move);
		attrs.frgAttr.set(funDef, fragment);
		attrs.imcAttr.set(funDef, move);
		fragments.put(fragment.label, fragment);
	}

	@Override
	public void visit(FunDecl funDecl) {
		//super.visit(funDecl);
		//NOTHING don't even go deeper, probably
	}


	@Override
	public void visit(BinExpr binExpr) {
		super.visit(binExpr);

		IMC code;

		IMCExpr left = (IMCExpr) attrs.imcAttr.get(binExpr.fstExpr);
		IMCExpr right = (IMCExpr) attrs.imcAttr.get(binExpr.sndExpr);
		Typ t = attrs.typAttr.get(binExpr);


		switch (binExpr.oper) {
			case ASSIGN:
				code = new MOVE(left, right);
				break;
			case OR:
				code = new BINOP(BINOP.Oper.OR, left, right);
				break;
			case AND:
				code = new BINOP(BINOP.Oper.AND, left, right);
				break;
			case EQU:
				code = new BINOP(BINOP.Oper.EQU, left, right);
				break;
			case NEQ:
				code = new BINOP(BINOP.Oper.NEQ, left, right);
				break;
			case LTH:
				code = new BINOP(BINOP.Oper.LTH, left, right);
				break;
			case GTH:
				code = new BINOP(BINOP.Oper.GTH, left, right);
				break;
			case LEQ:
				code = new BINOP(BINOP.Oper.LEQ, left, right);
				break;
			case GEQ:
				code = new BINOP(BINOP.Oper.GEQ, left, right);
				break;
			case ADD:
				code = new BINOP(BINOP.Oper.ADD, left, right);
				break;
			case SUB:
				code = new BINOP(BINOP.Oper.SUB, left, right);
				break;
			case MUL:
				code = new BINOP(BINOP.Oper.MUL, left, right);
				break;
			case DIV:
				code = new BINOP(BINOP.Oper.DIV, left, right);
				break;
			case MOD:
				code = new BINOP(BINOP.Oper.MOD, left, right);
				break;
			case ARR:
				//We need to remove the variable dereferencement on the left
				if(!(left instanceof MEM)) throw new InternalCompilerError();
				left = ((MEM) left).addr;

				BINOP indexedAccess = new BINOP(BINOP.Oper.MUL, right, new CONST(t.size()));
				code = new MEM(new BINOP(BINOP.Oper.ADD, left, indexedAccess), t.size());
				break;
			case REC:
				//We need to remove the variable dereferencement on the left
				if(!(left instanceof MEM)) throw new InternalCompilerError();
				left = ((MEM) left).addr;
				code = new MEM(new BINOP(BINOP.Oper.ADD, left, right), t.size());
				break;
			default:
				throw new InternalCompilerError();
		}

		attrs.imcAttr.set(binExpr, code);
	}

	@Override
	public void visit(UnExpr unExpr) {
		super.visit(unExpr);

		IMC code;
		IMCExpr expr = (IMCExpr) attrs.imcAttr.get(unExpr.subExpr);

		switch (unExpr.oper) {
			case ADD:
				code = new UNOP(UNOP.Oper.ADD, expr);
				break;
			case SUB:
				code = new UNOP(UNOP.Oper.SUB, expr);
				break;
			case NOT:
				code = new UNOP(UNOP.Oper.NOT, expr);
				break;
			case VAL:
				Typ t = attrs.typAttr.get(unExpr);
				code = new MEM(expr, t.size());
				break;
			case MEM:
				if(!(expr instanceof MEM)) throw new InternalCompilerError();
				//Remove the MEM dereferencing, and return the address directly
				code = ((MEM)expr).addr;
				break;
			default:
				throw new InternalCompilerError();
		}

		attrs.imcAttr.set(unExpr, code);
	}

	@Override
	public void visit(CompName compName) {
		super.visit(compName);

		CompDecl decl = (CompDecl) attrs.declAttr.get(compName);
		OffsetAccess acc = (OffsetAccess) attrs.accAttr.get(decl);

		attrs.imcAttr.set(compName, new CONST(acc.offset));
	}


	@Override
	public void visit(AtomExpr atomExpr) {
		switch (atomExpr.type) {
			case INTEGER:
				try {
					long value = Long.parseLong(atomExpr.value);
					attrs.imcAttr.set(atomExpr, new CONST(value));
				} catch (NumberFormatException ex) {
					Report.warning(atomExpr, "Illegal integer constant.");
				}
				break;
			case BOOLEAN:
				if (atomExpr.value.equals("true")) {
					attrs.imcAttr.set(atomExpr, new CONST(1));
				}
				if (atomExpr.value.equals("false")) {
					attrs.imcAttr.set(atomExpr, new CONST(0));
				}
				break;
			case CHAR:
				char nested;
				if (atomExpr.value.charAt(1) == '\\') {
					char secondChar = atomExpr.value.charAt(2);
					switch(secondChar){
						case 't':
							nested = '\t';
							break;
						case 'n':
							nested = '\n';
							break;
						default:
							nested = secondChar;
					}
				} else {
					nested = atomExpr.value.charAt(1);
				}
				attrs.imcAttr.set(atomExpr, new CONST(nested));
				break;
			case STRING:
				String label = LABEL.newLabelName();
				attrs.imcAttr.set(atomExpr, new NAME(label));
				ConstFragment fragment = new ConstFragment(label, atomExpr.value);
				attrs.frgAttr.set(atomExpr, fragment);
				fragments.put(fragment.label, fragment);
				break;
			case PTR:
				attrs.imcAttr.set(atomExpr, new CONST(0));
				break;
			case VOID:
				attrs.imcAttr.set(atomExpr, new NOP());
				break;
		}
	}


	@Override
	public void visit(VarName varName) {
		super.visit(varName);

		VarDecl decl = (VarDecl) attrs.declAttr.get(varName);
		Typ t = attrs.typAttr.get(decl);
		Access a = attrs.accAttr.get(decl);

		IMC code;


		if (a instanceof OffsetAccess) {
			OffsetAccess acc = (OffsetAccess) a;

			IMCExpr fpTemp = new TEMP(codeFragments.peek().FP);

			int levelDiff = codeFragments.peek().frame.level - acc.level;
			if (levelDiff < 0) throw new InternalCompilerError();

			while (levelDiff != 0) {
				fpTemp = new MEM(fpTemp, pointerSize);
				levelDiff--;
			}
			code = new BINOP(BINOP.Oper.ADD, fpTemp, new CONST(acc.offset));
		} else {
			StaticAccess acc = (StaticAccess) a;
			code = new NAME(acc.label);
		}
		code = new MEM((IMCExpr) code, t.size());

		attrs.imcAttr.set(varName, code);
	}

	@Override
	public void visit(FunCall funCall) {
		super.visit(funCall);

		FunDecl decl = (FunDecl) attrs.declAttr.get(funCall);
		Frame frame = attrs.frmAttr.get(decl);

		//caller_level - callee_level
		int levelDiff = codeFragments.peek().frame.level - frame.level;

		if (levelDiff < -1) throw new InternalCompilerError();

		IMCExpr staticLink = new TEMP(codeFragments.peek().FP);

		//If not calling a global function (callig a global just pass something irelevant)
		if(!frame.label.startsWith("_")){
			long counter = -1;
			while (counter < levelDiff) {
				staticLink = new MEM(staticLink, pointerSize);
				counter++;
			}
		}

		Vector<IMCExpr> exprs = new Vector<>();
		Vector<Long> widths = new Vector<>();
		exprs.add(staticLink);
		widths.add(pointerSize);

		for (Expr arg : funCall.args) {
			IMCExpr code = (IMCExpr) attrs.imcAttr.get(arg);
			Typ t = attrs.typAttr.get(arg);

			exprs.add(code);
			widths.add(t.size());
		}

		IMC code = new CALL(frame.label, exprs, widths);
		attrs.imcAttr.set(funCall, code);
	}

	@Override
	public void visit(Exprs exprs) {
		super.visit(exprs);

		IMC code = attrs.imcAttr.get(exprs.expr(0));
		if (code instanceof IMCExpr) {
			code = new ESTMT((IMCExpr) code);
		}

		for (int i = 1; i < exprs.numExprs() - 1; i++) {
			IMC expCode = attrs.imcAttr.get(exprs.expr(i));
			if (expCode instanceof IMCExpr) {
				expCode = new ESTMT((IMCExpr) expCode);
			}

			Vector<IMCStmt> nestedStmts = new Vector<>();
			nestedStmts.add((IMCStmt) code);
			nestedStmts.add((IMCStmt) expCode);
			code = new STMTS(nestedStmts);
		}

		IMC lastCode = attrs.imcAttr.get(exprs.lastExpr());

		if (lastCode instanceof IMCStmt) {
			Vector<IMCStmt> nestedStmts = new Vector<>();
			nestedStmts.add((IMCStmt) code);
			nestedStmts.add((IMCStmt) lastCode);
			code = new STMTS(nestedStmts);
			code = new SEXPR((IMCStmt) code, new NOP());
		} else {
			code = new SEXPR((IMCStmt) code, (IMCExpr) lastCode);
		}

		attrs.imcAttr.set(exprs, code);
	}

	//==================================
	//Control flow
	//==================================

	@Override
	public void visit(WhileExpr whileExpr) {
		super.visit(whileExpr);

		IMC condImc = attrs.imcAttr.get(whileExpr.cond);
		IMC bodyImc = attrs.imcAttr.get(whileExpr.body);

		if (bodyImc instanceof IMCExpr) {
			bodyImc = new ESTMT((IMCExpr) bodyImc);
		}

		LABEL entryLabel = new LABEL(LABEL.newLabelName());
		LABEL exitLabel = new LABEL(LABEL.newLabelName());
		LABEL loopLabel = new LABEL(LABEL.newLabelName());

		CJUMP condJump = new CJUMP((IMCExpr) condImc, loopLabel.label, exitLabel.label);
		JUMP entryJump = new JUMP(entryLabel.label);


		Vector<IMCStmt> wStmts = new Vector<>();

		wStmts.add(entryLabel);
		wStmts.add(condJump);
		wStmts.add(loopLabel);
		wStmts.add((IMCStmt) bodyImc);
		wStmts.add(entryLabel);
		wStmts.add(entryJump);
		wStmts.add(exitLabel);

		IMC code = new STMTS(wStmts);
		attrs.imcAttr.set(whileExpr, code);
	}

	@Override
	public void visit(IfExpr ifExpr) {
		super.visit(ifExpr);

		IMCExpr condExpr = (IMCExpr) attrs.imcAttr.get(ifExpr.cond);
		IMC thenExpr = attrs.imcAttr.get(ifExpr.thenExpr);
		IMC elseExpr = attrs.imcAttr.get(ifExpr.elseExpr);

		if (thenExpr instanceof IMCExpr) {
			thenExpr = new ESTMT((IMCExpr) thenExpr);
		}

		if (elseExpr instanceof IMCExpr) {
			elseExpr = new ESTMT((IMCExpr) elseExpr);
		}

		LABEL thenLabel = new LABEL(LABEL.newLabelName());
		LABEL elseLabel = new LABEL(LABEL.newLabelName());
		LABEL exitLabel = new LABEL(LABEL.newLabelName());


		Vector<IMCStmt> ifStatements = new Vector<>();
		ifStatements.add(new CJUMP(condExpr, thenLabel.label, elseLabel.label));
		ifStatements.add(thenLabel);
		ifStatements.add((IMCStmt) thenExpr);
		ifStatements.add(new JUMP(exitLabel.label));
		ifStatements.add(elseLabel);
		ifStatements.add((IMCStmt) elseExpr);
		ifStatements.add(exitLabel);
		IMC code = new STMTS(ifStatements);

		attrs.imcAttr.set(ifExpr, code);
	}

	@Override
	public void visit(ForExpr forExpr) {
		super.visit(forExpr);

		IMCExpr forVar = (IMCExpr) attrs.imcAttr.get(forExpr.var);
		IMCExpr loBound = (IMCExpr) attrs.imcAttr.get(forExpr.loBound);
		IMCExpr hiBound = (IMCExpr) attrs.imcAttr.get(forExpr.hiBound);
		IMC forBody = attrs.imcAttr.get(forExpr.body);

		if (forBody instanceof IMCExpr) {
			forBody = new ESTMT((IMCExpr) forBody);
		}

		LABEL condLabel = new LABEL(LABEL.newLabelName());
		LABEL loopLabel = new LABEL(LABEL.newLabelName());
		LABEL exitLabel = new LABEL(LABEL.newLabelName());


		IMCStmt initExpr = new MOVE(forVar, loBound);

		IMCExpr condImc;
		condImc = new BINOP(BINOP.Oper.LEQ, forVar, hiBound);
		CJUMP condJump = new CJUMP(condImc, loopLabel.label, exitLabel.label);
		JUMP entryJump = new JUMP(condLabel.label);
		IMCStmt incrementStatement = new MOVE(forVar, new BINOP(BINOP.Oper.ADD, forVar, new CONST(1)));

		Vector<IMCStmt> forStmts = new Vector<>();
		forStmts.add(initExpr);
		forStmts.add(condLabel);
		forStmts.add(condJump);
		forStmts.add(loopLabel);
		forStmts.add((IMCStmt) forBody);
		forStmts.add(incrementStatement);
		forStmts.add(entryJump);
		forStmts.add(exitLabel);
		IMC code = new STMTS(forStmts);

		attrs.imcAttr.set(forExpr, code);
	}


	@Override
	public void visit(VarDecl varDecl) {
		super.visit(varDecl);

		Access a = attrs.accAttr.get(varDecl);

		if (a instanceof StaticAccess) {
			StaticAccess acc = (StaticAccess) a;
			Typ t = attrs.typAttr.get(varDecl);
			DataFragment fragment = new DataFragment(acc.label, t.size());

			fragments.put(acc.label, fragment);
		}
	}


	//==================================
	// Simple copies - This means that the node adds no code of it's own, and simply copies its nested node
	//==================================


	@Override
	public void visit(CastExpr castExpr) {
		super.visit(castExpr);

		IMC nestedImc = attrs.imcAttr.get(castExpr.expr);
		attrs.imcAttr.set(castExpr, nestedImc);
	}


	@Override
	public void visit(WhereExpr whereExpr) {
		super.visit(whereExpr);

		IMC nestedImc = attrs.imcAttr.get(whereExpr.expr);
		attrs.imcAttr.set(whereExpr, nestedImc);
	}

}
