package compiler.data.codegen;

/**
 * Created by gregor on 20.5.2016.
 */
public class VirtualRegister extends Operand {
	public long name;

	public VirtualRegister() {
		this.name = genRegName();
	}

	public VirtualRegister(long name) {
		this.name = name;
	}

	private static long nameGenerator = 0;
	public static long genRegName(){
		return nameGenerator++;
	}
}
