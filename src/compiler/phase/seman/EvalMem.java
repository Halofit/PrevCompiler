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


	@Override
	public void visit(UnExpr unExpr) {
		super.visit(unExpr);

		switch (unExpr.oper) {
			case MEM:
				Boolean inMem = attrs.memAttr.get(unExpr.subExpr);
				if (inMem == null || !inMem) {
					SemAn.signalError("Can only reference objects in memory.", unExpr);
				}
				break;
			case VAL:
				Typ subT = attrs.typAttr.get(unExpr.subExpr);
				if(subT instanceof PtrTyp) attrs.memAttr.set(unExpr, true);
				break;
		}
	}

	@Override
	public void visit(BinExpr binExpr) {
		super.visit(binExpr);

		Boolean inMem;
		Typ lT;
		Typ rT;

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
				if (lT instanceof ArrTyp && rT instanceof IntegerTyp) attrs.memAttr.set(binExpr, true);
				break;
			case REC:
				//Write
				lT = attrs.typAttr.get(binExpr.fstExpr);
				rT = attrs.typAttr.get(binExpr.sndExpr);
				if (lT != null && rT != null) attrs.memAttr.set(binExpr, true);
				break;

		}
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
		//Maybe do nothing here. Do it in above in BinExpr -> mem
	}

	@Override
	public void visit(WhereExpr whereExpr) {
		for (int d = 0; d < whereExpr.numDecls(); d++) {
			whereExpr.decl(d).accept(this);
		}
		whereExpr.expr.accept(this);
	}
}
