import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CodeWriter {
	private String outFile;
	private static final String stackPointer = "SP";
	private static final int TEMP_BASE_ADDR = 5;
	private static final int THIS = 3;
	private static final int THAT = 4;
	private StringBuilder asm = new StringBuilder();
	private int counter = 0;

	private static final String LINE = "\n";
	private static String currentFunction = "";

	private enum Segement {
		LCL, ARG, THIS, THAT, STATIC, POINTER, TEMP, CONSTANT;
	}

	public CodeWriter(String outFile, boolean isDirectory) {
		super();
		this.outFile = outFile;
		if (isDirectory) {
			asm.append("@261\n");
			asm.append("D=A\n");
			asm.append("@SP\n");
			asm.append("M=D\n");

			asm.append("@261\n");
			asm.append("D=A\n");
			asm.append("@LCL\n");
			asm.append("M=D\n");

			asm.append("@256\n");
			asm.append("D=A\n");
			asm.append("@ARG\n");
			asm.append("M=D\n");

			asm.append("@1000\n");
			asm.append("D=A\n");
			asm.append("@THIS\n");
			asm.append("M=D\n");

			asm.append("@1500\n");
			asm.append("D=A\n");
			asm.append("@THAT\n");
			asm.append("M=D\n");

			asm.append("@Sys.init\n");
			asm.append("0;JEQ\n");
		}

	}

	public void translate(List<VMCommand> commands) {
		for (VMCommand vmCommand : commands) {
			if (vmCommand.getName() == VMCommand.Command.PUSH) {
				generatePushCommand(vmCommand);
			} else if (vmCommand.getName() == VMCommand.Command.POP) {
				generatePopCommand(vmCommand);
			} else if (vmCommand.getName() == VMCommand.Command.NEG) {
				generateNegateCommand();
			} else if (vmCommand.getName() == VMCommand.Command.ADD) {
				generateAddCommand();
			} else if (vmCommand.getName() == VMCommand.Command.SUB) {
				generateSubCommand();
			} else if (vmCommand.getName() == VMCommand.Command.AND) {
				generateAndCommand();
			} else if (vmCommand.getName() == VMCommand.Command.OR) {
				generateOrCommand();
			} else if (vmCommand.getName() == VMCommand.Command.NOT) {
				generateNotCommand();
			} else if (vmCommand.getName() == VMCommand.Command.EQ) {
				generateEqCommand();
			} else if (vmCommand.getName() == VMCommand.Command.LT) {
				generateLtCommand();
			} else if (vmCommand.getName() == VMCommand.Command.GT) {
				generateGtCommand();
			} else if (vmCommand.getName() == VMCommand.Command.LABEL) {
				generateLabelCommand(vmCommand);
			} else if (vmCommand.getName() == VMCommand.Command.IF_GOTO) {
				generateIfGotoCommand(vmCommand);
			} else if (vmCommand.getName() == VMCommand.Command.GOTO) {
				generateGotoCommand(vmCommand);
			} else if (vmCommand.getName() == VMCommand.Command.FUNCTION) {
				generateFunctionCommand(vmCommand);
			} else if (vmCommand.getCommandType() == VMCommand.CommandType.FUNCTION) {
				generateFunctionCommand(vmCommand);
			}
		}
		generateNoop();
		try {
			FileWriter f = new FileWriter(outFile);
			f.write(asm.toString());
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generateFunctionCommand(VMCommand vmCommand) {
		FunctionProcessor fp = new FunctionProcessor(asm);
		VMCommand.Command command = vmCommand.getName();
		switch (command) {
		case FUNCTION:
			currentFunction = vmCommand.getArg1();
			fp.processFunction(vmCommand);
			break;
		case RETURN:
			fp.processReturn(vmCommand, currentFunction);
			break;
		case CALL:
			fp.processFunctionCall(vmCommand, currentFunction);
			break;
		default:
			break;

		}
	}

	private void generateGotoCommand(VMCommand vmCommand) {
		BranchingProcessor branchingProcessor = new BranchingProcessor(asm);
		branchingProcessor.processGoto(vmCommand);
	}

	private void generateIfGotoCommand(VMCommand vmCommand) {
		BranchingProcessor branchingProcessor = new BranchingProcessor(asm);
		branchingProcessor.processIfGoto(vmCommand);
	}

	private void generateLabelCommand(VMCommand vmCommand) {
		BranchingProcessor branchingProcessor = new BranchingProcessor(asm);
		branchingProcessor.processLabel(vmCommand);
	}

	private void generateGtCommand() {
		int labelCount = getLabelCount();
		String LABELNAME = String.format("LABEL%s", labelCount);
		String LABEl = String.format("(%s)\n", LABELNAME);
		String DONELABELNAME = String.format("DONE%s", labelCount);
		String DONELABEL = String.format("(%s)\n", DONELABELNAME);
		decrementStackPointer();
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("A=A-1\n");
		asm.append("D=D-M\n");
		asm.append("M=0\n");
		asm.append("@" + LABELNAME + LINE);
		asm.append("D;JLT\n");
		asm.append("@" + DONELABELNAME + LINE);
		asm.append("0;JEQ\n");
		asm.append(LABEl);
		asm.append("@SP\n");
		asm.append("A=M-1\n");
		asm.append("M=-1\n");
		asm.append(DONELABEL);
	}

	private void generateLtCommand() {
		int labelCount = getLabelCount();
		String LABELNAME = String.format("LABEL%s", labelCount);
		String LABEl = String.format("(%s)\n", LABELNAME);
		String DONELABELNAME = String.format("DONE%s", labelCount);
		String DONELABEL = String.format("(%s)\n", DONELABELNAME);
		decrementStackPointer();
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("A=A-1\n");
		asm.append("D=D-M\n");
		asm.append("M=0\n");
		asm.append("@" + LABELNAME + LINE);
		asm.append("D;JGT\n");
		asm.append("@" + DONELABELNAME + LINE);
		asm.append("0;JEQ\n");
		asm.append(LABEl);
		asm.append("@SP\n");
		asm.append("A=M-1\n");
		asm.append("M=-1\n");
		asm.append(DONELABEL);
	}

	private void generateEqCommand() {
		int labelCount = getLabelCount();
		String LABELNAME = String.format("LABEL%s", labelCount);
		String LABEl = String.format("(%s)\n", LABELNAME);
		String DONELABELNAME = String.format("DONE%s", labelCount);
		String DONELABEL = String.format("(%s)\n", DONELABELNAME);
		decrementStackPointer();
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("A=A-1\n");
		asm.append("D=D-M\n");
		asm.append("M=0\n");
		asm.append("@" + LABELNAME + LINE);
		asm.append("D;JEQ\n");
		asm.append("@" + DONELABELNAME + LINE);
		asm.append("0;JEQ\n");
		asm.append(LABEl);
		asm.append("@SP\n");
		asm.append("A=M-1\n");
		asm.append("M=-1\n");
		asm.append(DONELABEL);
	}

	private void generateNotCommand() {
		asm.append("@" + stackPointer + LINE);
		asm.append("A=M-1\n");
		asm.append("M=!M\n");
	}

	private void generateOrCommand() {
		decrementStackPointer();
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("@" + stackPointer + "\n");
		asm.append("A=M-1\n");
		asm.append("D=D|M\n");
		asm.append("M=D\n");
	}

	private void generateAndCommand() {
		decrementStackPointer();
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("@" + stackPointer + "\n");
		asm.append("A=M-1\n");
		asm.append("D=D&M\n");
		asm.append("M=D\n");
	}

	private void generateSubCommand() {
		decrementStackPointer();
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("@" + stackPointer + "\n");
		asm.append("A=M-1\n");
		asm.append("D=D-M\n");
		asm.append("D=-D\n");
		asm.append("M=D\n");
	}

	private void generateAddCommand() {
		decrementStackPointer();
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("@" + stackPointer + "\n");
		asm.append("A=M-1\n");
		asm.append("D=D+M\n");
		asm.append("M=D\n");
	}

	private void generateNegateCommand() {
		asm.append("@" + stackPointer + LINE);
		asm.append("A=M-1\n");
		asm.append("M=-M\n");
	}

	private void generatePopCommand(VMCommand vmCommand) {
		PopProcessor popProcessor = new PopProcessor(asm);
		String segment = vmCommand.getArg1();
		switch (segment) {
		case "local":
		case "argument":
		case "this":
		case "that":
			popProcessor.popProperSegments(vmCommand);
			break;
		case "temp":
			popProcessor.popTemp(vmCommand);
			break;
		case "static":
			popProcessor.popStatic(vmCommand);
			break;
		case "pointer":
			popProcessor.popPointer(vmCommand);
			break;
		default:

		}

//	String segmentPointer = "";
//	String segmentIndex = vmCommand.getArg2();
//	boolean isStatic = vmCommand.getArg1().equals("static");
//	boolean isPointer = vmCommand.getArg1().equals("pointer");if(vmCommand.getArg1().equals("pointer"))
//	{
//		segmentPointer = vmCommand.getArg2().equals("0") ? "THIS" : "THAT";
//	}else
//	{
//		segmentPointer = getSegementPointer(vmCommand.getArg1(), segmentIndex);
//	}
//
//	if(!isStatic)
//	{
//		calculateSegmentAddr(vmCommand, segmentPointer, segmentIndex);
//	}
//
//	decrementStackPointer();
//
//	String variableName = getVariableName(vmCommand.getSourcefile(), segmentIndex, isStatic);
//
//	// read from stack and store in segment
//	asm.append("A=M\n");asm.append("D=M\n");if(isPointer)
//	{
//		asm.append("@" + segmentPointer + LINE);
//	}else
//	{
//		asm.append("@" + variableName + LINE);
//	}if(!isStatic&&!isPointer)
//	{
//		asm.append("A=M\n");
//	}asm.append("M=D\n");
	}

	private void generatePushCommand(VMCommand vmCommand) {
		PushProcessor pushProcessor = new PushProcessor(asm);
		String segmentName = vmCommand.getArg1();
		switch (segmentName) {
		case "local":
		case "argument":
		case "this":
		case "that":
		case "temp":
			pushProcessor.pushProperSegments(vmCommand);
			break;
		case "constant":
			pushProcessor.pushConstant(vmCommand);
			break;
		case "static":
			pushProcessor.pushStatic(vmCommand);
			break;
		case "pointer":
			pushProcessor.pushPointer(vmCommand);
			break;
		default:

		}
	}

	private void generateNoop() {
		asm.append("(STOP)\n");
		asm.append("@STOP\n");
		asm.append("0;JEQ\n");
	}

	private void decrementStackPointer() {
		// decrement the stack pointer
		asm.append("@" + stackPointer + "\n");
		asm.append("M=M-1\n");
	}

	private void calculateSegmentAddr(VMCommand vmCommand, String segmentPointer, String segmentIndex) {
		String indexVariable = "";
		boolean isStatic = vmCommand.getArg1().equals("static");
		boolean isTemp = vmCommand.getArg1().equals("temp");
		indexVariable = getVariableName(vmCommand.getSourcefile(), vmCommand.getArg2(), isStatic);
		asm.append("@" + segmentPointer + LINE);
		if (isTemp) {
			asm.append("D=A\n");
		} else {
			asm.append("D=M\n");
		}
		asm.append("@" + segmentIndex + LINE);
		asm.append("D=D+A\n");
		asm.append("@" + indexVariable + LINE);
		asm.append("M=D\n");
	}

	private String getSegementPointer(String segment, String segmentIndex) {
		switch (segment) {
		case "argument":
			return "ARG";
		case "local":
			return "LCL";
		case "this":
			return "THIS";
		case "that":
			return "THAT";
		case "temp":
			Integer tempIndex = TEMP_BASE_ADDR;
			return tempIndex.toString();
		default:
			return segment;
		}
	}

	private String getVariableName(String file, String index, boolean isStatic) {
		return isStatic ? (file + "." + index) : "addr";
	}

	private int getLabelCount() {
		++counter;
		return counter;
	}
}
