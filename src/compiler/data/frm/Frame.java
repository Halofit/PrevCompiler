package compiler.data.frm;

import compiler.common.logger.Loggable;
import compiler.common.logger.Logger;

/**
 * A stack frame.
 *
 * @author sliva
 */
public class Frame implements Loggable {

	/**
	 * The static level.
	 */
	public final int level;

	/**
	 * The entry label.
	 */
	public final String label;

	/**
	 * The size of the stack frame.
	 */
	public long size;

	/**
	* The size of block containing input arguments (and a static link) when
	* function is called and the result when function returns.
	*/
	public final long inpCallSize;

	/**
	 * The size of block containing local variables.
	 */
	public final long locVarsSize;

	/**
	 * The size of block containing temporary variables.
	 */
	public long tmpVarsSize;

	/**
	 * The size of block containing hidden registers.
	 */
	public final long hidRegsSize;

	/**
	 * The size of block containing output arguments (and a static link) when
	 * function calls another function and the result when the called function
	 * returns.
	 */
	public final long outCallSize;

	public long numTemps;

	/**
	 * Constructs a new empty stack frame.
	 *
	 * @param level The static level.
	 * @param label The entry label.
	 */
	public Frame(int level, String label, long inpCallSize, long locVarsSize, long tmpVarsSize, long hidRegsSize,
				 long outCallSize) {
		this.level = level;
		this.label = label;

		this.inpCallSize = inpCallSize;
		this.locVarsSize = locVarsSize;
		this.tmpVarsSize = tmpVarsSize;
		this.hidRegsSize = hidRegsSize;
		this.outCallSize = outCallSize;

		numTemps = (tmpVarsSize / 8);

		setSize();
	}


	public void setSize(){
		this.size = this.locVarsSize + 16 + this.tmpVarsSize + this.hidRegsSize + this.outCallSize;
	}

	public long addTemp(){
		this.tmpVarsSize += 8;
		this.numTemps++;
		setSize();
		return numTemps-1;
	}

	public long getTempsOffset(long idx){
		//local variables + RA + oldFP + 8 for every next temp
		return this.locVarsSize + 16 + (idx+1)*8;
	}

	public long getOldFPOffset(){
		//local variables + RA + oldFP + 8 for every next temp
		return this.locVarsSize + 8;
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("frame");
		logger.addAttribute("level", Integer.toString(level));
		logger.addAttribute("label", label);
		logger.addAttribute("size", Long.toString(size));
		logger.addAttribute("inpCallSize", Long.toString(inpCallSize));
		logger.addAttribute("locVarsSize", Long.toString(locVarsSize));
		logger.addAttribute("tmpVarsSize", Long.toString(tmpVarsSize));
		logger.addAttribute("hidRegsSize", Long.toString(hidRegsSize));
		logger.addAttribute("outCallSize", Long.toString(outCallSize));
		logger.endElement();
	}

}
