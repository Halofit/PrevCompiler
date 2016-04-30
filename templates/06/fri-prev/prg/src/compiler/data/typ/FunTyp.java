package compiler.data.typ;

import java.util.*;

import compiler.common.logger.*;
import compiler.common.report.*;

/**
 * @author sliva
 */
public class FunTyp extends Typ {

	public final Typ resultTyp;

	private final Typ[] parTyps;

	public FunTyp(LinkedList<Typ> parTyps, Typ resultTyp) {
		super();
		this.parTyps = new Typ[parTyps.size()];
		for (int p = 0; p < parTyps.size(); p++)
			this.parTyps[p] = parTyps.get(p);
		this.resultTyp = resultTyp;
	}

	public int numPars() {
		return parTyps.length;
	}

	public Typ parTyp(int p) {
		return parTyps[p];
	}

	@Override
	public boolean isStructEquivTo(Typ typ) {
		if (typ == null)
			return false;
		if (typ.actualTyp() instanceof FunTyp) {
			FunTyp funType = (FunTyp) (typ.actualTyp());
			boolean isEquiv = true;
			isEquiv = isEquiv && (this.resultTyp.isStructEquivTo(funType.resultTyp));
			isEquiv = isEquiv && (this.parTyps.length == funType.parTyps.length);
			for (int p = 0; p < this.parTyps.length; p++)
				isEquiv = isEquiv && (this.parTyps[p].isStructEquivTo(funType.parTyps[p]));
			return isEquiv;
		} else
			return false;
	}
	
	@Override
	public long size() {
		throw new InternalCompilerError();
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("typ");
		logger.addAttribute("kind", "FUN");
		for (int p = 0; p < parTyps.length; p++) {
			if (parTyps[p] != null)
				parTyps[p].log(logger);
		}
		resultTyp.log(logger);
		logger.endElement();
	}

}
