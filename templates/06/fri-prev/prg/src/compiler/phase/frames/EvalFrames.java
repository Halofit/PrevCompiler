package compiler.phase.frames;

import java.util.*;

import compiler.common.report.*;
import compiler.data.acc.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;
import compiler.data.frm.*;
import compiler.data.typ.*;

/**
 * Evaluates stack frames and variable accesses.
 * 
 * @author sliva
 */
public class EvalFrames extends FullVisitor {

	private final Attributes attrs;

	public EvalFrames(Attributes attrs) {
		this.attrs = attrs;
	}

	/** The static stack of frames. */
	private Stack<Frame> frames = new Stack<Frame>();

	/**
	 * The set of labels generated for globally visible functions and variables.
	 */
	private HashSet<String> globalLabels = new HashSet<String>();

	/** The counter of locally visible functions. */
	private int localLabels = 0;

	@Override
	public void visit(FunDef funDef) {
		long inpCallSize = 0;
		FunTyp funTyp = (FunTyp) attrs.typAttr.get(funDef);
		if (funTyp == null)
			return;
		inpCallSize += 8;
		for (int p = 0; p < funTyp.numPars(); p++) {
			long size = funTyp.parTyp(p).size();
			OffsetAccess access = new OffsetAccess(frames.empty() ? 0 : frames.peek().level + 1, inpCallSize, size);
			attrs.accAttr.set(funDef.par(p), access);
			inpCallSize += size;
		}
		inpCallSize = Long.max(inpCallSize, funTyp.resultTyp.size());
		Frame frame;
		if (frames.empty()) {
			String label = "_" + funDef.name;
			if (globalLabels.contains(label)) {
				Report.warning(funDef, "Duplicated global name '" + funDef.name + "'");
				return;
			}
			globalLabels.add(label);
			frame = new Frame(0, label, inpCallSize, 0, 0, 0, 0);
		}
		else {
			frame = new Frame(frames.peek().level + 1, "F" + localLabels + "_" + funDef.name, inpCallSize, 0, 0, 0, 0);
			localLabels++;
		}
		frames.push(frame);
		for (int p = 0; p < funDef.numPars(); p++)
			funDef.par(p).accept(this);
		funDef.type.accept(this);
		funDef.body.accept(this);
		frame = frames.pop();
		attrs.frmAttr.set(funDef, frame);
	}

	@Override
	public void visit(RecType recType) {
		RecTyp recTyp = (RecTyp) attrs.typAttr.get(recType);
		if (recTyp == null)
			return;
		long offset = 0;
		for (int c = 0; c < recType.numComps(); c++) {
			recType.comp(c).accept(this);
			long size = recTyp.compTyp(c).size();
			OffsetAccess access = new OffsetAccess(-1, offset, size);
			attrs.accAttr.set(recType.comp(c), access);
			offset += size;
		}
	}

	@Override
	public void visit(VarDecl varDecl) {
		varDecl.type.accept(this);
		Typ varTyp = attrs.typAttr.get(varDecl);
		if (varTyp == null)
			return;
		long size = varTyp.size();
		if (frames.isEmpty()) {
			String label = "_" + varDecl.name;
			if (globalLabels.contains(label)) {
				Report.warning(varDecl, "Duplicated global name '" + varDecl.name + "'");
				return;
			}
			globalLabels.add(label);
			StaticAccess access = new StaticAccess(label, size);
			attrs.accAttr.set(varDecl, access);
		} else {
			Frame frame = frames.pop();
			OffsetAccess access = new OffsetAccess(frame.level, -(frame.locVarsSize + size), size);
			attrs.accAttr.set(varDecl, access);
			frames.push(new Frame(frame.level, frame.label, frame.inpCallSize, frame.locVarsSize + size,
					frame.tmpVarsSize, frame.hidRegsSize, frame.outCallSize));
		}
	}

}
