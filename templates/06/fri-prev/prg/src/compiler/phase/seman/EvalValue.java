package compiler.phase.seman;

import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;

/**
 * Computes the value of simple integer constant expressions.
 * 
 * <p>
 * Simple integer constant expressions consists of integer constants and five
 * basic arithmetic operators (<code>ADD</code>, <code>SUB</code>,
 * <code>MUL</code>, <code>DIV</code>, and <code>MOD</code>).
 * </p>
 * 
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
	
	public void visit(AtomExpr atomExpr) {
		switch (atomExpr.type) {
		case INTEGER:
			try {
				Long value = new Long(atomExpr.value);
				attrs.valueAttr.set(atomExpr, value);
			} catch (NumberFormatException ex) {
				Report.warning(atomExpr, "Illegal integer value.");
			}
			break;
		default:
		}
	}

	public void visit(BinExpr binExpr) {
		binExpr.fstExpr.accept(this);
		binExpr.sndExpr.accept(this);
		switch (binExpr.oper) {
		case ADD: {
			Long fstValue = attrs.valueAttr.get(binExpr.fstExpr);
			Long sndValue = attrs.valueAttr.get(binExpr.sndExpr);
			if ((fstValue != null) && (sndValue != null)) {
				attrs.valueAttr.set(binExpr, fstValue + sndValue);
			}
			break;
		}
		case SUB: {
			Long fstValue = attrs.valueAttr.get(binExpr.fstExpr);
			Long sndValue = attrs.valueAttr.get(binExpr.sndExpr);
			if ((fstValue != null) && (sndValue != null)) {
				attrs.valueAttr.set(binExpr, fstValue - sndValue);
			}
			break;
		}
		case MUL: {
			Long fstValue = attrs.valueAttr.get(binExpr.fstExpr);
			Long sndValue = attrs.valueAttr.get(binExpr.sndExpr);
			if ((fstValue != null) && (sndValue != null)) {
				attrs.valueAttr.set(binExpr, fstValue * sndValue);
			}
			break;
		}
		case DIV: {
			Long fstValue = attrs.valueAttr.get(binExpr.fstExpr);
			Long sndValue = attrs.valueAttr.get(binExpr.sndExpr);
			if ((fstValue != null) && (sndValue != null)) {
				attrs.valueAttr.set(binExpr, fstValue / sndValue);
			}
			break;
		}
		case MOD: {
			Long fstValue = attrs.valueAttr.get(binExpr.fstExpr);
			Long sndValue = attrs.valueAttr.get(binExpr.sndExpr);
			if ((fstValue != null) && (sndValue != null)) {
				attrs.valueAttr.set(binExpr, fstValue % sndValue);
			}
			break;
		}
		default:
		}
	}

	public void visit(UnExpr unExpr) {
		unExpr.subExpr.accept(this);
		switch (unExpr.oper) {
		case ADD: {
			Long subExpr = attrs.valueAttr.get(unExpr.subExpr);
			if (subExpr != null)
				attrs.valueAttr.set(unExpr, +subExpr);
			break;
		}
		case SUB: {
			Long subExpr = attrs.valueAttr.get(unExpr.subExpr);
			if (subExpr != null)
				attrs.valueAttr.set(unExpr, -subExpr);
			break;
		}
		default:
		}
	}

}
