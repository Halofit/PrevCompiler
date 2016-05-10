package compiler.data.imc;

import java.util.*;

import compiler.common.logger.*;

/**
 * MEM represents a memory access.
 * 
 * @author sliva
 */
public class MEM extends IMCExpr {

	/** The address. */
	public final IMCExpr addr;

	/** The width (in bytes) of the memory access. */
	public final long width;
	
	/**
	 * Constructs a new memory access.
	 * 
	 * @param addr
	 *            The address in the memory.
	 * @param width
	 *            The width (in bytes) of the memory access.
	 */
	public MEM(IMCExpr addr, long width) {
		this.addr = addr;
		this.width = width;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "MEM " + "(" + width + ")");
		if (addr != null) addr.toXML(logger);
		logger.endElement();
	}
	
	@Override
	public SEXPR linCode() {
		int result = TEMP.newTempName();
		SEXPR addrLC = addr.linCode();
		Vector<IMCStmt> lc = new Vector<IMCStmt>();
		lc.addAll(((STMTS)(addrLC.stmt)).stmts());
		lc.add(new MOVE(new TEMP(result), new MEM(addrLC.expr, width)));
		return new SEXPR(new STMTS(lc), new TEMP(result));
	}

}
