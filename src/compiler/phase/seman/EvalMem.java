package compiler.phase.seman;

import compiler.data.ast.*;
import compiler.data.ast.attr.Attributes;
import compiler.data.ast.code.FullVisitor;
import compiler.data.typ.ArrTyp;
import compiler.data.typ.IntegerTyp;
import compiler.data.typ.PtrTyp;
import compiler.data.typ.Typ;

/**
 * @author sliva
 */
public class EvalMem extends FullVisitor {
	
	private final Attributes attrs;
	
	public EvalMem(Attributes attrs) {
		this.attrs = attrs;
	}

	@Override
	public void visit(UnExpr unExpr) {
		super.visit(unExpr);

		boolean result = false;

		switch (unExpr.oper) {
			case MEM:
				//just check
				Boolean inMem = attrs.memAttr.get(unExpr.subExpr);
				if (inMem == null || !inMem) {
					SemAn.signalError("Can only reference objects in memory.", unExpr);
				}
				break;
			case VAL:
				Typ subT = attrs.typAttr.get(unExpr.subExpr);
				if (subT.actualTyp() instanceof PtrTyp) result = true;
				break;
		}

		attrs.memAttr.set(unExpr, result);
	}

	@Override
	public void visit(BinExpr binExpr) {
		super.visit(binExpr);

		Boolean inMem;
		Typ lT;
		Typ rT;

		boolean result = false;

		switch (binExpr.oper) {
			case ASSIGN:
				//Just check
				inMem = attrs.memAttr.get(binExpr.fstExpr);
				if (inMem == null || !inMem) {
					SemAn.signalError("Left side of the assignement must be in memory.", binExpr);
				}
				break;
			case ARR:
				lT = attrs.typAttr.get(binExpr.fstExpr);
				rT = attrs.typAttr.get(binExpr.sndExpr);
				if (lT.actualTyp() instanceof ArrTyp &&
					rT.actualTyp() instanceof IntegerTyp) {
					result = true;
				}
				break;
			case REC:
				//Write
				lT = attrs.typAttr.get(binExpr.fstExpr);
				rT = attrs.typAttr.get(binExpr.sndExpr);
				if (lT.actualTyp() != null && rT.actualTyp() != null) {
					result = true;
				}
				break;
		}

		attrs.memAttr.set(binExpr, result);
	}

	@Override
	public void visit(VarName varName) {
		super.visit(varName);

		Typ varT = attrs.typAttr.get(varName);
		if (varT != null) {
			attrs.memAttr.set(varName, true);
		} else {
			SemAn.signalError("Var type has no type, cannot be in memory.", varName); //Probably redundant
		}
	}

	@Override
	public void visit(CompName compName) {
		super.visit(compName);
		attrs.memAttr.set(compName, false);
		//Maybe do nothing here. Do it in above in BinExpr -> mem
	}

	@Override
	public void visit(WhereExpr whereExpr) {
		for (int d = 0; d < whereExpr.numDecls(); d++) {
			whereExpr.decl(d).accept(this);
		}
		whereExpr.expr.accept(this);

		attrs.memAttr.set(whereExpr, false);
	}

	//Other exprs

	@Override
	public void visit(AtomExpr atomExpr) {
		super.visit(atomExpr);
		attrs.memAttr.set(atomExpr, false);
	}

	@Override
	public void visit(CastExpr castExpr) {
		super.visit(castExpr);
		attrs.memAttr.set(castExpr, false);
	}

	@Override
	public void visit(Exprs exprs) {
		super.visit(exprs);
		attrs.memAttr.set(exprs, false);
	}

	@Override
	public void visit(ExprError exprError) {
		super.visit(exprError);
		attrs.memAttr.set(exprError, false);
	}

	@Override
	public void visit(ForExpr forExpr) {
		super.visit(forExpr);
		attrs.memAttr.set(forExpr, false);
	}

	@Override
	public void visit(FunCall funCall) {
		super.visit(funCall);
		attrs.memAttr.set(funCall, false);
	}

	@Override
	public void visit(IfExpr ifExpr) {
		super.visit(ifExpr);
		attrs.memAttr.set(ifExpr, false);
	}

	@Override
	public void visit(Program program) {
		super.visit(program);
		attrs.memAttr.set(program, false);
	}


	@Override
	public void visit(WhileExpr whileExpr) {
		super.visit(whileExpr);
		attrs.memAttr.set(whileExpr, false);
	}
}
