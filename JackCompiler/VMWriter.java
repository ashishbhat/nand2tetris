import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
	private String className;

	private StringBuilder vmCode = new StringBuilder();
	private final String ENDLINE = "\n";

	/**
	 * @param className
	 * @param vmCode
	 */
	public VMWriter(String className) {
		super();
		this.className = className;
	}

	public void writeFunction(String name, int nVars) {
		vmCode.append("function " + name + " " + nVars);
		vmCode.append(ENDLINE);
	}

	public void writeReturn() {
		vmCode.append("return");
		vmCode.append(ENDLINE);
	}

	public void writeCall(String name, int nArgs) {
		vmCode.append("call " + name + " " + nArgs);
		vmCode.append(ENDLINE);
	}

	public void writePush(String segment, int index) {
		vmCode.append("push " + segment + " " + index);
		vmCode.append(ENDLINE);
	}

	public void writePop(String segment, int index) {
		vmCode.append("pop " + segment + " " + index);
		vmCode.append(ENDLINE);
	}

	public void writeAirthmetic(String command) {
		vmCode.append(command);
		vmCode.append(ENDLINE);
	}

	public void writeLabel(String labelName) {
		vmCode.append("label " + labelName);
		vmCode.append(ENDLINE);
	}

	public void writeIf(String labelName) {
		vmCode.append("if-goto " + labelName);
		vmCode.append(ENDLINE);
	}

	public void writeGoto(String labelName) {
		vmCode.append("goto " + labelName);
		vmCode.append(ENDLINE);
	}

	public void close() throws IOException {
		System.out.println(vmCode.toString());
		FileWriter f = new FileWriter(className);
		f.write(vmCode.toString());
		f.close();
	}

}
