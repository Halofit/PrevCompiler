package compiler.data.imc;

import java.util.*;

import compiler.common.logger.*;

/**
 * STSMS represents a sequence of statements.
 * 
 * @author sliva
 */
public class STMTS extends IMCStmt {

	/** A sequence of statements. */
	private final Vector<IMCStmt> stmts;
	
	/**
	 * Constructs a new sequence of statements.
	 * 
	 * @param stmts Statements.
	 */
	public STMTS(Vector<IMCStmt> stmts) {
		this.stmts = new Vector<IMCStmt>();
		this.stmts.addAll(stmts);
	}
	
	public int numStmts() {
		return stmts.size();
	}
	
	public IMCStmt stmts(int stmt) {
		return stmts.get(stmt);
	}
	
	public Vector<IMCStmt> stmts() {
		Vector<IMCStmt> _stmts = new Vector<IMCStmt>();
		_stmts.addAll(stmts);
		return _stmts;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "STMTS");
		for (int stmt = 0; stmt < stmts.size(); stmt++)
			this.stmts.get(stmt).toXML(logger);
		logger.endElement();
	}
	
	public STMTS linCode() {
		Vector<IMCStmt> lc = new Vector<IMCStmt>();
		for (int stmt = 0; stmt < stmts.size(); stmt++) {
			STMTS lcStmt = stmts.get(stmt).linCode();
			lc.addAll(lcStmt.stmts());
		}
		return new STMTS(lc);
	}

}
