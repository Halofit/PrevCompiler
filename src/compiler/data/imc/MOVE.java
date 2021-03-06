package compiler.data.imc;

import compiler.common.logger.Logger;
import compiler.common.report.InternalCompilerError;
import compiler.phase.codegen.CodeGen;

import java.util.Vector;

/**
 * MOVE represents a data move.
 * 
 * @author sliva
 */
public class MOVE extends IMCStmt {

	public static long idGen = 0;

	public final long id;

	/** The destination. */
	public final IMCExpr dst;

	/** The source. */
	public final IMCExpr src;

	/**
	 * Constructs a new data move.
	 * 
	 * @param dst
	 *            The destination.
	 * @param src
	 *            The source.
	 */
	public MOVE(IMCExpr dst, IMCExpr src) {
		this.id = idGen++;
		this.dst = dst;
		this.src = src;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "MOVE(" + id+")");
		if (dst != null) dst.toXML(logger);
		if (src != null) src.toXML(logger);
		logger.endElement();
	}
		
	@Override
	public STMTS linCode() {
		SEXPR dstLC;
		SEXPR srcLC;
		
		if (dst instanceof MEM) {
			dstLC = ((MEM)dst).addr.linCode();
			srcLC = src.linCode();
			Vector<IMCStmt> lc = new Vector<IMCStmt>();
			lc.addAll(((STMTS)(dstLC.stmt)).stmts());
			lc.addAll(((STMTS)(srcLC.stmt)).stmts());
			lc.add(new MOVE(new MEM(dstLC.expr, ((MEM)dst).width), srcLC.expr));
			return new STMTS(lc);
		}
		if (dst instanceof TEMP) {
			dstLC = dst.linCode();
			srcLC = src.linCode();
			Vector<IMCStmt> lc = new Vector<IMCStmt>();
			lc.addAll(((STMTS)(dstLC.stmt)).stmts());
			lc.addAll(((STMTS)(srcLC.stmt)).stmts());
			lc.add(new MOVE(dstLC.expr, srcLC.expr));
			return new STMTS(lc);
		}
		throw new InternalCompilerError();
	}


	@Override
	public void visit(CodeGen phase) {
		phase.tile(this);
	}
}
