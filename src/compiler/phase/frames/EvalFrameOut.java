package compiler.phase.frames;

import compiler.data.ast.FunCall;
import compiler.data.ast.FunDecl;
import compiler.data.ast.FunDef;
import compiler.data.ast.Program;
import compiler.data.ast.attr.Attributes;
import compiler.data.ast.code.FullVisitor;
import compiler.data.frm.Frame;

/**
 * Frame and access evaluator.
 *
 * @author sliva
 */
public class EvalFrameOut extends FullVisitor {

	private final Attributes attrs;
	public static long globalProgramOutSize;

	//NOTE: static link is already included in the inpCallSize of the Frames, so no need to recompute (add 8 bytes)
	private long outSize; //for calculating the size of the output part of the stack frame (SL + output parameters)


	public EvalFrameOut(Attributes attrs) {
		this.attrs = attrs;
		outSize = 0;
	}

	@Override
	public void visit(FunCall funCall) {
		super.visit(funCall);

		FunDecl decl = (FunDecl) attrs.declAttr.get(funCall);
		Frame f = attrs.frmAttr.get(decl);

		outSize = Math.max(f.inpCallSize, outSize);
	}

	@Override
	public void visit(FunDef funDef) {
		//Backup old value
		long outSizeBak = outSize;
		outSize = 0;
		super.visit(funDef);


		Frame f = attrs.frmAttr.get(funDef);
		Frame newFrame = new Frame(f.level, f.label,  f.inpCallSize, f.locVarsSize, f.tmpVarsSize, f.hidRegsSize, outSize);
		attrs.frmAttr.set(funDef, newFrame);

		//Restore
		outSize = outSizeBak;
	}

	@Override
	public void visit(Program program) {
		super.visit(program);
		globalProgramOutSize = outSize;
	}
}
