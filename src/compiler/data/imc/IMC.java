package compiler.data.imc;

import compiler.common.logger.Logger;
import compiler.phase.codegen.CodeGen;

/**
 * An instruction of an intermediate code.
 * 
 * @author sliva
 */
public abstract class IMC {
	
	public abstract void toXML(Logger logger);
	public abstract void visit(CodeGen phase);
}
