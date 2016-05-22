package compiler.data.codegen;

/**
 * Created by gregor on 21. 05. 2016.
 */
public class Comment extends Instruction {

	public String content;

	public Comment(String content) {
		this.content = content;
	}

	public boolean startsWith(String prefix){
		return (content != null && content.startsWith(prefix));
	}

	@Override
	public String toString() {
		if(content == null){
			return "";
		}else{
			return "%" + content;
		}
	}
}
