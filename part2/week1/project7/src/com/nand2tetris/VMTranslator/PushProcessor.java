package com.nand2tetris.VMTranslator;

public class PushProcessor {
	StringBuilder asm;
	private static final String LINE = "\n";
	private static final String THIS = "3";
	private static final String THAT = "4";

	public PushProcessor(StringBuilder asm) {
		super();
		this.asm = asm;
	}

	void pushConstant(VMCommand command) {
		String constVal = command.getArg2();
		asm.append("@" + constVal + LINE);
		asm.append("D=A\n");
		asm.append("@SP\n");
		asm.append("A=M\n");
		asm.append("M=D\n");
		incrementStackPointer();
		return;
	}

	void pushStatic(VMCommand command) {
		String index = command.getArg2();
		String staticVariable = getVariableName(command.getSourcefile(), index, true);
		asm.append("@" + staticVariable + LINE);
		asm.append("D=M\n");
		asm.append("@SP\n");
		asm.append("A=M\n");
		asm.append("M=D\n");
		incrementStackPointer();
	}

	void pushPointer(VMCommand command) {
		String index = command.getArg2();
		String segmentBasePointer = index.equals("0") ? "THIS" : "THAT";
		asm.append("@" + segmentBasePointer + LINE);
		asm.append("D=M\n");
		asm.append("@SP\n");
		asm.append("A=M\n");
		asm.append("M=D\n");
		incrementStackPointer();
	}

	void pushProperSegments(VMCommand command) {
		String index = command.getArg2();
		String segmentBasePointer = getSegementPointer(command.getArg1(), index);
		calculateSegmentAddr(command, segmentBasePointer, index);
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("@SP\n");
		asm.append("A=M\n");
		asm.append("M=D\n");
		incrementStackPointer();
	}

	private void incrementStackPointer() {
		// increment the stack pointer
		asm.append("@SP\n");
		asm.append("M=M+1\n");
	}

	private void calculateSegmentAddr(VMCommand vmCommand, String segmentPointer, String segmentIndex) {
		String indexVariable = "";
		boolean isTemp = vmCommand.getArg1().equals("temp");
		indexVariable = getVariableName(vmCommand.getSourcefile(), vmCommand.getArg2(), false);
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
			return "5"; // TEMP BASE ADDR
		default:
			return segment;
		}
	}

	private String getVariableName(String file, String index, boolean isStatic) {
		return isStatic ? (file + "." + index) : "addr";
	}
}
