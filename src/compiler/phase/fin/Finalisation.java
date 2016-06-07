package compiler.phase.fin;

import compiler.Main;
import compiler.Task;
import compiler.common.report.InternalCompilerError;
import compiler.data.codegen.*;
import compiler.data.frg.CodeFragment;
import compiler.data.frg.ConstFragment;
import compiler.data.frg.DataFragment;
import compiler.data.frg.Fragment;
import compiler.phase.Phase;
import compiler.phase.regalloc.RegisterAlloc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by gregor on 31. 05. 2016.
 */
public class Finalisation extends Phase {

	private static final String fileEnding = ".mms";

	private HashMap<CodeFragment, InstructionSet> fragInstrs;
	private HashMap<String, Fragment> fragments;

	public Finalisation(Task task) {
		super(task, "fin", false);

		this.fragInstrs = task.fragInstrs;
		this.fragments = task.fragments;
	}

	public void finishCode() {
		//This is no longer logged, this is the final result

		for (CodeFragment cf : fragInstrs.keySet()) {
			InstructionSet is = fragInstrs.get(cf);
			is.injectEntryAndExit(cf);
			is.joinLabels();
		}

		PrintWriter writer;
		try {
			writer = new PrintWriter(task.srcFName + fileEnding, "US-ASCII");

			//Header
			writer.println();
			writer.println();
			writer.println("\t\tLOC	Data_Segment");

			//define the common registers
			writer.println("FP IS $252"); //No indentation, FP mus be a label
			writer.println("SP IS $253");
			writer.println("RV IS $0");
			writer.println("COLORS IS $" + RegisterAlloc.physicalRegisters);
			writer.println();
			writer.println("%allocate a global register for loading constants and global variables");
			writer.println("\t\tGREG @");
			writer.println();
			writer.println();

			for (String s : fragments.keySet()) {
				Fragment f = fragments.get(s);

				if (f instanceof CodeFragment) {
					//For now do nothing, we should first handle other fragments
				} else if (f instanceof ConstFragment) {
					ConstFragment cf = (ConstFragment) f;
					writer.println(s + "\tBYTE\t" + cf.getStringAsValues());
				} else if (f instanceof DataFragment) {
					writer.println(s + "\tOCTA\t" + 0);
				} else {
					throw new InternalCompilerError();
				}
			}

			writer.println();
			writer.println();

			//set location for instructions
			writer.println("\t\tLOC	#100");
			writer.println();
			writer.println();

			writer.println("%Set stack pointer to 0x4000'0000'0000'0000 - 8");
			writer.println("Main\tPUT rG,252");
			writer.println("\t\tSETH SP,#4000");
			writer.println("\t\tSUB SP,SP,8");
			writer.println("\t\tSETL FP,0");
			writer.println("\t\tPUSHJ $0,_");
			writer.println("\t\tTRAP 0,Halt,0");



			String indent = "\t\t";
			String bufferdLabel = null;
			for (CodeFragment codeFragment : fragInstrs.keySet()) {
				writer.println("");
				InstructionSet instrs = fragInstrs.get(codeFragment);
				for (Instruction instr : instrs.instrs) {
					if(instr instanceof Label){
						if(bufferdLabel != null){
							System.err.println("Buffered label is non-null!");
							//throw new InternalCompilerError();
						}
						bufferdLabel = instr.toString();
					}else if(instr instanceof Comment){
						writer.println(instr);
					}else if (instr instanceof Mnemonic){
						if(bufferdLabel != null){
							writer.print(bufferdLabel);
							writer.print('\t');
							writer.println(instr);
							bufferdLabel = null;
						}else{
							writer.print(indent);
							writer.println(instr);
						}
					}else {
						throw new InternalCompilerError();
					}
				}
				writer.println("");
				writer.println("");
			}

			writer.println("");
			writer.println("");
			writer.println(auxiliary_functions);
			writer.close();

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if(Main.oddaja){
			Scanner sc;
			try {
				sc = new Scanner(new File(task.srcFName + fileEnding));
				while(sc.hasNextLine()){
					System.out.println(sc.nextLine());
				}
				sc.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}


	private final String auxiliary_functions =
						   "\n" +
						   "_printChr\tLDO $1,SP,8\n" +
						   "\t\t\tSET $0,0\n" +
						   "\t\t\tSTO $0,SP,8\n" +
						   "\t\t\tSTO $1,SP,0\n" +
						   "\t\t\tADD $255,SP,7\n" +
						   "\t\t\tTRAP 0,Fputs,StdOut\n" +
						   "\t\t\tPOP 0,0\n" +
						   "\n" +
						   "\t\t\t\n" +
						   "_printStr\tADD $0,SP,8 %get string pointer\n" +
						   "\t\t\tLDO $255,$0,0\n" +
						   "\t\t\tTRAP 0,Fputs,StdOut\n" +
						   "\t\t\tPOP 0,0\n" +
						   "\t\t\t\n" +
						   "\t\t\t\n" +
						   "\t\t\t\n" +
						   "_printInt\tSUBU $1,SP,128 %first set-up the buffer\n" +
						   "\t\t\tSETL $3,0\n" +
						   "\t\t\tSTO $3,$1,0\n" +
						   "\t\t\tLDO $0,SP,8\t%load number into $0\n" +
						   "\t\t\tSETL $2,#20\n" +
						   "\t\t\tSTB $2,$1,0 %set first byte to space\n" +
						   "\t\t\tBNN $0,Pos\n" +
						   "\n" +
						   "Neg\t\t\tSETL $2,#2D\n" +
						   "\t\t\tSTB $2,$1,0 %set first byte to minus\n" +
						   "\t\t\tNEG $0,$0\n" +
						   "\t\t\t%fall trough to positive\n" +
						   "\n" +
						   "Pos\t\t\tADDU $2,$1,16\n" +
						   "\t\t\tADDU $1,$1,1 %for the sign\n" +
						   "Loop_p\t\tSUB $2,$2,1\n" +
						   "\t\t\tDIV $0,$0,10 %divide by 10\n" +
						   "\t\t\tGET $4,rR\n" +
						   "\t\t\tADD $4,$4,#30\n" +
						   "\t\t\tSTB $4,$2,0\n" +
						   "\t\t\tCMP $3,$1,$2\n" +
						   "\t\t\tBNZ $3,Loop_p\n" +
						   "\n" +
						   "\t\t\t%write actual data\n" +
						   "\t\t\tSUBU $255,SP,128\n" +
						   "\t\t\tTRAP 0,Fputs,StdOut\n" +
						   "\t\t\tPOP 0,0\n";
}
