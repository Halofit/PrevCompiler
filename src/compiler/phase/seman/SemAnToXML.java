package compiler.phase.seman;

import compiler.common.logger.*;
import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.typ.*;
import compiler.phase.abstr.*;

/**
 * A visitor for printing out the XML description of the abstract syntax tree
 * including the information computed during semantic analysis.
 * 
 * <p>
 * Traverses the abstract syntax tree in a depth-first manner and produces the
 * XML description of the abstract syntax tree.
 * </p>
 * 
 * @author sliva
 */
public class SemAnToXML extends AbstrToXML {
	
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
	 */
	public SemAnToXML(Logger logger, boolean boxed, Attributes attrs) {
		super(logger, false);
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
		logger.begElement("seman");
		if (attrs.typAttr.get(arrType) != null)
			attrs.typAttr.get(arrType).log(logger);
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(AtomExpr atomExpr) {
		begElement();
		super.visit(atomExpr);
		logger.begElement("seman");
		if (attrs.typAttr.get(atomExpr) != null)
			attrs.typAttr.get(atomExpr).log(logger);
		if (attrs.valueAttr.get(atomExpr) != null)
			logger.addAttribute("value", attrs.valueAttr.get(atomExpr).toString());
		if (attrs.memAttr.get(atomExpr))
			logger.addAttribute("mem", "true");
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(AtomType atomType) {
		begElement();
		super.visit(atomType);
		logger.begElement("seman");
		if (attrs.typAttr.get(atomType) != null)
			attrs.typAttr.get(atomType).log(logger);
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(BinExpr binExpr) {
		begElement();
		super.visit(binExpr);
		logger.begElement("seman");
		if (attrs.typAttr.get(binExpr) != null)
			attrs.typAttr.get(binExpr).log(logger);
		if (attrs.valueAttr.get(binExpr) != null)
			logger.addAttribute("value", attrs.valueAttr.get(binExpr).toString());
		if (attrs.memAttr.get(binExpr))
			logger.addAttribute("mem", "true");
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(CastExpr castExpr) {
		begElement();
		super.visit(castExpr);
		logger.begElement("seman");
		if (attrs.typAttr.get(castExpr) != null)
			attrs.typAttr.get(castExpr).log(logger);
		logger.endElement();
		if (attrs.memAttr.get(castExpr))
			logger.addAttribute("mem", "true");
		endElement();
	}

	@Override
	public void visit(CompDecl compDecl) {
		begElement();
		super.visit(compDecl);
		logger.begElement("seman");
		if (attrs.typAttr.get(compDecl) != null)
			attrs.typAttr.get(compDecl).log(logger);
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(CompName compName) {
		begElement();
		super.visit(compName);
		logger.begElement("seman");
		if (attrs.typAttr.get(compName) != null)
			attrs.typAttr.get(compName).log(logger);
		{
			Decl decl = attrs.declAttr.get(compName);
			if (decl != null) {
				if (!(decl instanceof CompDecl))
					throw new InternalCompilerError();
				logger.addAttribute("decl", ((Position) decl).toString());
			}
		}
		if (attrs.memAttr.get(compName))
			logger.addAttribute("mem", "true");
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(DeclError declError) {
		begElement();
		super.visit(declError);
		logger.begElement("seman");
		if (attrs.typAttr.get(declError) != null)
			attrs.typAttr.get(declError).log(logger);
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(Exprs exprs) {
		begElement();
		super.visit(exprs);
		logger.begElement("seman");
		if (attrs.typAttr.get(exprs) != null)
			attrs.typAttr.get(exprs).log(logger);
		if (attrs.memAttr.get(exprs))
			logger.addAttribute("mem", "true");
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(ExprError exprError) {
		begElement();
		super.visit(exprError);
		logger.begElement("seman");
		if (attrs.typAttr.get(exprError) != null)
			attrs.typAttr.get(exprError).log(logger);
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(ForExpr forExpr) {
		begElement();
		super.visit(forExpr);
		logger.begElement("seman");
		if (attrs.typAttr.get(forExpr) != null)
			attrs.typAttr.get(forExpr).log(logger);
		if (attrs.memAttr.get(forExpr))
			logger.addAttribute("mem", "true");
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(FunCall funCall) {
		begElement();
		super.visit(funCall);
		logger.begElement("seman");
		if (attrs.typAttr.get(funCall) != null)
			attrs.typAttr.get(funCall).log(logger);
		{
			Decl decl = attrs.declAttr.get(funCall);
			if (decl != null) {
				if (!(decl instanceof FunDecl))
					throw new InternalCompilerError();
				logger.addAttribute("decl", ((Position) decl).toString());
			}
		}
		if (attrs.memAttr.get(funCall))
			logger.addAttribute("mem", "true");
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(FunDecl funDecl) {
		begElement();
		super.visit(funDecl);
		logger.begElement("seman");
		if (attrs.typAttr.get(funDecl) != null)
			attrs.typAttr.get(funDecl).log(logger);
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(FunDef funDef) {
		begElement();
		super.visit(funDef);
		logger.begElement("seman");
		if (attrs.typAttr.get(funDef) != null)
			attrs.typAttr.get(funDef).log(logger);
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(IfExpr ifExpr) {
		begElement();
		super.visit(ifExpr);
		logger.begElement("seman");
		if (attrs.typAttr.get(ifExpr) != null)
			attrs.typAttr.get(ifExpr).log(logger);
		if (attrs.memAttr.get(ifExpr))
			logger.addAttribute("mem", "true");
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(ParDecl parDecl) {
		begElement();
		super.visit(parDecl);
		logger.begElement("seman");
		if (attrs.typAttr.get(parDecl) != null)
			attrs.typAttr.get(parDecl).log(logger);
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(Program program) {
		begElement();
		super.visit(program);
		logger.begElement("seman");
		if (attrs.typAttr.get(program) != null)
			attrs.typAttr.get(program).log(logger);
		if (attrs.memAttr.get(program))
			logger.addAttribute("mem", "true");
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(PtrType ptrType) {
		begElement();
		super.visit(ptrType);
		logger.begElement("seman");
		if (attrs.typAttr.get(ptrType) != null)
			attrs.typAttr.get(ptrType).log(logger);
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(RecType recType) {
		begElement();
		super.visit(recType);
		logger.begElement("seman");
		if (attrs.typAttr.get(recType) != null)
			attrs.typAttr.get(recType).log(logger);
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(TypeDecl typDecl) {
		begElement();
		super.visit(typDecl);
		logger.begElement("seman");
		if (attrs.typAttr.get(typDecl) != null) {
			logger.begElement("typ");
			logger.addAttribute("kind", "NAME(" + ((TypName) (attrs.typAttr.get(typDecl))).name + ")");
			((TypName) (attrs.typAttr.get(typDecl))).getType().log(logger);
			logger.endElement();
		}
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(TypeError typeError) {
		begElement();
		super.visit(typeError);
		logger.begElement("seman");
		if (attrs.typAttr.get(typeError) != null)
			attrs.typAttr.get(typeError).log(logger);
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(TypeName typeName) {
		begElement();
		super.visit(typeName);
		logger.begElement("seman");
		if (attrs.typAttr.get(typeName) != null)
			attrs.typAttr.get(typeName).log(logger);
		{
			Decl decl = attrs.declAttr.get(typeName);
			if (decl != null) {
				if (!(decl instanceof TypeDecl))
					throw new InternalCompilerError();
				logger.addAttribute("decl", ((Position) decl).toString());
			}
		}
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(UnExpr unExpr) {
		begElement();
		super.visit(unExpr);
		logger.begElement("seman");
		if (attrs.typAttr.get(unExpr) != null)
			attrs.typAttr.get(unExpr).log(logger);
		if (attrs.valueAttr.get(unExpr) != null)
			logger.addAttribute("value", attrs.valueAttr.get(unExpr).toString());
		if (attrs.memAttr.get(unExpr))
			logger.addAttribute("mem", "true");
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(VarDecl varDecl) {
		begElement();
		super.visit(varDecl);
		logger.begElement("seman");
		if (attrs.typAttr.get(varDecl) != null)
			attrs.typAttr.get(varDecl).log(logger);
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(VarName varName) {
		begElement();
		super.visit(varName);
		logger.begElement("seman");
		if (attrs.typAttr.get(varName) != null)
			attrs.typAttr.get(varName).log(logger);
		{
			Decl decl = attrs.declAttr.get(varName);
			if (decl != null) {
				if (!(decl instanceof VarDecl))
					throw new InternalCompilerError();
				logger.addAttribute("decl", ((Position) decl).toString());
			}
		}
		if (attrs.memAttr.get(varName))
			logger.addAttribute("mem", "true");
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(WhereExpr whereExpr) {
		begElement();
		super.visit(whereExpr);
		logger.begElement("seman");
		if (attrs.typAttr.get(whereExpr) != null)
			attrs.typAttr.get(whereExpr).log(logger);
		if (attrs.memAttr.get(whereExpr))
			logger.addAttribute("mem", "true");
		logger.endElement();
		endElement();
	}

	@Override
	public void visit(WhileExpr whileExpr) {
		begElement();
		super.visit(whileExpr);
		logger.begElement("seman");
		if (attrs.typAttr.get(whileExpr) != null)
			attrs.typAttr.get(whileExpr).log(logger);
		if (attrs.memAttr.get(whileExpr))
			logger.addAttribute("mem", "true");
		logger.endElement();
		endElement();
	}

}
