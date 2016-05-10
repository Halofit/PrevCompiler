package compiler.data.imc;

import java.util.*;

import compiler.common.report.*;
import compiler.common.logger.*;

/**
 * MOVE represents a data move.
 * 
 * @author sliva
 */
public class MOVE extends IMCStmt {

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
		this.dst = dst;
		this.src = src;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "MOVE");
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

}
