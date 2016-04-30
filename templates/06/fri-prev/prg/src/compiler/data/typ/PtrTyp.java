package compiler.data.typ;

import compiler.common.logger.*;

/**
 * @author sliva
 */
public class PtrTyp extends Typ implements AssignableTyp, CastableTyp, ComparableTyp, PassableTyp, ReturnableTyp {

	public final Typ baseTyp;

	public PtrTyp(Typ type) {
		this.baseTyp = type;
	}

	@Override
	public boolean isStructEquivTo(Typ typ) {
		if (typ == null)
			return false;
		return (typ.actualTyp() instanceof PtrTyp)
				&& ((this.baseTyp.isStructEquivTo(((PtrTyp) (typ.actualTyp())).baseTyp))
						|| (this.baseTyp.actualTyp() instanceof VoidTyp)
						|| (((PtrTyp) (typ.actualTyp())).baseTyp.actualTyp() instanceof VoidTyp));
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("typ");
		logger.addAttribute("kind", "PTR");
		if (baseTyp != null)
			baseTyp.log(logger);
		logger.endElement();
	}

	@Override
	public long size() {
		return 8;
	}

}
