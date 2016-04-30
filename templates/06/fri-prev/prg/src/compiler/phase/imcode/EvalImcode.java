package compiler.phase.imcode;

import java.util.*;

import compiler.common.report.*;
import compiler.data.acc.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;
import compiler.data.frg.*;
import compiler.data.frm.*;
import compiler.data.imc.*;

/**
 * Evaluates intermediate code.
 * 
 * @author sliva
 */
public class EvalImcode extends FullVisitor {

	private final Attributes attrs;

	private final HashMap<String, Fragment> fragments;

	private Stack<CodeFragment> codeFragments = new Stack<CodeFragment>();

	public EvalImcode(Attributes attrs, HashMap<String, Fragment> fragments) {
		this.attrs = attrs;
		this.fragments = fragments;
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
			if (atomExpr.value.equals("true"))
				attrs.imcAttr.set(atomExpr, new CONST(1));
			if (atomExpr.value.equals("false"))
				attrs.imcAttr.set(atomExpr, new CONST(0));
			break;
		case CHAR:
			if (atomExpr.value.charAt(1) == '\'')
				attrs.imcAttr.set(atomExpr, new CONST(atomExpr.value.charAt(2)));
			else
				attrs.imcAttr.set(atomExpr, new CONST(atomExpr.value.charAt(1)));
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

	// TODO
	
	@Override
	public void visit(FunDef funDef) {
		Frame frame = attrs.frmAttr.get(funDef);
		int FP = TEMP.newTempName();
		int RV = TEMP.newTempName();
		CodeFragment tmpFragment = new CodeFragment(frame, FP, RV, null);
		codeFragments.push(tmpFragment);

		for (int p = 0; p < funDef.numPars(); p++)
			funDef.par(p).accept(this);
		funDef.type.accept(this);
		funDef.body.accept(this);

		codeFragments.pop();
		IMCExpr expr = (IMCExpr) attrs.imcAttr.get(funDef.body);
		MOVE move = new MOVE(new TEMP(RV), expr);
		Fragment fragment = new CodeFragment(tmpFragment.frame, tmpFragment.FP, tmpFragment.RV, move);
		attrs.frgAttr.set(funDef, fragment);
		attrs.imcAttr.set(funDef, move);
		fragments.put(fragment.label, fragment);
	}
	
	// TODO

}
