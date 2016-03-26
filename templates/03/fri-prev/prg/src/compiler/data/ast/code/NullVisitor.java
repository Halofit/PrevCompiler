package compiler.data.ast.code;

import compiler.data.ast.*;

/**
 * A visitor that does nothing.
 * 
 * @author sliva
 */
public class NullVisitor implements Visitor {

	public void visit(ArrType arrType) {
	}

	public void visit(AtomExpr atomExpr) {
	}

	public void visit(AtomType atomType) {
	}

	public void visit(BinExpr binExpr) {
	}

	public void visit(CastExpr castExpr) {
	}

	public void visit(CompDecl compDecl) {
	}

	public void visit(CompName compName) {
	}
	
	public void visit(DeclError declError) {
	}

	public void visit(Exprs exprs) {
	}

	public void visit(ExprError exprError) {
	}

	public void visit(ForExpr forExpr) {
	}

	public void visit(FunCall funCall) {
	}

	public void visit(FunDecl funDecl) {
	}

	public void visit(FunDef funDef) {
	}

	public void visit(IfExpr ifExpr) {
	}

	public void visit(ParDecl parDecl) {
	}

	public void visit(Program program) {
	}

	public void visit(PtrType ptrType) {
	}

	public void visit(RecType recType) {
	}

	public void visit(TypeDecl typDecl) {
	}
	
	public void visit(TypeError typeError) {
	}

	public void visit(TypeName typeName) {
	}

	public void visit(UnExpr unExpr) {
	}

	public void visit(VarDecl varDecl) {
	}

	public void visit(VarName varName) {
	}

	public void visit(WhereExpr whereExpr) {
	}

	public void visit(WhileExpr whileExpr) {
	}

}
