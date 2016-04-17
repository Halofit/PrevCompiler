package compiler.data.typ;

import compiler.common.logger.Logger;

/**
 * @author sliva
 */
public class StringTyp extends AtomTyp implements AssignableTyp, PassableTyp, ReturnableTyp {

	@Override
	public boolean isStructEquivTo(Typ typ) {
		if (typ == null)
			return false;
		return (typ.actualTyp() instanceof StringTyp);
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("typ");
		logger.addAttribute("kind", "STRING");
		logger.endElement();
	}

	@Override
	public long size() {
		return 8;
	}

}
