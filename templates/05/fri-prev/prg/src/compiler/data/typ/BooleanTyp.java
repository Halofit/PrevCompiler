package compiler.data.typ;

import compiler.common.logger.Logger;

/**
 * @author sliva
 */
public class BooleanTyp extends AtomTyp implements AssignableTyp, ComparableTyp, PassableTyp, ReturnableTyp {

	@Override
	public boolean isStructEquivTo(Typ typ) {
		if (typ == null)
			return false;
		return (typ.actualTyp() instanceof BooleanTyp);
	}

	@Override
	public long size() {
		return 1;
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("typ");
		logger.addAttribute("kind", "BOOLEAN");
		logger.endElement();
	}
	
}
