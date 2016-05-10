package compiler.data.typ;

import compiler.common.logger.Logger;

/**
 * @author sliva
 */
public class VoidTyp extends AtomTyp implements ReturnableTyp {

	@Override
	public boolean isStructEquivTo(Typ typ) {
		if (typ == null)
			return false;
		return (typ.actualTyp() instanceof VoidTyp);
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("typ");
		logger.addAttribute("kind", "VOID");
		logger.endElement();
	}

	@Override
	public long size() {
		return 0;
	}

}
