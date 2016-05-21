package compiler.phase.frames;

import compiler.common.report.PhaseErrors.StackFrameError;
import compiler.common.report.Report;
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

import java.util.ArrayList;

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

	private long parametersSize; //For calculating parameter's offset and the inpCallSize -> also includes Static Link !!!!!

	private static final String functionNamePrefix = "fun";

	private ArrayList<String> topLevelLabels;

	public EvalFrames(Attributes attrs) {
		this.attrs = attrs;
		level = 0;
		topLevelLabels = new ArrayList<>();
	}


	@Override
	public void visit(WhereExpr whereExpr) {
		//As always reverse the ordering

		for (int d = 0; d < whereExpr.numDecls(); d++) {
			whereExpr.decl(d).accept(this);
		}

		whereExpr.expr.accept(this);
	}


	@Override
	public void visit(FunDef funDef) {
		//Backup prevous values
		long variableBak = localVariablesSize;
		long parametersSizeBak = parametersSize;

		//reset for this function values
		localVariablesSize = 0;
		parametersSize = 0;

		parametersSize += new PtrTyp(new VoidTyp()).size(); //Add the size of a pointer for the static pointer

		level++;
		super.visit(funDef);
		level--;

		String label;
		if (level == 0) {
			label = "_" + funDef.name;
			if (topLevelLabels.contains(label)) {
				throw new StackFrameError(funDef.toString() + "| Duplicate top-level name detected (" + funDef.name + ").");
			}
			topLevelLabels.add(label);
		} else {
			label = functionNamePrefix + nestedFunctionCounter + "___" + funDef.name;
			nestedFunctionCounter++;
		}

		long inpCallSize = Math.max(parametersSize, attrs.typAttr.get(funDef.type).actualTyp().size());

		//NOTE: level + 1 is so that the function level is the same as the level of variables inside the function
		//			this should not effect any other check, since difference between function levels remains the same
		Frame frame = new Frame(level+1, label, inpCallSize, localVariablesSize, 0, 0, 0);
		attrs.frmAttr.set(funDef, frame);


		//Restore previous values
		localVariablesSize = variableBak;
		parametersSize = parametersSizeBak;
	}

	@Override
	public void visit(FunDecl funDecl) {
		//Backup prevous values
		long parametersSizeBak = parametersSize;

		//reset for this function values
		parametersSize = 0;

		parametersSize += new PtrTyp(new VoidTyp()).size(); //Add the size of a pointer for the static pointer

		level++;
		super.visit(funDecl);
		level--;

		String label;
		if (level == 0) {
			label = "_" + funDecl.name;
			if (topLevelLabels.contains(label)) {
				throw new StackFrameError(funDecl.toString() + "| Duplicate top-level name detected (" + funDecl.name + ").");
			}
			topLevelLabels.add(label);
		} else {
			label = functionNamePrefix + nestedFunctionCounter + "___" + funDecl.name;
			nestedFunctionCounter++;
			Report.warning(funDecl,
						   "Non top-level function declaration found. This function is not visible outside of the local scope (" + funDecl.name + ").");
		}

		long inpCallSize = Math.max(parametersSize, attrs.typAttr.get(funDecl.type).actualTyp().size());

		Frame frame = new Frame(level, label, inpCallSize, 0, 0, 0, 0);
		attrs.frmAttr.set(funDecl, frame);


		//Restore previous values
		parametersSize = parametersSizeBak;

	}


	@Override
	public void visit(VarDecl varDecl) {
		super.visit(varDecl);
		Typ t = attrs.typAttr.get(varDecl).actualTyp();
		localVariablesSize += t.size();
		localVariablesSize += t.padding();


		Access acc;
		if (level == 0) {
			//static variable
			String label = "_" + varDecl.name;
			if (topLevelLabels.contains(label)) {
				throw new StackFrameError(varDecl.toString() + "| Duplicate top-level name detected (" + varDecl.name + ").");
			}
			topLevelLabels.add(label);

			acc = new StaticAccess(label, t.size());
		} else {
			//stack variable
			long offset = -localVariablesSize;
			acc = new OffsetAccess(level, offset, t.size());
		}

		attrs.accAttr.set(varDecl, acc);
	}


	@Override
	public void visit(ParDecl parDecl) {
		super.visit(parDecl);

		Typ t = attrs.typAttr.get(parDecl);
		t = t.actualTyp();

		//For parameters offset goes before increment (since they go the other way)
		Access acc = new OffsetAccess(level, parametersSize, t.size());
		attrs.accAttr.set(parDecl, acc);

		parametersSize += t.size();
		parametersSize += t.padding();

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

		Access a = new OffsetAccess(-1, recordSize, t.size());
		recordSize += t.size();
		recordSize  += t.padding();


		attrs.accAttr.set(compDecl, a);
	}

}
