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
		}

		PrintWriter writer;
		try {
			writer = new PrintWriter(task.srcFName + fileEnding, "US-ASCII");

			//Header
			writer.println();
			writer.println();
			writer.println("\t\tLOC	Data_Segment");

			//define the common registers
			writer.println("FP IS $253"); //No indentation, FP mus be a label
			writer.println("SP IS $254");
			writer.println("RV IS $0");
			writer.println("COLORS IS $" + RegisterAlloc.physicalRegisters);
			writer.println();
			writer.println("\t\tGREG @"); //Set the two registers as global SP,FP
			writer.println("\t\tGREG @");
			writer.println();
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

			String indent = "\t\t";
			String bufferdLabel = null;
			for (CodeFragment codeFragment : fragInstrs.keySet()) {
				writer.println("");
				InstructionSet instrs = fragInstrs.get(codeFragment);
				for (Instruction instr : instrs.instrs) {
					if(instr instanceof Label){
						if(bufferdLabel != null){
							System.err.println("Buffered label is non-null!");
							throw new InternalCompilerError();
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
						   "\n"+
						   "_printStr\tADD $0,SP,8\n"+
						   "\tLDO $255,$0,0\n"+
						   "\tTRAP 0,Fputs,StdOut\n"+
						   "\tPOP 0,0\n"+
						   "\n"+
						   "\n"+
						   "\n"+
						   "_printChr\t LDO $0,SP,8\n"+
						   "\tSTB $0,SP,1\n"+
						   "\tADD $255,SP,8\n"+
						   "\tTRAP 0,Fputs,StdOut\n"+
						   "\tPOP 0,0\n"+
						   "\n"+
						   "\n"+
						   "\n"+
						   "_printInt\tADD $0,SP,8\n"+
						   "\tLDO $0,$0,0\n"+
						   "\tGET $3,rJ\n"+
						   "\tSETL $2,1 # Base divider\n"+
						   "\t\n"+
						   "\t# check if it's minus\n"+
						   "\tCMP $1,$0,0\n"+
						   "\tBNN $1,_printInt_radix\n"+
						   "\t\n"+
						   "\t# make it positive\n"+
						   "\tNEG $0,$0\n"+
						   "\tSETL $1,45\n"+
						   "\tSTO $1,SP,8\n"+
						   "\tPUSHJ $64,_printChr\n"+
						   "\t\n"+
						   "\t# Calculate largest divider with base 10\n"+
						   "_printInt_radix\tCMP $1,$2,$0\n"+
						   "\tBP $1,_printInt_print_start\n"+
						   "\tMUL $2,$2,10\n"+
						   "\tJMP _printInt_radix\n"+
						   "_printInt_print_start DIV $2,$2,10\n"+
						   "_printInt_print CMP $1,$2,0\n"+
						   "\tBNP $1,_printInt_end\n"+
						   "\tDIV $0,$0,$2\n"+
						   "\tADD $0,$0,48 # Convert number to ascii number and print\n"+
						   "\tSTO $0,SP,8\n"+
						   "\tPUSHJ $64,_printChr\n"+
						   "\tGET $0,rR\n"+
						   "\tDIV $2,$2,10\n"+
						   "\tJMP _printInt_print\n"+
						   "_printInt_end PUT rJ,$3\n"+
						   "\tPOP 0,0";
}
