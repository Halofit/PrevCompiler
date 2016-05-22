package compiler.data.codegen;

import java.util.HashMap;

/**
 * Created by gregor on 20.5.2016.
 */
public class Label extends Instruction {
	public String label;

	private Label(String label){
		this.label = label;
	}

	private static HashMap<String, Label> labels;
	public static Label get(String label){
		Label laObj = labels.get(label);
		if(laObj == null){
			laObj = new Label(label);
			labels.put(label, laObj);
		}
		return laObj;
	}

	@Override
	public String toString() {
		return "    " + label + ": ";
	}
}
