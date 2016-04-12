package compiler.phase.seman;

import compiler.common.report.InternalCompilerError;
import compiler.data.ast.*;
import compiler.data.ast.attr.Attributes;
import compiler.data.ast.code.FullVisitor;
import compiler.data.typ.*;

import java.util.LinkedList;

/**
 * Type checker.
 * <p>
 * <p>
 * Type checker checks type of all sentential forms of the program and resolves
 * the component names as this cannot be done earlier, i.e., in
 * {@link compiler.phase.seman.EvalDecl}.
 * </p>
 *
 * @author sliva
 */
public class EvalTyp extends FullVisitor {

	private final Attributes attrs;

	public EvalTyp(Attributes attrs) {
		this.attrs = attrs;
	}

	/**
	 * The symbol table.
	 */
	private SymbolTable symbolTable = new SymbolTable();

	//id of namespaces
	private static long namespaceGenerator = 0L;

	private static String namespaceNameGen() {
		Long l = namespaceGenerator;
		namespaceGenerator++;
		return l.toString();
	}

	// TODO

	@Override
	public void visit(ArrType arrType) {
		super.visit(arrType);

		Long size = attrs.valueAttr.get(arrType.size);
		if (size == null) {
			SemAn.signalError("Arrays must be of constant size.", arrType);
		}
		if (size < 1) {
			SemAn.signalError("Arrays must be at least size of 1", arrType);
		}

		Typ baseType = attrs.typAttr.get(arrType.elemType);
		ArrTyp at = new ArrTyp(size, baseType);
		attrs.typAttr.set(arrType, at);
	}

	@Override
	public void visit(AtomExpr atomExpr) {
		super.visit(atomExpr);

		Typ atTyp;

		switch (atomExpr.type) {
			case INTEGER:
				atTyp = new IntegerTyp();
				break;
			case BOOLEAN:
				atTyp = new BooleanTyp();
				break;
			case CHAR:
				atTyp = new CharTyp();
				break;
			case STRING:
				atTyp = new StringTyp();
				break;
			case PTR:
				atTyp = new PtrTyp(new VoidTyp());
				break;
			case VOID:
				atTyp = new VoidTyp();
				break;
			default:
				//Should never happen, except if I add a new type and forget about it
				throw new InternalCompilerError();
		}

		attrs.typAttr.set(atomExpr, atTyp);
	}

	@Override
	public void visit(AtomType atomType) {
		super.visit(atomType);

		Typ aTyp;
		switch (atomType.type) {
			case INTEGER:
				aTyp = new IntegerTyp();
				break;
			case BOOLEAN:
				aTyp = new BooleanTyp();
				break;
			case CHAR:
				aTyp = new CharTyp();
				break;
			case STRING:
				aTyp = new StringTyp();
				break;
			case VOID:
				aTyp = new VoidTyp();
				break;
			default:
				//Should never happen, except if I add a new type and forget about it
				throw new InternalCompilerError();
		}

		attrs.typAttr.set(atomType, aTyp);
	}

	@Override
	public void visit(BinExpr binExpr) {
		super.visit(binExpr);
		Expr op1 = binExpr.fstExpr;
		Expr op2 = binExpr.sndExpr;
		Typ op1T = attrs.typAttr.get(op1);
		Typ op2T = attrs.typAttr.get(op2);

		Typ binE = null;

		switch (binExpr.oper) {
			case ASSIGN:
				//if op1T is typ1 and opt2T is typ2 and typ1 is in memory than this is void type
				if (op1T.isStructEquivTo(op2T)) {
					//TODO typ1 is in memory?
					binE = new VoidTyp();
				} else {
					SemAn.signalError("Assignement  operator type mismatch.", binExpr);
				}

				break;
			case OR:
			case AND:
				if (op1T instanceof BooleanTyp && op2T instanceof BooleanTyp) {
					binE = new BooleanTyp();
				} else {
					SemAn.signalError("Boolean expresions are only allowed on boolean types", binExpr);
				}
				break;

			case EQU:
			case NEQ:
			case LTH:
			case GTH:
			case LEQ:
			case GEQ:
				if (op1T instanceof BooleanTyp || op1T instanceof IntegerTyp || op1T instanceof CharTyp) {
					if (op1T.isStructEquivTo(op2T)) {
						binE = new BooleanTyp();
					} else {
						SemAn.signalError("Can only compare values of the same type", binExpr);
					}
				} else {
					SemAn.signalError("Comparisons are only allowed on integers, booleans and characters", binExpr);
				}
				break;

			case ADD:
			case SUB:
			case MUL:
			case DIV:
			case MOD:
				if (op1T instanceof IntegerTyp && op2T instanceof IntegerTyp) {
					binE = new IntegerTyp();
				} else {
					SemAn.signalError("Arithmetic operation attempted with non-integers.", binExpr);
				}
				break;

			case ARR:
				if (!(op2T instanceof IntegerTyp)) {
					SemAn.signalError("Arrays must be indexed with integers", binExpr);
				} else {
					binE = op1T.actualTyp();
				}
				break;
			case REC:
				//TODO determine types of components
				//TODO resolve name of components
				if (op1T instanceof RecTyp) {
					VarName recVar = (VarName) op1;
					Decl recDecl = attrs.declAttr.get(recVar);
					if (recDecl == null) SemAn.signalError("Undeclared record use.", binExpr);
					Typ recType = attrs.typAttr.get(recDecl);
					if (recType == null) SemAn.signalError("Cannot determine the record's type.", binExpr);
					if (!(op2 instanceof CompName))
						SemAn.signalError("Right side of . (dot) operator must be a component name.", binExpr);

					//Resolve the component name
					Decl decl = symbolTable.fndDecl(((Integer) recType.hashCode()).toString(), ((VarName) op1).name());
					if (decl == null) SemAn.signalError("Cannot resolve component name.", binExpr);
					attrs.declAttr.set((CompName) op2, decl);

					Typ compType = attrs.typAttr.get(op2);
					if (compType == null) SemAn.signalError("Cannot determine component's type.", binExpr);
					binE = compType;
				} else {
					SemAn.signalError("Can only use . (dot) operator on record types.", binExpr);
				}
				break;

		}

		attrs.typAttr.set(binExpr, binE);
	}

	@Override
	public void visit(CastExpr castExpr) {
		super.visit(castExpr);

		Typ castTyp = attrs.typAttr.get(castExpr.type);
		Typ exprTyp = attrs.typAttr.get(castExpr.expr);

		if (!(castTyp instanceof PtrTyp)
				|| !(exprTyp instanceof PtrTyp)
				|| !(((PtrTyp) exprTyp).baseTyp instanceof VoidTyp)) {
			SemAn.signalError("Can only cast void pointer types to other ptr types", castExpr);
		}

		attrs.typAttr.set(castExpr, castTyp);
	}

	@Override
	public void visit(CompDecl compDecl) {
		super.visit(compDecl);

		Typ compType = attrs.typAttr.get(compDecl.type);
		if (compType == null) SemAn.signalError("Unknown type in component declaration.", compDecl);
		attrs.typAttr.set(compDecl, compType);
	}

	@Override
	public void visit(CompName compName) {
		super.visit(compName);
		Typeable t = attrs.declAttr.get(compName);
		if (t == null) SemAn.signalError("Cannot resolve component name", compName);
		Typ compType = attrs.typAttr.get(t);
		if (compType == null) SemAn.signalError("Cannot resolve component type", compName);
		attrs.typAttr.set(compName, compType);
	}

	@Override
	public void visit(DeclError declError) {
		super.visit(declError);
		SemAn.signalError("DeclError in AST tree, exiting.", declError);
	}

	@Override
	public void visit(Exprs exprs) {
		super.visit(exprs);

		for(Expr e: exprs.exprs){
			if(attrs.typAttr.get(e) == null) SemAn.signalError("One of expressions has an unindentified type.", exprs);
		}

		Expr last = exprs.lastExpr();
		Typ exprsTyp = attrs.typAttr.get(last);
		if (exprsTyp == null) SemAn.signalError("Cannot find last expressions type.", exprs); //Redundant
		attrs.typAttr.set(exprs, exprsTyp);
	}

	@Override
	public void visit(ExprError exprError) {
		super.visit(exprError);
		SemAn.signalError("ExprError in AST tree, exiting.", exprError);
	}

	@Override
	public void visit(ForExpr forExpr) {
		super.visit(forExpr);

		//Do type checking on the for statement
		if (!(attrs.typAttr.get(forExpr.var) instanceof IntegerTyp)) {
			SemAn.signalError("For interation variable must be an integer.", forExpr);
		}

		if (!(attrs.typAttr.get(forExpr.loBound) instanceof IntegerTyp)) {
			SemAn.signalError("For lower bound must be an integer.", forExpr);
		}

		if (!(attrs.typAttr.get(forExpr.hiBound) instanceof IntegerTyp)) {
			SemAn.signalError("For high bound must be an integer.", forExpr);
		}

		if (attrs.typAttr.get(forExpr.body) == null) {
			SemAn.signalError("For body must have a type.", forExpr);
		}

		//For statement is of tyop void
		attrs.typAttr.set(forExpr, new VoidTyp());
	}

	@Override
	public void visit(FunCall funCall) {
		super.visit(funCall);

		FunDecl decl = (FunDecl) attrs.declAttr.get(funCall);
		FunTyp declType = (FunTyp) attrs.typAttr.get(decl);
		if(decl == null || declType == null) SemAn.signalError("Cannot determine function delaration type.", funCall);

		if(decl.numPars() != funCall.numArgs()) {
			SemAn.signalError("Argument parity error: expected " + decl.numPars() + "arguments, got " + funCall.numArgs() + ".", funCall);
		}

		for (int i = 0; i < decl.numPars(); i++) {
			Typ parTyp = attrs.typAttr.get(decl.par(i));
			Typ argTyp = attrs.typAttr.get(funCall.arg(i));

			if(parTyp == null) SemAn.signalError("Cannot determine parameter type", funCall);
			if(argTyp == null) SemAn.signalError("Cannot determine argument type",  funCall);

			if(!parTyp.getClass().equals(argTyp.getClass())){
				SemAn.signalError("Type mismatch in argument " + i + ".", funCall);
			}
		}

		attrs.typAttr.set(funCall, declType.resultTyp);

	}

	@Override
	public void visit(FunDecl funDecl) {
		super.visit(funDecl);

		LinkedList<Typ> parTyps = new LinkedList<>();
		for(ParDecl e: funDecl.pars){
			Typ t = attrs.typAttr.get(e);
			if(t == null) SemAn.signalError("Cannot determine param type", funDecl);
			parTyps.add(t);
		}

		attrs.typAttr.set(funDecl, new FunTyp(parTyps, ));
	}

	@Override
	public void visit(FunDef funDef) {
		super.visit(funDef);
	}

	@Override
	public void visit(IfExpr ifExpr) {
		super.visit(ifExpr);
	}

	@Override
	public void visit(ParDecl parDecl) {
		super.visit(parDecl);
	}

	@Override
	public void visit(Program program) {
		super.visit(program);
	}

	@Override
	public void visit(PtrType ptrType) {
		super.visit(ptrType);
	}

	@Override
	public void visit(RecType recType) {
		super.visit(recType);

		// HACK -  probably rework
		// Use the hashCode of the object itself.
		// Hashcodes by default are normally unique, since they represent the internal Java ID
		String nsname = ((Integer) recType.hashCode()).toString();
		symbolTable.enterNamespace(nsname);
		for (CompDecl cd : recType.comps) {
			symbolTable.insDecl(nsname, cd.name, cd);
			cd.accept(this);
		}
		symbolTable.leaveNamespace();
	}

	@Override
	public void visit(TypeDecl typDecl) {
		super.visit(typDecl);
	}

	@Override
	public void visit(TypeError typeError) {
		super.visit(typeError);
	}

	@Override
	public void visit(TypeName typeName) {
		super.visit(typeName);
	}

	@Override
	public void visit(UnExpr unExpr) {
		super.visit(unExpr);
	}

	@Override
	public void visit(VarDecl varDecl) {
		super.visit(varDecl);
	}

	@Override
	public void visit(VarName varName) {
		super.visit(varName);
	}

	@Override
	public void visit(WhereExpr whereExpr) {
		super.visit(whereExpr);
	}

	@Override
	public void visit(WhileExpr whileExpr) {
		super.visit(whileExpr);
	}
}
