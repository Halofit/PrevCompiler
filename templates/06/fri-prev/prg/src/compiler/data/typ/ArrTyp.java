package compiler.data.typ;

import compiler.common.logger.*;

/**
 * @author sliva
 */
public class ArrTyp extends Typ {

	public final long size;

	public final Typ elemTyp;

	public ArrTyp(long size, Typ type) {
		super();
		this.size = size;
		this.elemTyp = type;
	}

	@Override
	public boolean isStructEquivTo(Typ typ) {
		if (typ == null)
			return false;
		return (typ.actualTyp() instanceof ArrTyp) && (this.size == ((ArrTyp) (typ.actualTyp())).size)
				&& (this.elemTyp.isStructEquivTo(((ArrTyp) (typ.actualTyp())).elemTyp));
	}
	
	@Override
	public long size() {
		return size * elemTyp.size();
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("typ");
		logger.addAttribute("kind", "ARR(" + size + ")");
		if (elemTyp != null)
			elemTyp.log(logger);
		logger.endElement();
	}

}
