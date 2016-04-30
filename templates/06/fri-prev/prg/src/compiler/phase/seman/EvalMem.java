package compiler.phase.seman;

import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;
import compiler.data.typ.*;

/**
 * @author sliva
 */
public class EvalMem extends FullVisitor {
	
	private final Attributes attrs;
	
	public EvalMem(Attributes attrs) {
		this.attrs = attrs;
	}

	private boolean genericEvalAddr(Expr expr) {
		if ((expr instanceof CompName) || (expr instanceof VarName))
			return true;

		Typ typ = attrs.typAttr.get(expr);
		if ((typ != null) && (typ.actualTyp() instanceof PtrTyp))
			return true;
		else
			return false;
	}

	public void visit(AtomExpr atomExpr) {
		attrs.memAttr.set(atomExpr, false);
	}

	public void visit(BinExpr binExpr) {
		binExpr.fstExpr.accept(this);
		binExpr.sndExpr.accept(this);

		switch (binExpr.oper) {
		case ARR: {
			attrs.memAttr.set(binExpr, true);
			break;
		}
		case REC: {
			attrs.memAttr.set(binExpr, true);
			break;
		}
		default: {
			attrs.memAttr.set(binExpr, false);
			break;
		}
		}
	}

	public void visit(CastExpr castExpr) {
		castExpr.type.accept(this);
		castExpr.expr.accept(this);
		attrs.memAttr.set(castExpr, genericEvalAddr(castExpr.expr));
	}

	public void visit(CompName compName) {
		attrs.memAttr.set(compName, genericEvalAddr(compName));
	}

	public void visit(Exprs exprs) {
		for (int e = 0; e < exprs.numExprs(); e++)
			exprs.expr(e).accept(this);
		attrs.memAttr.set(exprs, genericEvalAddr(exprs.expr(exprs.numExprs() - 1)));
	}
	
	public void visit(ExprError exprError) {
		attrs.memAttr.set(exprError, false);
	}

	public void visit(ForExpr forExpr) {
		forExpr.var.accept(this);
		forExpr.loBound.accept(this);
		forExpr.hiBound.accept(this);
		forExpr.body.accept(this);
		attrs.memAttr.set(forExpr, genericEvalAddr(forExpr));
	}

	public void visit(FunCall funCall) {
		for (int a = 0; a < funCall.numArgs(); a++)
			funCall.arg(a).accept(this);
		attrs.memAttr.set(funCall, genericEvalAddr(funCall));
	}

	public void visit(IfExpr ifExpr) {
		ifExpr.cond.accept(this);
		ifExpr.thenExpr.accept(this);
		ifExpr.elseExpr.accept(this);
		attrs.memAttr.set(ifExpr, genericEvalAddr(ifExpr));
	}

	public void visit(Program program) {
		program.expr.accept(this);
		attrs.memAttr.set(program, genericEvalAddr(program));
	}

	public void visit(UnExpr unExpr) {
		unExpr.subExpr.accept(this);

		switch (unExpr.oper) {
		case VAL: {
			attrs.memAttr.set(unExpr, true);
			break;
		}
		default: {
			attrs.memAttr.set(unExpr, false);
			break;
		}
		}
	}

	public void visit(VarName varName) {
		attrs.memAttr.set(varName, genericEvalAddr(varName));
	}

	public void visit(WhereExpr whereExpr) {
		whereExpr.expr.accept(this);
		for (int d = 0; d < whereExpr.numDecls(); d++)
			whereExpr.decl(d).accept(this);
		attrs.memAttr.set(whereExpr, false);
	}

	public void visit(WhileExpr whileExpr) {
		whileExpr.cond.accept(this);
		whileExpr.body.accept(this);
		attrs.memAttr.set(whileExpr, genericEvalAddr(whileExpr));
	}

}
