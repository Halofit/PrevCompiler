package compiler.phase.frames;

import compiler.common.logger.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.phase.seman.*;

/**
 * A visitor for printing out the XML description of the abstract syntax tree
 * including the information computed during semantic analysis and frame
 * construction.
 * 
 * <p>
 * Traverses the abstract syntax tree in a depth-first manner and produces the
 * XML description of the abstract syntax tree.
 * </p>
 * 
 * @author sliva
 */
public class FramesToXML extends SemAnToXML {

	/** Whether begin and end elements are produced or not. */
	private final boolean boxed;

	private final Attributes attrs;

	/**
	 * Constructs a new visitor for printing out the XML description of the
	 * abstract syntax tree including the information computed during semantic
	 * analysis.
	 * 
	 * @param logger
	 *            The logger used to produce the XML description of the abstract
	 *            syntax tree to (must not be <code>null</code>).
	 * @param boxed
	 *            Whether begin and end elements are produced or not.
	 * @param attrs
	 *            Semantic attributes associated with AST nodes.
	 */
	public FramesToXML(Logger logger, boolean boxed, Attributes attrs) {
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
		endElement();
	}

	@Override
	public void visit(CastExpr castExpr) {
		begElement();
		super.visit(castExpr);
		endElement();
	}

	@Override
	public void visit(CompDecl compDecl) {
		begElement();
		super.visit(compDecl);
		if (attrs.accAttr.get(compDecl) != null)
			attrs.accAttr.get(compDecl).log(logger);
		endElement();
	}

	@Override
	public void visit(CompName compName) {
		begElement();
		super.visit(compName);
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
		endElement();
	}

	@Override
	public void visit(FunCall funCall) {
		begElement();
		super.visit(funCall);
		endElement();
	}

	@Override
	public void visit(FunDecl funDecl) {
		begElement();
		super.visit(funDecl);
		if (attrs.frmAttr.get(funDecl) != null)
			attrs.frmAttr.get(funDecl).log(logger);
		endElement();
	}

	@Override
	public void visit(FunDef funDef) {
		begElement();
		super.visit(funDef);
		if (attrs.frmAttr.get(funDef) != null)
			attrs.frmAttr.get(funDef).log(logger);
		endElement();
	}

	@Override
	public void visit(IfExpr ifExpr) {
		begElement();
		super.visit(ifExpr);
		endElement();
	}

	@Override
	public void visit(ParDecl parDecl) {
		begElement();
		super.visit(parDecl);
		if (attrs.accAttr.get(parDecl) != null)
			attrs.accAttr.get(parDecl).log(logger);
		endElement();
	}

	@Override
	public void visit(Program program) {
		begElement();
		super.visit(program);
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
		endElement();
	}

	@Override
	public void visit(VarDecl varDecl) {
		begElement();
		super.visit(varDecl);
		if (attrs.accAttr.get(varDecl) != null)
			attrs.accAttr.get(varDecl).log(logger);
		endElement();
	}

	@Override
	public void visit(VarName varName) {
		begElement();
		super.visit(varName);
		endElement();
	}

	@Override
	public void visit(WhereExpr whereExpr) {
		begElement();
		super.visit(whereExpr);
		endElement();
	}

	@Override
	public void visit(WhileExpr whileExpr) {
		begElement();
		super.visit(whileExpr);
		endElement();
	}

}
