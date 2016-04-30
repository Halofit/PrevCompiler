package compiler.phase.imcode;

import compiler.common.logger.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.imc.*;
import compiler.data.frg.*;
import compiler.phase.frames.*;

public class ImcodeToXML extends FramesToXML {

	/** Whether begin and end elements are produced or not. */
	private final boolean boxed;

	private final Attributes attrs;

	/**
	 * Constructs a new visitor for printing out the XML description of the
	 * abstract syntax tree including the information computed during semantic
	 * analysis, frame and access evaluation, and intermediate code generation.
	 * 
	 * @param logger
	 *            The logger used to produce the XML description of the abstract
	 *            syntax tree to (must not be <code>null</code>).
	 * @param boxed
	 *            Whether begin and end elements are produced or not.
	 * @param attrs
	 *            Semantic attributes associated with AST nodes.
	 */
	public ImcodeToXML(Logger logger, boolean boxed, Attributes attrs) {
		super(logger, false, attrs);
		this.boxed = boxed;
		this.attrs = attrs;
	}

	/**
	 * Prints out the begin element (if requested).
	 */
	private final void begElement() {
		if (boxed)
			logger.begElement("ast");
	}

	/**
	 * Prints out the end element (if requested).
	 */
	private final void endElement() {
		if (boxed)
			logger.endElement();
	}

	// Visitor.

	@Override
	public void visit(ArrType arrType) {
		begElement();
		super.visit(arrType);
		endElement();
	}

	@Override
	public void visit(AtomExpr atomExpr) {
		begElement();
		super.visit(atomExpr);
		Fragment fragment = attrs.frgAttr.get(atomExpr);
		if (fragment != null) {
			logger.begElement("fragment");
			fragment.toXML(logger);
			logger.endElement();
		}
		IMC imc = attrs.imcAttr.get(atomExpr);
		if (imc != null) {
			logger.begElement("imcode");
			imc.toXML(logger);
			logger.endElement();
		}
		endElement();
	}

	@Override
	public void visit(AtomType atomType) {
		begElement();
		super.visit(atomType);
		endElement();
	}

	@Override
	public void visit(BinExpr binExpr) {
		begElement();
		super.visit(binExpr);
		IMC imc = attrs.imcAttr.get(binExpr);
		if (imc != null) {
			logger.begElement("imcode");
			imc.toXML(logger);
			logger.endElement();
		}
		endElement();
	}

	@Override
	public void visit(CastExpr castExpr) {
		begElement();
		super.visit(castExpr);
		IMC imc = attrs.imcAttr.get(castExpr);
		if (imc != null) {
			logger.begElement("imcode");
			imc.toXML(logger);
			logger.endElement();
		}
		endElement();
	}

	@Override
	public void visit(CompDecl compDecl) {
		begElement();
		super.visit(compDecl);
		endElement();
	}

	@Override
	public void visit(CompName compName) {
		begElement();
		super.visit(compName);
		IMC imc = attrs.imcAttr.get(compName);
		if (imc != null) {
			logger.begElement("imcode");
			imc.toXML(logger);
			logger.endElement();
		}
		endElement();
	}

	@Override
	public void visit(DeclError declError) {
		begElement();
		super.visit(declError);
		endElement();
	}

	@Override
	public void visit(Exprs exprs) {
		begElement();
		super.visit(exprs);
		IMC imc = attrs.imcAttr.get(exprs);
		if (imc != null) {
			logger.begElement("imcode");
			imc.toXML(logger);
			logger.endElement();
		}
		endElement();
	}

	@Override
	public void visit(ExprError exprError) {
		begElement();
		super.visit(exprError);
		endElement();
	}

	@Override
	public void visit(ForExpr forExpr) {
		begElement();
		super.visit(forExpr);
		IMC imc = attrs.imcAttr.get(forExpr);
		if (imc != null) {
			logger.begElement("imcode");
			imc.toXML(logger);
			logger.endElement();
		}
		endElement();
	}

	@Override
	public void visit(FunCall funCall) {
		begElement();
		super.visit(funCall);
		IMC imc = attrs.imcAttr.get(funCall);
		if (imc != null) {
			logger.begElement("imcode");
			imc.toXML(logger);
			logger.endElement();
		}
		endElement();
	}

	@Override
	public void visit(FunDecl funDecl) {
		begElement();
		super.visit(funDecl);
		endElement();
	}

	@Override
	public void visit(FunDef funDef) {
		begElement();
		super.visit(funDef);
		Fragment fragment = attrs.frgAttr.get(funDef);
		if (fragment != null) {
			logger.begElement("fragment");
			fragment.toXML(logger);
			logger.endElement();
		}
		IMC imc = attrs.imcAttr.get(funDef);
		if (imc != null) {
			logger.begElement("imcode");
			imc.toXML(logger);
			logger.endElement();
		}
		endElement();
	}

	@Override
	public void visit(IfExpr ifExpr) {
		begElement();
		super.visit(ifExpr);
		IMC imc = attrs.imcAttr.get(ifExpr);
		if (imc != null) {
			logger.begElement("imcode");
			imc.toXML(logger);
			logger.endElement();
		}
		endElement();
	}

	@Override
	public void visit(ParDecl parDecl) {
		begElement();
		super.visit(parDecl);
		endElement();
	}

	@Override
	public void visit(Program program) {
		begElement();
		super.visit(program);
		Fragment fragment = attrs.frgAttr.get(program);
		if (fragment != null) {
			logger.begElement("fragment");
			fragment.toXML(logger);
			logger.endElement();
		}
		IMC imc = attrs.imcAttr.get(program);
		if (imc != null) {
			logger.begElement("imcode");
			imc.toXML(logger);
			logger.endElement();
		}
		endElement();
	}

	@Override
	public void visit(PtrType ptrType) {
		begElement();
		super.visit(ptrType);
		endElement();
	}

	@Override
	public void visit(RecType recType) {
		begElement();
		super.visit(recType);
		endElement();
	}

	@Override
	public void visit(TypeDecl typDecl) {
		begElement();
		super.visit(typDecl);
		endElement();
	}

	@Override
	public void visit(TypeError typeError) {
		begElement();
		super.visit(typeError);
		endElement();
	}

	@Override
	public void visit(TypeName typeName) {
		begElement();
		super.visit(typeName);
		endElement();
	}

	@Override
	public void visit(UnExpr unExpr) {
		begElement();
		super.visit(unExpr);
		IMC imc = attrs.imcAttr.get(unExpr);
		if (imc != null) {
			logger.begElement("imcode");
			imc.toXML(logger);
			logger.endElement();
		}
		endElement();
	}

	@Override
	public void visit(VarDecl varDecl) {
		begElement();
		super.visit(varDecl);
		Fragment fragment = attrs.frgAttr.get(varDecl);
		if (fragment != null) {
			logger.begElement("fragment");
			fragment.toXML(logger);
			logger.endElement();
		}
		endElement();
	}

	@Override
	public void visit(VarName varName) {
		begElement();
		super.visit(varName);
		IMC imc = attrs.imcAttr.get(varName);
		if (imc != null) {
			logger.begElement("imcode");
			imc.toXML(logger);
			logger.endElement();
		}
		endElement();
	}

	@Override
	public void visit(WhereExpr whereExpr) {
		begElement();
		super.visit(whereExpr);
		IMC imc = attrs.imcAttr.get(whereExpr);
		if (imc != null) {
			logger.begElement("imcode");
			imc.toXML(logger);
			logger.endElement();
		}
		endElement();
	}

	@Override
	public void visit(WhileExpr whileExpr) {
		begElement();
		super.visit(whileExpr);
		IMC imc = attrs.imcAttr.get(whileExpr);
		if (imc != null) {
			logger.begElement("imcode");
			imc.toXML(logger);
			logger.endElement();
		}
		endElement();
	}

}
