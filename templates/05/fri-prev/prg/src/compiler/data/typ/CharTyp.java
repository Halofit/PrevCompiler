package compiler.data.typ;

import compiler.common.logger.Logger;

/**
 * @author sliva
 */
public class CharTyp extends AtomTyp implements AssignableTyp, ComparableTyp, PassableTyp, ReturnableTyp {

	@Override
	public boolean isStructEquivTo(Typ typ) {
		if (typ == null)
			return false;
		return (typ.actualTyp() instanceof CharTyp);
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("typ");
		logger.addAttribute("kind", "CHAR");
		logger.endElement();
	}

	@Override
	public long size() {
		return 1;
	}

}
