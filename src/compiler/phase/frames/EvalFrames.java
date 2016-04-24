package compiler.phase.frames;

import compiler.data.acc.Access;
import compiler.data.acc.OffsetAccess;
import compiler.data.acc.StaticAccess;
import compiler.data.ast.*;
import compiler.data.ast.attr.Attributes;
import compiler.data.ast.code.FullVisitor;
import compiler.data.frm.Frame;
import compiler.data.typ.PtrTyp;
import compiler.data.typ.Typ;
import compiler.data.typ.VoidTyp;

/**
 * Frame and access evaluator.
 *
 * @author sliva
 */
public class EvalFrames extends FullVisitor {

	private final Attributes attrs;

	private int level; //Static level of function and variables
	private long nestedFunctionCounter; //Counter of non-top level functions, used for name generation

	private long localVariablesSize; //for calculating size of all local variables
	private long outCallSize; //for calculating the size of the output part of the stack frame (SL + output parameters)

	private long parametersSize; //For calculating parameter's offset and the inpCallSize -> also includes Static Link !!!!!


	private static final String functionNamePrefix = "function";


	public EvalFrames(Attributes attrs) {
		this.attrs = attrs;
		level = 0;
	}


	@Override
	public void visit(WhereExpr whereExpr) {
		//As allways reverse the ordering
		//TODO is this required
		for (int d = 0; d < whereExpr.numDecls(); d++) {
			whereExpr.decl(d).accept(this);
		}

		whereExpr.expr.accept(this);
	}

	@Override
	public void visit(FunDef funDef) {
		//Backup prevous values
		long variableBak = localVariablesSize;
		long outCallBak = outCallSize;
		long parametersSizeBak = parametersSize;

		//reset for this function values
		localVariablesSize = 0;
		outCallSize = 0;
		parametersSize = 0;

		parametersSize += new PtrTyp(new VoidTyp()).size(); //Add the size of a pointer for the static pointer

		level++;
		super.visit(funDef);
		level--;

		String label;
		if (level == 0) {
			//TODO handle multiple decls ->
			label = "_" + funDef.name;
		} else {
			label = functionNamePrefix + nestedFunctionCounter + "___" + funDef.name;
			nestedFunctionCounter++;
		}

		long inpCallSize = Math.max(parametersSize, attrs.typAttr.get(funDef.type).actualTyp().size());

		Frame frame = new Frame(level, label, inpCallSize, localVariablesSize, 0, 0, outCallSize);
		attrs.frmAttr.set(funDef, frame);


		//Restore previous values
		localVariablesSize = variableBak;
		outCallSize = outCallBak;
		parametersSize = parametersSizeBak;

		//Add this functions size:
		outCallSize = Math.max(inpCallSize, outCallSize);
	}

	@Override
	public void visit(FunDecl funDecl) {
		//backup
		long parametersSizeBak = parametersSize;
		parametersSize = 0;
		parametersSize += new PtrTyp(new VoidTyp()).size(); //Add the size of a pointer for the static pointer

		super.visit(funDecl);

		long inpCallSize = Math.max(parametersSize, attrs.typAttr.get(funDecl.type).actualTyp().size());

		//Add this functions size:
		outCallSize = Math.max(inpCallSize, outCallSize);

		//restore
		parametersSize = parametersSizeBak;
	}


	@Override
	public void visit(VarDecl varDecl) {
		super.visit(varDecl);
		Typ t = attrs.typAttr.get(varDecl).actualTyp();
		localVariablesSize += t.size();


		Access acc;
		if (level == 0) {
			//static variable
			//TODO handle multiple declarations
			String label = "_" + varDecl.name;
			acc = new StaticAccess(label, t.size());
		} else {
			//stack variable
			long offset = -localVariablesSize;
			acc = new OffsetAccess(offset, t.size());
		}

		attrs.accAttr.set(varDecl, acc);
	}


	@Override
	public void visit(ParDecl parDecl) {
		super.visit(parDecl);

		Typ t = attrs.typAttr.get(parDecl);
		t = t.actualTyp();

		//For parameters offset goes before increment (since they go the other way)
		Access acc = new OffsetAccess(parametersSize, t.size());
		attrs.accAttr.set(parDecl, acc);

		parametersSize += t.size();

	}

	private long recordSize;

	@Override
	public void visit(RecType recType) {
		//Not necessary, just good practice
		long recordSizeBak = recordSize;

		recordSize = 0;
		super.visit(recType);

		recordSize = recordSizeBak;
	}

	@Override
	public void visit(CompDecl compDecl) {
		super.visit(compDecl);
		Typ t = attrs.typAttr.get(compDecl);
		t = t.actualTyp();
		recordSize += t.size();

		Access a = new OffsetAccess(recordSize, t.size());
		attrs.accAttr.set(compDecl, a);
	}
}
