package compiler.phase.seman;

import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;

/**
 * Computes the value of simple integer constant expressions.
 * <p>
 * <p>
 * Simple integer constant expressions consists of integer constants and five
 * basic arithmetic operators (<code>ADD</code>, <code>SUB</code>,
 * <code>MUL</code>, <code>DIV</code>, and <code>MOD</code>).
 * </p>
 * <p>
 * <p>
 * This is needed during type resolving and type checking to compute the correct
 * array types.
 * </p>
 *
 * @author sliva
 */
public class EvalValue extends FullVisitor {

	private final Attributes attrs;

	public EvalValue(Attributes attrs) {
		this.attrs = attrs;
	}


	@Override
	public void visit(AtomExpr atomExpr) {
		super.visit(atomExpr);
		if(atomExpr.type == AtomExpr.AtomTypes.INTEGER){
			try {
				attrs.valueAttr.set(atomExpr, Long.parseLong(atomExpr.value));
			}catch (NumberFormatException nfe){
				throw new CompilerError("Invalid integer cnstant " + atomExpr.value);
			}
		}
	}


	@Override
	public void visit(BinExpr binExpr) {
		super.visit(binExpr);
		Long left = attrs.valueAttr.get(binExpr.fstExpr);
		Long right = attrs.valueAttr.get(binExpr.sndExpr);

		if(left != null && right != null){
			//Both parts of the expression can be evalueated at compile time
			//We only care about binary operators {+,-,*,/,%}
			Long value;
			switch(binExpr.oper){
				case ADD :
					value = left + right;
					break;
				case SUB :
					value = left - right;
					break;
				case MUL :
					value = left * right;
					break;
				case DIV :
					value = left / right;
					break;
				case MOD :
					value = left % right;
					break;
				default:
					value = null;
			}
			attrs.valueAttr.set(binExpr, value);
		}

	}

	@Override
	public void visit(Exprs exprs) {
		super.visit(exprs);
		//Value of expressions is the value of the final expression
		Long value = attrs.valueAttr.get(exprs.lastExpr());
		attrs.valueAttr.set(exprs, value);
	}

	@Override
	public void visit(UnExpr unExpr) {
		super.visit(unExpr);

		Long op = attrs.valueAttr.get(unExpr.subExpr);
		if(op != null){
			Long value;

			switch (unExpr.oper){
				case ADD:
					value = op;
					break;
				case SUB:
					value = -op;
					break;
				default:
					value = null;
			}

			attrs.valueAttr.set(unExpr, value);
		}
	}

}
