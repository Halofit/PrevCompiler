package compiler.data.typ;

import compiler.common.logger.*;

/**
 * @author sliva
 */
public abstract class Typ {

	public Typ() {
	}

	public Typ actualTyp() {
		return this;
	}

	public static boolean equiv(Typ fstTyp, Typ sndTyp) {
		if ((fstTyp == null) || (sndTyp == null))
			return false;
		return fstTyp.isStructEquivTo(sndTyp);
	}

	public abstract boolean isStructEquivTo(Typ typ);

	public abstract void log(Logger logger);

}
