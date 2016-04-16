package compiler.phase.seman;

import compiler.data.ast.*;
import compiler.data.ast.attr.Attributes;
import compiler.data.ast.code.FullVisitor;
import compiler.data.typ.*;

/**
 * Created by gregor on 16. 04. 2016.
 */
public class EvalFinalTyp extends FullVisitor {

	private final Attributes attrs;

	public EvalFinalTyp(Attributes attrs) {
		this.attrs = attrs;
	}

	@Override
	public void visit(WhereExpr whereExpr) {
		//Firstly check for circularity
		for (Decl d : whereExpr.decls) {
			if (d instanceof TypeDecl) {
				Typ t = attrs.typAttr.get(d);
				if (!isBaseType(t)) {
					if (((TypName) t).isCircular()) {
						SemAn.signalError("Detected a circular type: " + ((TypName) t).name, whereExpr);
					}
				}
			}
		}

		if (false) {
			//Now attempt to resolve
			for (Decl d : whereExpr.decls) {
				if (d instanceof TypeDecl) {
					Typ t = attrs.typAttr.get(d);
					if (t == null) SemAn.signalError("Type finaliser: canot find TypeDecl's type", whereExpr);

					if (!isBaseType(t)) {
						TypName nt = new TypName(d.name);
						t = t.actualTyp();
						nt.setType(t);
						attrs.typAttr.set(d, nt);
					}
				}
			}
		}

		super.visit(whereExpr);
	}

	private boolean isBaseType(Typ t) {
		return (t instanceof AtomTyp ||
				t instanceof ArrTyp ||
				t instanceof RecTyp);
	}
}
