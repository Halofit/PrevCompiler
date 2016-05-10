package compiler.data.typ;

import compiler.common.logger.Logger;

/**
 * @author sliva
 */
public class IntegerTyp extends AtomTyp implements AssignableTyp, ComparableTyp, PassableTyp, ReturnableTyp {

	@Override
	public boolean isStructEquivTo(Typ typ) {
		if (typ == null)
			return false;
		return (typ.actualTyp() instanceof IntegerTyp);
	}

	@Override
	public long size() {
		return 8;
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("typ");
		logger.addAttribute("kind", "INTEGER");
		logger.endElement();
	}

}
