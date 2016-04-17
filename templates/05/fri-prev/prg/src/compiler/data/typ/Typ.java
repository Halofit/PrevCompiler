package compiler.data.typ;

import compiler.common.logger.*;

/**
 * @author sliva
 */
public abstract class Typ implements Loggable {

	/**
	 * Returns the actual type represented by this type.
	 */
	public Typ actualTyp() {
		return this;
	}

	/**
	 * Checks whether two types are structurally equivalent.
	 * 
	 * @param fstTyp
	 *            The first type.
	 * @param sndTyp
	 *            The second type.
	 * @return <code>true</code> if the specified types are structurally
	 *         equivalent or <code>false</code> otherwise.
	 */
	public static boolean equiv(Typ fstTyp, Typ sndTyp) {
		if ((fstTyp == null) || (sndTyp == null))
			return false;
		return fstTyp.isStructEquivTo(sndTyp);
	}

	/**
	 * Checks whether this type is structurally equivalent to another type.
	 * 
	 * @param typ
	 *            Another type.
	 * @return <code>true</code> if this type is structurally equivalent to the
	 *         specified type or <code>false</code> otherwise.
	 */
	public abstract boolean isStructEquivTo(Typ typ);

	/**
	 * Returns the size of this type in bytes.
	 * 
	 * @return The size of this type in bytes.
	 */
	public abstract long size();

}
