package compiler.phase.codegen;

import compiler.Task;
import compiler.common.report.InternalCompilerError;
import compiler.data.codegen.*;
import compiler.data.frg.CodeFragment;
import compiler.data.frg.Fragment;
import compiler.data.imc.*;
import compiler.phase.Phase;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by gregor on 20.5.2016.
 */
public class CodeGen extends Phase {
	public static boolean commentAnnotations = false;
	public static boolean spacingComments = false;

	private int fpTemp;

	private FixedRegister sp = new FixedRegister("SP");
	private FixedRegister fp = new FixedRegister("FP");
	private FixedRegister reminderReg = new FixedRegister("rR");
	private FixedRegister colorRegister = new FixedRegister("COLORS"); //Color register is the highest used register

	private ConstantOperand const0 = new ConstantOperand(0);
	private ConstantOperand const1 = new ConstantOperand(1);

	private HashMap<CodeFragment, InstructionSet> fragInstrs;
	private HashMap<IMC, InstructionSet> map;

	public CodeGen(Task task) {
		super(task, "codegen");
		map = new HashMap<>();
		fragInstrs = task.fragInstrs;
	}

	@Override
	public void close() {
		super.close();

		PrintWriter writer;
		try {
			writer = new PrintWriter(task.srcFName + ".mmix", "US-ASCII");

			String indent = "\t";

			for (CodeFragment codeFragment : fragInstrs.keySet()) {
				writer.println("");
				writer.println(codeFragment.frame.label + ":");
				writer.println("... (prolog)");
				InstructionSet instrs = fragInstrs.get(codeFragment);
				for (Instruction instr : instrs.instrs) {
					writer.print(indent);
					writer.println(instr);
				}
				writer.println("... (epilog)");
				writer.println("");
				writer.println("");
			}

			writer.println("");
			writer.println("");
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void generateCode() {
		for (Fragment frag : task.fragments.values()) {
			if (frag instanceof CodeFragment) {
				fpTemp = ((CodeFragment) frag).FP;
				InstructionSet fragis = generateFragmentCode((CodeFragment) frag);
				fragInstrs.put((CodeFragment) frag, fragis);
			}
		}
	}

	public InstructionSet generateFragmentCode(CodeFragment frag) {
		frag.linCode.visit(this);
		return getInstrs(frag.linCode);
	}

	private InstructionSet getInstrs(IMC imcode) {
		InstructionSet instrs = map.get(imcode);
		if (instrs == null) throw new InternalCompilerError();
		return instrs;
	}

	private void setInstrs(IMC imcode, InstructionSet instrs) {
		if (instrs == null) throw new InternalCompilerError();
		map.put(imcode, instrs);
	}

	public void tile(BINOP binop) {
		boolean canFold2 = false;
		if (binop.expr2 instanceof CONST) {
			CONST c = (CONST) binop.expr2;
			if (c.value <= 0xFF && c.value >= 0) {
				canFold2 = true;
			}
		}

		boolean canFold1 = false;
		if (binop.expr1 instanceof CONST) {
			CONST c = (CONST) binop.expr1;
			if (c.value <= 0xFF && c.value >= 0) {
				canFold1 = true;
			}
		}

		InstructionSet ownis = new InstructionSet("BINOP | " + binop.oper.toString());
		Operand op1;
		Operand op2;

		if (canFold2) {
			binop.expr1.visit(this);
			InstructionSet is1 = getInstrs(binop.expr1);
			ownis.add(is1);

			op1 = is1.ret;
			op2 = new ConstantOperand(((CONST)binop.expr2).value);
		} else if (canFold1) {
			binop.expr2.visit(this);
			InstructionSet is2 = getInstrs(binop.expr2);
			ownis.add(is2);

			//In this case we need to rotate the operands
			op2 = new ConstantOperand(((CONST)binop.expr1).value);
			op1 = is2.ret;
		} else {
			binop.expr1.visit(this);
			binop.expr2.visit(this);
			InstructionSet is1 = getInstrs(binop.expr1);
			InstructionSet is2 = getInstrs(binop.expr2);
			ownis.add(is1);
			ownis.add(is2);
			op1 = is1.ret;
			op2 = is2.ret;
		}

		//For consistency create register AFTER visiting nested statements
		ownis.set(VirtualRegister.create());
		Operand ret = ownis.ret;


		switch (binop.oper) {
			case OR:
				ownis.add(new Mnemonic("OR", ret, op1, op2));
				break;
			case AND:
				ownis.add(new Mnemonic("AND", ret, op1, op2));
				break;
			case EQU:
				ownis.add(new Mnemonic("CMP", ret, op1, op2));
				ownis.add(new Mnemonic("OR", ret, ret, const1));
				ownis.add(new Mnemonic("CMP", ret, ret, const1));
				break;
			case NEQ:
				ownis.add(new Mnemonic("CMP", ret, op1, op2));
				break;
			case LTH:
				ownis.add(new Mnemonic("CMP", ret, op1, op2));
				ownis.add(new Mnemonic("ADD", ret, ret, const1));
				ownis.add(new Mnemonic("CMP", ret, ret, const0));
				ownis.add(new Mnemonic("SUB", ret, ret, const1));
				break;
			case GTH:
				ownis.add(new Mnemonic("CMP", ret, op1, op2));
				ownis.add(new Mnemonic("SUB", ret, ret, const1));
				ownis.add(new Mnemonic("CMP", ret, ret, const0));
				ownis.add(new Mnemonic("ADD", ret, ret, const1));
				break;
			case LEQ:
				ownis.add(new Mnemonic("CMP", ret, op1, op2));
				ownis.add(new Mnemonic("SUB", ret, ret, const1));
				break;
			case GEQ:
				ownis.add(new Mnemonic("CMP", ret, op1, op2));
				ownis.add(new Mnemonic("ADD", ret, ret, const1));
				break;
			case ADD:
				ownis.add(new Mnemonic("ADD", ret, op1, op2));
				break;
			case SUB:
				ownis.add(new Mnemonic("SUB", ret, op1, op2));
				break;
			case MUL:
				ownis.add(new Mnemonic("MUL", ret, op1, op2));
				break;
			case DIV:
				ownis.add(new Mnemonic("DIV", ret, op1, op2));
				break;
			case MOD:
				ownis.add(new Mnemonic("DIV", ret, op1, op2));
				ownis.add(new Mnemonic("GET", ret, reminderReg));
				break;
		}

		setInstrs(binop, ownis);
	}

	public void tile(CALL call) {
		for (IMCExpr arg : call.args) {
			arg.visit(this);
		}

		InstructionSet ownis = new InstructionSet("CALL");
		ownis.set(VirtualRegister.create());

		long argOffsetCnt = 0;

		for (IMCExpr arg : call.args) {
			InstructionSet argis = getInstrs(arg);
			ownis.add(argis);
			ownis.add(new Mnemonic("STO", argis.ret, sp, new ConstantOperand(argOffsetCnt)));
			argOffsetCnt += 8;
		}

		ownis.add(new Mnemonic("PUSHJ", colorRegister, new OperandLabel(call.label)));
		ownis.add(new Mnemonic("LDO", ownis.ret, sp, const0));

		setInstrs(call, ownis);
	}

	public void tile(CJUMP cjump) {
		cjump.cond.visit(this);

		InstructionSet condis = getInstrs(cjump.cond);
		InstructionSet ownis = new InstructionSet("CJUMP");
		ownis.add(condis);

		//NOTE: CJUMP assumes the negative label ALWAYS follows the cjump instruction
		// 			this is guaranteed by the basic blocks section
		//This means we jump on a non-negative value (true)
		ownis.add(new Mnemonic("PBNZ", condis.ret, new OperandLabel(cjump.posLabel)));

		// If the previous assert doesn't hold, uncomment this line:
		//ownis.add(new Mnemonic("JMP", new OperandLabel(cjump.negLabel)));

		setInstrs(cjump, ownis);
	}


	public void tile(CONST constant) {
		long val = constant.value;

		InstructionSet ownis = new InstructionSet("CONST");
		ownis.set(VirtualRegister.create());
		Register ret = ownis.ret;

		ownis.add(new Mnemonic("SETL", ret, new ConstantOperand(val & 0xFFFFL)));
		val = val >> 16;
		if (val != 0) {
			ownis.add(new Mnemonic("INCML", ret, new ConstantOperand(val & 0xFFFFL)));
			val = val >> 16;
		}

		if (val != 0) {
			ownis.add(new Mnemonic("INCMH", ret, new ConstantOperand(val & 0xFFFFL)));
			val = val >> 16;
		}

		if (val != 0) {
			ownis.add(new Mnemonic("INCH", ret, new ConstantOperand(val & 0xFFFFL)));
		}

		setInstrs(constant, ownis);
	}


	public void tile(ESTMT estmt) {
		estmt.expr.visit(this);
		InstructionSet ownis = new InstructionSet("ESTMT");
		InstructionSet nis = getInstrs(estmt.expr);

		ownis.add(nis);
		ownis.set(nis.ret);

		setInstrs(estmt, ownis);
	}


	public void tile(JUMP jump) {
		InstructionSet ownis = new InstructionSet("JUMP");
		ownis.add(new Mnemonic("JMP", new OperandLabel(jump.label)));

		setInstrs(jump, ownis);
	}


	public void tile(LABEL label) {
		InstructionSet ownis = new InstructionSet("LABEL");
		ownis.add(Label.get(label.label));
		setInstrs(label, ownis);
	}


	public void tile(MEM mem) {
		mem.addr.visit(this);

		InstructionSet addris = getInstrs(mem.addr);
		InstructionSet ownis = new InstructionSet("MEM");
		Register locReg = addris.ret;

		String mnemonic;

		switch ((byte) mem.width) {
			case 1:
				mnemonic = "LDB";
				break;
			case 2:
				mnemonic = "LDW";
				break;
			case 4:
				mnemonic = "LDT";
				break;
			case 8:
				mnemonic = "LDO";
				break;
			default:
				throw new InternalCompilerError();
		}

		ownis.set(VirtualRegister.create());
		ownis.add(addris);
		ownis.add(new Mnemonic(mnemonic, ownis.ret, locReg, const0));

		setInstrs(mem, ownis);
	}

	// BTW -> I hate this ImCode, who thought that MEM being used for 2 different things is a good idea?
	public void tile(MOVE move) {
		move.src.visit(this);
		move.dst.visit(this);

		InstructionSet srcis = getInstrs(move.src);
		InstructionSet dstis = getInstrs(move.dst);
		InstructionSet ownis = new InstructionSet("MOVE");

		if(srcis.ret != null){

			//This ordering needs to be this way so the fixing below can work
			ownis.add(srcis);
			ownis.add(dstis);

			Register dstret = dstis.ret;
			Register srcret = srcis.ret;


			if (move.dst instanceof MEM) {
				//If the left side is MEM then we have to remove the last instruction
				// which should be LDB/LDW/LDT/LDO and replace with SDB/SDW/SDT/SDO

				Instruction lasttmp = ownis.popLast();
				if (!(lasttmp instanceof Mnemonic)) {
					System.out.println(lasttmp);
					throw new InternalCompilerError();
				}
				Mnemonic last = (Mnemonic) lasttmp;
				String mnemonic;
				switch (last.mnemonic) {
					case "LDB":
						mnemonic = "STB";
						break;
					case "LDW":
						mnemonic = "STW";
						break;
					case "LDT":
						mnemonic = "STT";
						break;
					case "LDO":
						mnemonic = "STO";
						break;
					default:
						throw new InternalCompilerError();
				}

				ownis.add(new Mnemonic(mnemonic, srcret, last.operands[1], last.operands[2]));
			} else {
				ownis.add(new Mnemonic("ADD", dstret, srcret, const0));
			}
		}

		setInstrs(move, ownis);
	}


	public void tile(NAME name) {
		InstructionSet ownis = new InstructionSet("NAME | " + name.name);
		ownis.set(VirtualRegister.create());
		ownis.add(new Mnemonic("GETA", ownis.ret, new OperandLabel(name.name)));

		setInstrs(name, ownis);
	}


	public void tile(NOP nop) {
		InstructionSet ownis = new InstructionSet("NOP");
		setInstrs(nop, ownis);
	}


	public void tile(SEXPR sexpr) {
		sexpr.stmt.visit(this);
		sexpr.expr.visit(this);

		InstructionSet stmtis = getInstrs(sexpr.stmt);
		InstructionSet expris = getInstrs(sexpr.expr);

		InstructionSet ownis = new InstructionSet("SEXPR");
		ownis.add(stmtis);
		ownis.add(expris);
		ownis.set(expris.ret);

		setInstrs(sexpr, ownis);
	}


	public void tile(STMTS stmts) {
		for (IMCStmt stmt : stmts.stmts) {
			stmt.visit(this);
		}

		InstructionSet ownis = new InstructionSet("STMTS");

		for (IMCStmt stmt : stmts.stmts) {

			if (commentAnnotations) {
				if (stmt instanceof MOVE) {
					ownis.add(new Comment("Move (" + ((MOVE) stmt).id + ")"));
				} else if (stmt instanceof CJUMP) {
					ownis.add(new Comment("Cjump"));
				} else if (stmt instanceof JUMP) {
					ownis.add(new Comment("Jump"));
				} else if (stmt instanceof LABEL) ownis.add(new Comment("Label"));
			}

			ownis.add(getInstrs(stmt));
			if (spacingComments) ownis.add(new Comment(null));
		}

		setInstrs(stmts, ownis);
	}


	public void tile(TEMP temp) {
		InstructionSet ownis = new InstructionSet("TEMP |" + temp.name);
		if (temp.name == fpTemp) {
			ownis.set(fp);
		} else {
			ownis.set(VirtualRegister.create(temp.name));
		}
		setInstrs(temp, ownis);
	}


	public void tile(UNOP unop) {
		unop.expr.visit(this);

		InstructionSet ownis = new InstructionSet("UNOP");
		InstructionSet expris = getInstrs(unop.expr);

		ownis.add(expris);
		Register ret = ownis.ret;
		ownis.set(ret);

		switch (unop.oper) {
			case ADD:
				break;
			case SUB:
				ownis.add(new Mnemonic("NEG", ret, ret));
				break;
			case NOT:
				ownis.add(new Mnemonic("CMP", ret, ret, const0));
				ownis.add(new Mnemonic("OR", ret, ret, const1));
				ownis.add(new Mnemonic("SUB", ret, ret, const1));
				break;
		}

		setInstrs(unop, ownis);
	}


}
