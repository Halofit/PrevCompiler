package compiler.data.ast.code;

import compiler.data.ast.*;

/**
 * Abstract syntax tree visitor.
 * 
 * <p>
 * An object implementing interface {@link compiler.data.ast.code.Visitor} can
 * traverse the abstract syntax tree consisting of objects of classes defined in
 * package {@link compiler.data.ast}.
 * </p>
 * 
 * <p>
 * Every concrete class in package {@link compiler.data.ast} implements a
 * method {@link compiler.data.ast.AST#accept(Visitor) accept} which
 * calls back the visitor:
 * <ul>
 * <li>All code that must be executed at each particular node is implemented in
 * the corresponding visiting method.</li>
 * <li>To follow an edge in the abstract syntax tree, the object reachable by
 * the edge should be asked to accept the visitor instead of visiting an object
 * directly.</li>
 * </ul>
 * 
 * <p>
 * Example: see the source code of {@link compiler.data.ast.code.FullVisitor} and
 * {@link compiler.phase.abstr.AbstrToXML}.
 * </p>
 * 
 * @author sliva
 */
public interface Visitor {

	public void visit(ArrType arrType);

	public void visit(AtomExpr atomExpr);

	public void visit(AtomType atomType);

	public void visit(BinExpr binExpr);

	public void visit(CastExpr castExpr);

	public void visit(CompDecl compDecl);

	public void visit(CompName compName);
	
	public void visit(DeclError declError);

	public void visit(Exprs exprs);

	public void visit(ExprError errorExpr);

	public void visit(ForExpr forExpr);

	public void visit(FunCall funCall);

	public void visit(FunDecl funDecl);

	public void visit(FunDef funDef);

	public void visit(IfExpr ifExpr);

	public void visit(ParDecl parDecl);

	public void visit(Program program);

	public void visit(PtrType ptrType);

	public void visit(RecType recType);

	public void visit(TypeDecl typDecl);
	
	public void visit(TypeError typeError);

	public void visit(TypeName typeName);

	public void visit(UnExpr unExpr);

	public void visit(VarDecl varDecl);

	public void visit(VarName varName);

	public void visit(WhereExpr whereExpr);

	public void visit(WhileExpr whileExpr);

}
