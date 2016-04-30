package compiler.data.ast.code;

import compiler.data.ast.*;

/**
 * A visitor that traverses the entire abstract syntax tree but does nothing.
 * 
 * @author sliva
 */
public class FullVisitor implements Visitor {

	public void visit(ArrType arrType) {
		arrType.size.accept(this);
		arrType.elemType.accept(this);
	}

	public void visit(AtomExpr atomExpr) {
	}

	public void visit(AtomType atomType) {
	}

	public void visit(BinExpr binExpr) {
		binExpr.fstExpr.accept(this);
		binExpr.sndExpr.accept(this);
	}

	public void visit(CastExpr castExpr) {
		castExpr.type.accept(this);
		castExpr.expr.accept(this);
	}

	public void visit(CompDecl compDecl) {
		compDecl.type.accept(this);
	}

	public void visit(CompName compName) {
	}
	
	public void visit(DeclError declError) {
	}

	public void visit(Exprs exprs) {
		for (int e = 0; e < exprs.numExprs(); e++)
			exprs.expr(e).accept(this);
	}

	public void visit(ExprError exprError) {
	}

	public void visit(ForExpr forExpr) {
		forExpr.var.accept(this);
		forExpr.loBound.accept(this);
		forExpr.hiBound.accept(this);
		forExpr.body.accept(this);
	}

	public void visit(FunCall funCall) {
		for (int a = 0; a < funCall.numArgs(); a++)
			funCall.arg(a).accept(this);
	}

	public void visit(FunDecl funDecl) {
		for (int p = 0; p < funDecl.numPars(); p++)
			funDecl.par(p).accept(this);
		funDecl.type.accept(this);
	}

	public void visit(FunDef funDef) {
		for (int p = 0; p < funDef.numPars(); p++)
			funDef.par(p).accept(this);
		funDef.type.accept(this);
		funDef.body.accept(this);
	}

	public void visit(IfExpr ifExpr) {
		ifExpr.cond.accept(this);
		ifExpr.thenExpr.accept(this);
		ifExpr.elseExpr.accept(this);
	}

	public void visit(ParDecl parDecl) {
		parDecl.type.accept(this);
	}

	public void visit(Program program) {
		program.expr.accept(this);
	}

	public void visit(PtrType ptrType) {
		ptrType.baseType.accept(this);
	}

	public void visit(RecType recType) {
		for (int c = 0; c < recType.numComps(); c++)
			recType.comp(c).accept(this);
	}

	public void visit(TypeDecl typDecl) {
		typDecl.type.accept(this);
	}
	
	public void visit(TypeError typeError) {
	}

	public void visit(TypeName typeName) {
	}

	public void visit(UnExpr unExpr) {
		unExpr.subExpr.accept(this);
	}

	public void visit(VarDecl varDecl) {
		varDecl.type.accept(this);
	}

	public void visit(VarName varName) {
	}

	public void visit(WhereExpr whereExpr) {
		whereExpr.expr.accept(this);
		for (int d = 0; d < whereExpr.numDecls(); d++)
			whereExpr.decl(d).accept(this);
	}

	public void visit(WhileExpr whileExpr) {
		whileExpr.cond.accept(this);
		whileExpr.body.accept(this);
	}

}
