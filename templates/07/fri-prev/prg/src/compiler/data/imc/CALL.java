package compiler.data.imc;

import java.util.*;

import compiler.common.logger.*;
import compiler.common.report.*;

/**
 * CALL represents a function call.
 * 
 * @author sliva
 */
public class CALL extends IMCExpr {

	/** The function's label. */
	public final String label;

	/** The function's arguments. */
	private final IMCExpr[] args;
	
	/** The width of the arguments (in bytes). */
	private final long[] widths;

	/**
	 * Constructs a new function call.
	 * 
	 * @param label
	 *            The function's label.
	 * @param args
	 *            Arguments (including the static link).
	 * @param widths
	 *            The width of the arguments (in bytes).
	 */
	public CALL(String label, Vector<IMCExpr> args, Vector<Long> widths) {
		this.label = label;
		if (args.size() != widths.size())
			throw new InternalCompilerError ();
		this.args = new IMCExpr[args.size()];
		for (int arg = 0; arg < args.size(); arg++)
			this.args[arg] = args.get(arg);
		this.widths = new long[widths.size()];
		for (int width = 0; width < widths.size(); width++)
			this.widths[width] = widths.get(width);
	}

	/**
	 * Returns the number of arguments.
	 * 
	 * @return The number of arguments.
	 */
	public int numArgs() {
		return args.length;
	}
	
	/**
	 * Returns the argument with the given index.
	 * 
	 * @param arg
	 *            The index of an argument.
	 * @return The requested argument.
	 */
	public IMCExpr args(int arg) {
		return args[arg];
	}

	public long widths(int arg) {
		return widths[arg];
	}
	
	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		StringBuffer ws = new StringBuffer();
		for (int arg = 0; arg < args.length; arg++)
			ws.append((arg > 0 ? "," : "") + widths[arg]);
		logger.addAttribute("kind", "CALL " + label + " (" + ws + ")");
		for (int arg = 0; arg < args.length; arg++)
			this.args[arg].toXML(logger);
		logger.endElement();
	}
	
	@Override
	public SEXPR linCode() {
		int result = TEMP.newTempName();
		
		Vector<IMCStmt> lc = new Vector<IMCStmt>();
		SEXPR[] argsLC = new SEXPR[args.length];
		Vector<IMCExpr> newargs = new Vector<IMCExpr>();
		Vector<Long> newwidths = new Vector<Long>();
		for (int arg = 0; arg < args.length; arg++) {
			argsLC[arg] = args[arg].linCode();
			lc.addAll(((STMTS)(argsLC[arg].stmt)).stmts());
			newargs.add(argsLC[arg].expr);
			newwidths.add(widths[arg]);
		}
		lc.add(new MOVE(new TEMP(result), new CALL(label, newargs, newwidths)));
		return new SEXPR(new STMTS(lc), new TEMP(result));
	}

}
