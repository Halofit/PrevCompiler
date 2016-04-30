package compiler.data.imc;

import java.util.*;

import compiler.common.logger.*;

/**
 * CJUMP represents a conditional jump.
 * 
 * @author sliva
 */
public class CJUMP extends IMCStmt {

	/** The condition. */
	public final IMCExpr cond;

	/** The destination if the condition evaluates to nonzero. */
	public final String posLabel;

	/** The destination if the condition evaluates to zero. */
	public final String negLabel;

	/**
	 * Constructs a new conditional jump.
	 * 
	 * @param cond
	 *            The condition.
	 * @param posLabel
	 *            The destination if the condition evaluates to nonzero.
	 * @param negLabel
	 *            The destination if the condition evaluates to zero.
	 */
	public CJUMP(IMCExpr cond, String posLabel, String negLabel) {
		this.cond = cond;
		this.posLabel = posLabel;
		this.negLabel = negLabel;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "CJUMP:" + posLabel + ":" + negLabel);
		if (cond != null) cond.toXML(logger);
		logger.endElement();
	}

	@Override
	public STMTS linCode() {
		SEXPR condLC = cond.linCode();
		Vector<IMCStmt> lc = new Vector<IMCStmt>();
		lc.addAll(((STMTS)(condLC.stmt)).stmts());
		lc.add(new CJUMP(condLC.expr, posLabel, negLabel));
		return new STMTS(lc);
	}

}
