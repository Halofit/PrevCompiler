package compiler.phase.abstr;

import compiler.common.logger.*;
import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.code.*;

/**
 * A visitor for printing out the XML description of the abstract syntax tree.
 * 
 * <p>
 * Traverses the abstract syntax tree in a depth-first manner and produces the
 * XML description of the abstract syntax tree.
 * </p>
 * 
 * @author sliva
 */
public class AbstrToXML implements Visitor {

	/**
	 * The logger used to produce the XML description of the abstract syntax
	 * tree to (must not be <code>null</code>).
	 */
	public final Logger logger;

	/** Whether begin and end elements are produced or not. */
	private boolean boxed;

	/**
	 * Constructs a new visitor for printing out the XML description of the
	 * abstract syntax tree.
	 * 
	 * @param logger
	 *            The logger used to produce the XML description of the abstract
	 *            syntax tree to (must not be <code>null</code>).
	 * @param boxed
	 *            Whether begin and end elements are produced or not.
	 */
	public AbstrToXML(Logger logger, boolean boxed) {
		this.logger = logger;
		this.boxed = boxed;
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

	// Visitor methods.

	public void visit(ArrType arrType) {
		begElement();
		((Position) arrType).log(logger);
		logger.addAttribute("kind", "ArrType");
		arrType.size.accept(this);
		arrType.elemType.accept(this);
		endElement();
	}

	public void visit(AtomExpr atomExpr) {
		begElement();
		((Position) atomExpr).log(logger);
		logger.addAttribute("kind", "AtomExpr");
		logger.addAttribute("name", atomExpr.type.toString() + "(" + atomExpr.value + ")");
		endElement();
	}

	public void visit(AtomType atomType) {
		begElement();
		((Position) atomType).log(logger);
		logger.addAttribute("kind", "AtomType");
		logger.addAttribute("name", atomType.type.toString());
		endElement();
	}

	public void visit(BinExpr binExpr) {
		begElement();
		((Position) binExpr).log(logger);
		logger.addAttribute("kind", "BinExpr:" + binExpr.oper.toString());
		binExpr.fstExpr.accept(this);
		binExpr.sndExpr.accept(this);
		endElement();
	}

	public void visit(CastExpr castExpr) {
		begElement();
		((Position) castExpr).log(logger);
		logger.addAttribute("kind", "CastExpr");
		castExpr.type.accept(this);
		castExpr.expr.accept(this);
		endElement();
	}

	public void visit(CompDecl compDecl) {
		begElement();
		((Position) compDecl).log(logger);
		logger.addAttribute("kind", "CompDecl");
		logger.addAttribute("name", compDecl.name);
		compDecl.type.accept(this);
		endElement();
	}

	public void visit(CompName compName) {
		begElement();
		((Position) compName).log(logger);
		logger.addAttribute("kind", "CompName");
		logger.addAttribute("name", compName.name());
		endElement();
	}

	public void visit(DeclError declError) {
		begElement();
		logger.addAttribute("kind", "DeclError");
		endElement();
	}

	public void visit(Exprs exprs) {
		begElement();
		((Position) exprs).log(logger);
		logger.addAttribute("kind", "Exprs");
		for (int e = 0; e < exprs.numExprs(); e++)
			exprs.expr(e).accept(this);
		endElement();
	}

	public void visit(ExprError exprError) {
		begElement();
		logger.addAttribute("kind", "ExprError");
		endElement();
	}

	public void visit(ForExpr forExpr) {
		begElement();
		((Position) forExpr).log(logger);
		logger.addAttribute("kind", "ForExpr");
		forExpr.var.accept(this);
		forExpr.loBound.accept(this);
		forExpr.hiBound.accept(this);
		forExpr.body.accept(this);
		endElement();
	}

	public void visit(FunCall funCall) {
		begElement();
		((Position) funCall).log(logger);
		logger.addAttribute("kind", "FunCall");
		logger.addAttribute("name", funCall.name());
		for (int a = 0; a < funCall.numArgs(); a++)
			funCall.arg(a).accept(this);
		endElement();
	}

	public void visit(FunDecl funDecl) {
		begElement();
		((Position) funDecl).log(logger);
		logger.addAttribute("kind", "FunDecl");
		logger.addAttribute("name", funDecl.name);
		for (int p = 0; p < funDecl.numPars(); p++)
			funDecl.par(p).accept(this);
		funDecl.type.accept(this);
		endElement();
	}

	public void visit(FunDef funDef) {
		begElement();
		((Position) funDef).log(logger);
		logger.addAttribute("kind", "FunDef");
		logger.addAttribute("name", funDef.name);
		for (int p = 0; p < funDef.numPars(); p++)
			funDef.par(p).accept(this);
		funDef.type.accept(this);
		funDef.body.accept(this);
		endElement();
	}

	public void visit(IfExpr ifExpr) {
		begElement();
		((Position) ifExpr).log(logger);
		logger.addAttribute("kind", "IfExpr");
		ifExpr.cond.accept(this);
		ifExpr.thenExpr.accept(this);
		ifExpr.elseExpr.accept(this);
		endElement();
	}

	public void visit(ParDecl parDecl) {
		begElement();
		((Position) parDecl).log(logger);
		logger.addAttribute("kind", "ParDecl");
		logger.addAttribute("name", parDecl.name);
		parDecl.type.accept(this);
		endElement();
	}

	public void visit(Program program) {
		begElement();
		((Position) program).log(logger);
		logger.addAttribute("kind", "Program");
		program.expr.accept(this);
		endElement();
	}

	public void visit(PtrType ptrType) {
		begElement();
		((Position) ptrType).log(logger);
		logger.addAttribute("kind", "PtrType");
		ptrType.baseType.accept(this);
		endElement();
	}

	public void visit(RecType recType) {
		begElement();
		((Position) recType).log(logger);
		logger.addAttribute("kind", "RecType");
		for (int c = 0; c < recType.numComps(); c++)
			recType.comp(c).accept(this);
		endElement();
	}

	public void visit(TypeDecl typeDecl) {
		begElement();
		((Position) typeDecl).log(logger);
		logger.addAttribute("kind", "TypDecl");
		logger.addAttribute("name", typeDecl.name);
		typeDecl.type.accept(this);
		endElement();
	}

	public void visit(TypeError typeError) {
		begElement();
		logger.addAttribute("kind", "TypeError");
		endElement();
	}

	public void visit(TypeName typeName) {
		begElement();
		((Position) typeName).log(logger);
		logger.addAttribute("kind", "TypeName");
		logger.addAttribute("name", typeName.name());
		endElement();
	}

	public void visit(UnExpr unExpr) {
		begElement();
		((Position) unExpr).log(logger);
		logger.addAttribute("kind", "UnExpr:" + unExpr.oper.toString());
		unExpr.subExpr.accept(this);
		endElement();
	}

	public void visit(VarDecl varDecl) {
		begElement();
		((Position) varDecl).log(logger);
		logger.addAttribute("kind", "VarDecl");
		logger.addAttribute("name", varDecl.name);
		varDecl.type.accept(this);
		endElement();
	}

	public void visit(VarName varName) {
		begElement();
		((Position) varName).log(logger);
		logger.addAttribute("kind", "VarName");
		logger.addAttribute("name", varName.name());
		endElement();
	}

	public void visit(WhereExpr whereExpr) {
		begElement();
		((Position) whereExpr).log(logger);
		logger.addAttribute("kind", "WhereExpr");
		whereExpr.expr.accept(this);
		for (int d = 0; d < whereExpr.numDecls(); d++)
			whereExpr.decl(d).accept(this);
		endElement();
	}

	public void visit(WhileExpr whileExpr) {
		begElement();
		((Position) whileExpr).log(logger);
		logger.addAttribute("kind", "WhileExpr");
		whileExpr.cond.accept(this);
		whileExpr.body.accept(this);
		endElement();
	}

}
