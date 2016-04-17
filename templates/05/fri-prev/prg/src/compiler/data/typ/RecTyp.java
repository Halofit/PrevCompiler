package compiler.data.typ;

import java.util.*;

import compiler.common.logger.*;

/**
 * @author sliva
 */
public class RecTyp extends Typ {

	public final String nameSpace;

	private final Typ[] compTyps;

	public RecTyp(String nameSpace, LinkedList<Typ> compTyps) {
		super();
		this.nameSpace = nameSpace;
		this.compTyps = new Typ[compTyps.size()];
		for (int c = 0; c < compTyps.size(); c++)
			this.compTyps[c] = compTyps.get(c);
	}

	public int numComps() {
		return compTyps.length;
	}

	public Typ compTyp(int c) {
		return compTyps[c];
	}

	@Override
	public boolean isStructEquivTo(Typ typ) {
		if (typ == null)
			return false;
		if (typ.actualTyp() instanceof RecTyp) {
			RecTyp funType = (RecTyp) (typ.actualTyp());
			boolean isEquiv = true;
			isEquiv = isEquiv && (this.compTyps.length == funType.compTyps.length);
			for (int c = 0; c < this.compTyps.length; c++)
				isEquiv = isEquiv && (this.compTyps[c].isStructEquivTo(funType.compTyps[c]));
			return isEquiv;
		} else
			return false;
	}
	
	@Override
	public long size() {
		long size = 0;
		for (int c = 0; c < this.compTyps.length; c++)
			size = size + this.compTyps[c].size();
		return size;
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("typ");
		logger.addAttribute("kind", "REC");
		for (int c = 0; c < compTyps.length; c++) {
			if (compTyps[c] != null)
				compTyps[c].log(logger);
		}
		logger.endElement();
	}

}
