package compiler.data.typ;

import java.util.*;

import compiler.common.logger.*;
import compiler.common.report.*;

/**
 * @author sliva
 */
public class TypName extends Typ {

	public final String name;

	private Typ actualTyp;

	public TypName(String name) {
		super();
		this.name = name;
		this.actualTyp = null;
	}

	public void setType(Typ type) {
		if ((this.actualTyp != null) || (type == null))
			throw new InternalCompilerError();
		this.actualTyp = type;
	}

	public Typ getType() {
		return actualTyp;
	}

	@Override
	public Typ actualTyp() {
		return actualTyp.actualTyp();
	}

	public boolean isCircular() {
		return isCircular(this, new HashSet<TypName>());
	}

	private boolean isCircular(Typ typ, HashSet<TypName> typNames) {
		if (typ instanceof ArrTyp) {
			return isCircular(((ArrTyp) typ).elemTyp, typNames);
		}
		if (typ instanceof AtomTyp) {
			return false;
		}
		if (typ instanceof FunTyp) {
			boolean isCircular = isCircular(((FunTyp) typ).resultTyp, typNames);
			for (int p = 0; p < ((FunTyp) typ).numPars(); p++)
				isCircular = isCircular || isCircular(((FunTyp) typ).parTyp(p), typNames);
			return isCircular;
		}
		if (typ instanceof PtrTyp) {
			return false;
		}
		if (typ instanceof RecTyp) {
			boolean isCircular = false;
			for (int c = 0; c < ((RecTyp) typ).numComps(); c++)
				isCircular = isCircular || isCircular(((RecTyp) typ).compTyp(c), typNames);
			return isCircular;
		}
		if (typ instanceof TypName) {
			TypName typName = (TypName) typ;
			if (typNames.contains(typName))
				return true;
			typNames.add(typName);
			boolean isCircular = isCircular(typName.actualTyp, typNames);
			typNames.remove(typName);
			return isCircular;
		}
		throw new InternalCompilerError();
	}

	@Override
	public boolean isStructEquivTo(Typ typ) {
		if (typ == null)
			return false;
		return this.actualTyp().isStructEquivTo(typ);
	}

	@Override
	public long size() {
		return this.actualTyp.size();
	}
	
	@Override
	public void log(Logger logger) {
		logger.begElement("typ");
		logger.addAttribute("kind", "NAME(" + name + ")");
		logger.endElement();
	}

}
