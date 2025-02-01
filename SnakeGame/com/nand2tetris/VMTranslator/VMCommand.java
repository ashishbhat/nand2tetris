package com.nand2tetris.VMTranslator;

public class VMCommand {
	public enum CommandType {
		AIRTHMETIC, LOGIC, MEMORY
	}

	public enum Command {
		PUSH, POP, ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT;
	}

	private Command command;
	private CommandType commandType;
	private String arg1;
	private String arg2;
	private String sourcefile;

	/**
	 * @return the sourcefile
	 */
	public String getSourcefile() {
		return sourcefile;
	}

	/**
	 * @param sourcefile the sourcefile to set
	 */
	public void setSourcefile(String sourcefile) {
		this.sourcefile = sourcefile;
	}

	/**
	 * @return the name
	 */
	public Command getName() {
		return command;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(Command command) {
		this.command = command;
	}

	/**
	 * @return the commandType
	 */
	public CommandType getCommandType() {
		return commandType;
	}

	/**
	 * @param commandType the commandType to set
	 */
	public void setCommandType(CommandType commandType) {
		this.commandType = commandType;
	}

	/**
	 * @return the arg1
	 */
	public String getArg1() {
		return arg1;
	}

	/**
	 * @param arg1 the arg1 to set
	 */
	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}

	/**
	 * @return the arg2
	 */
	public String getArg2() {
		return arg2;
	}

	/**
	 * @param arg2 the arg2 to set
	 */
	public void setArg2(String arg2) {
		this.arg2 = arg2;
	}

}
