package compiler.data.codegen;

import java.util.HashMap;

/**
 * Created by gregor on 20.5.2016.
 */
public class VirtualRegister extends Register {
	public long name;

	private VirtualRegister() {
		this.name = genRegName();
	}

	public static VirtualRegister create() {
		return new VirtualRegister();
	}

	//For preexisting temporaries, we need to make sure they end up in the same register
	public static VirtualRegister create(int temporary) {
		VirtualRegister reg = tempMap.get(temporary);
		if (reg == null) {
			reg = new VirtualRegister();
			tempMap.put(temporary, reg);
		}
		return reg;
	}

	private static long nameGenerator = 0;

	public static long genRegName() {
		return nameGenerator++;
	}

	public static HashMap<Integer, VirtualRegister> tempMap = new HashMap<>();

	@Override
	public String toString() {
		return "r"+name;
	}
}
