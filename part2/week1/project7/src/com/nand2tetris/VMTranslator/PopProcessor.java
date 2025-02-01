package com.nand2tetris.VMTranslator;

public class PopProcessor {
	StringBuilder asm;
	private static final String LINE = "\n";

	public PopProcessor(StringBuilder asm) {
		super();
		this.asm = asm;
	}

	void popStatic(VMCommand command) {
		String segmentIndex = command.getArg2();
		String sourceCodeFile = command.getSourcefile();

		asm.append("@SP\n");
		asm.append("M=M-1\n");
		asm.append("A=M\n");
		asm.append("D=M\n");

		String addr = sourceCodeFile + "." + segmentIndex;
		asm.append("@" + addr + LINE);
		asm.append("M=D\n");
	}

	void popPointer(VMCommand command) {
		String pointer = command.getArg2();

		asm.append("@SP\n");
		asm.append("M=M-1\n");
		asm.append("A=M\n");
		asm.append("D=M\n");

		String addr = pointer.equals("0") ? "THIS" : "THAT";
		asm.append("@" + addr + LINE);
		asm.append("M=D\n");
	}

	void popTemp(VMCommand command) {
		String segment = command.getArg1();
		String segmentIndex = command.getArg2();
		String segementPointer = getSegementPointer(segment, segmentIndex);

		asm.append("@SP\n");
		asm.append("M=M-1\n");
		asm.append("A=M\n");
		asm.append("D=M\n");

		int addr = Integer.parseInt(segementPointer) + Integer.parseInt(segmentIndex);
		asm.append("@" + addr + LINE);
		asm.append("M=D\n");
	}

	void popProperSegments(VMCommand command) {
		String segment = command.getArg1();
		String segmentIndex = command.getArg2();
		String segementPointer = getSegementPointer(segment, segmentIndex);
		asm.append("@" + segementPointer + LINE);
		asm.append("D=M\n");
		asm.append("@" + segmentIndex + LINE);
		asm.append("D=D+A\n");
		asm.append("@addr\n");
		asm.append("M=D\n");

		asm.append("@SP\n");
		asm.append("M=M-1\n");
		asm.append("A=M\n");
		asm.append("D=M\n");

		asm.append("@addr\n");
		asm.append("A=M\n");
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
			return "5"; // TEMP BASE ADDR
		default:
			return segment;
		}
	}
}