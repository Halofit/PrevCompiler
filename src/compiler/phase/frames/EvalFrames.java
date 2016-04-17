package compiler.phase.frames;

import java.util.*;

import compiler.data.acc.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;
import compiler.data.frm.*;
import compiler.data.typ.*;

/**
 * Frame and access evaluator.
 * 
 * @author sliva
 */
public class EvalFrames extends FullVisitor {

	private final Attributes attrs;

	public EvalFrames(Attributes attrs) {
		this.attrs = attrs;
	}

	// TODO

}
