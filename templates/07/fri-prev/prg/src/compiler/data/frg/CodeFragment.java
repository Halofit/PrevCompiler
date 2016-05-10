package compiler.data.frg;

import compiler.common.logger.Logger;
import compiler.data.frm.*;
import compiler.data.imc.*;

/**
 * A code fragment.
 * 
 * @author sliva
 */
public class CodeFragment extends Fragment {

	/** The function's stack frame. */
	public final Frame frame;

	/** A temporary register used as the frame pointer. */
	public final int FP;

	/** A temporary register used for returning the function's result. */
	public final int RV;

	/** The intermediate code. */
	public final IMCStmt stmt;
	
	/** The linearized intermediate code. */
	public final STMTS linCode;

	/**
	 * Constucts a new code fragment.
	 * 
	 * @param frame
	 *            The function's stack frame.
	 * @param FP
	 *            A temporary register used as the frame pointer.
	 * @param RV
	 *            A temporary register used for returning the function's result.
	 * @param stmt
	 *            The intermediate code.
	 */
	public CodeFragment(Frame frame, int FP, int RV, IMCStmt stmt) {
		super(frame.label);
		this.frame = frame;
		this.FP = FP;
		this.RV = RV;
		this.stmt = stmt;
		this.linCode = (this.stmt == null) ? null : this.stmt.linCode();
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("frg");
		logger.addAttribute("kind", "CODE FRAGMENT " + "(" + label + "," + "FP=" + FP + "," + "RV=" + RV  + ")");
		if (linCode != null) linCode.toXML(logger);
		logger.endElement();
	}

}
