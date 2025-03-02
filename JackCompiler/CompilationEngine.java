import java.io.IOException;
import java.nio.file.Path;

public class CompilationEngine {
	public static final String CONSTANT_SEGMENT = "constant";

	private enum ClassVarDecState {
		START, QUALIFIER, TYPE, IDENTIFIER, CLOSE, TERMINATE;
	}

	private JackTokenizer tknzr;
	private Path sourcePath;
	private SymbolTable classTable;
	private SymbolTable subRoutineTable;
	private String className;
	private static int labelCount = 0;
	VMWriter vmWriter;
	private String subRoutineType = "";

	public CompilationEngine(Path sourcePath) {
		super();
		this.sourcePath = sourcePath;
		this.className = sourcePath.getFileName().toString().split("\\.")[0];
		this.classTable = new SymbolTable();
		this.subRoutineTable = new SymbolTable();
		tknzr = new JackTokenizer(sourcePath);
		vmWriter = new VMWriter(sourcePath.getParent().toString() + "/" + this.className + ".vm");
	}

	public void compile() throws IOException {
		compileClass();
	}

	private void compileClass() throws IOException {
		String token = null;
		String tokenType = null;
		tknzr.advance();
		process("class");
		token = tknzr.token();
		tokenType = tknzr.tokenType();
		if (!tokenType.equals(LexicalElements.IDENTIFIER)) {
			throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + token);
		}
		tknzr.advance();
		process("{");
		compileClassVarDec();
		compileSubroutine();
		process("}");
		vmWriter.close();
	}

	private void compileClassVarDec() {
		ClassVarDecState state = ClassVarDecState.START;
		String token = tknzr.token();
		SymbolTable.Kind kind = SymbolTable.Kind.NONE;
		String type = "";
		String name = "";
		while (true) {
			switch (state) {
			case START:
				if (token.equals(LexicalElements.STATIC) || token.equals(LexicalElements.FIELD)) {
					if (token.equals(LexicalElements.STATIC)) {
						kind = SymbolTable.Kind.STATIC;
					} else {
						kind = SymbolTable.Kind.FIELD;
					}
					printXMLToken(token);
					state = ClassVarDecState.QUALIFIER;
					tknzr.advance();
				} else {
					if (token.equals(LexicalElements.METHOD) || token.equals(LexicalElements.FUNCTION)
							|| token.equals(LexicalElements.CONSTRUCTOR) || token.equals("}")) {
						state = ClassVarDecState.TERMINATE;
					} else {
						throw new IllegalArgumentException(
								"Compilation Error. class var declaration must start with static or field.");
					}

				}
				break;
			case QUALIFIER:
				type = tknzr.token();
				processType();
				state = ClassVarDecState.TYPE;
				break;
			case TYPE:
				token = tknzr.token();
				if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
					throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + token);
				} else {
					name = token;
					classTable.define(name, type, kind);
					printXMLToken(token);
					state = ClassVarDecState.IDENTIFIER;
					tknzr.advance();
				}
				break;
			case IDENTIFIER:
				token = tknzr.token();
				try {
					process(",");
					state = ClassVarDecState.TYPE;
				} catch (Exception ex) {
					if (token.equals(";")) {
						state = ClassVarDecState.CLOSE;
					} else {
						throw new IllegalArgumentException("Compilation Error. Illegal Statement. Missing ;");
					}
				}
				break;
			case CLOSE:
				printXMLToken(token);
				tknzr.advance();
				compileClassVarDec();
				state = ClassVarDecState.TERMINATE;
			case TERMINATE:
				return;
			default:
				throw new IllegalArgumentException("Compilation Error. Illegal Statement");
			}
		}
	}

	private void compileSubroutine() {
		subRoutineTable.reset();
		String subRoutineName = "";
		subRoutineType = tknzr.token();
		if (!subRoutineType.equals(LexicalElements.METHOD) && !subRoutineType.equals(LexicalElements.FUNCTION)
				&& !subRoutineType.equals(LexicalElements.CONSTRUCTOR)) {
			return;
		} else {
			printXMLToken(subRoutineType);
		}
		processReturnType();
		tknzr.advance();
		String token = tknzr.token();
		subRoutineName = this.className + "." + token;
		if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
			throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + token);
		}
		tknzr.advance();
		process("(");
		compileParameterList();
		process(")");
		compileSubroutineBody(subRoutineName);
		compileSubroutine();
	}

	private void compileParameterList() {
		String token = tknzr.token();
		String type = "";
		String name = "";
		if (isMethod()) {
			subRoutineTable.define("this", className, SymbolTable.Kind.ARG);
		}
		while (!token.equals(")")) {
			type = token;
			processType();

			////
			if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
				throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + token);
			} else {
				name = tknzr.token();
				subRoutineTable.define(name, type, SymbolTable.Kind.ARG);
				token = tknzr.token();
				printXMLToken(token);
				tknzr.advance();
				token = tknzr.token();
			}
			///
			try {
				process(",");
			} catch (Exception ex) {
				if (token.equals(")")) {
					continue;
				} else {
					throw new IllegalArgumentException("Compilation Error. Missing ',' separator in parameterList");
				}
			}
			token = tknzr.token();
		}
	}

	private void compileSubroutineBody(String subRoutineName) {

		process("{");
		compileVarDec();
		int varCount = subRoutineTable.varCount(SymbolTable.Kind.VAR);
		;
		vmWriter.writeFunction(subRoutineName, varCount);
		if (subRoutineType.equalsIgnoreCase("constructor")) {
			int fieldCount = classTable.varCount(SymbolTable.Kind.FIELD);
			if (fieldCount > 0) {
				vmWriter.writePush("constant", fieldCount);
				vmWriter.writeCall("Memory.alloc", 1);
				vmWriter.writePop("pointer", 0);
			}
			compileStatements();
		} else if (subRoutineType.equalsIgnoreCase("method")) {
			vmWriter.writePush("argument", 0);
			vmWriter.writePop("pointer", 0);
			compileStatements();
		} else {
			compileStatements();
		}
		process("}");
	}

	private void compileVarDec() {
		String type = "";
		String name = "";
		if (!tknzr.token().equals("var")) {
			return;
		}
		process("var");
		if (!LexicalElements.builtinTypes.contains(tknzr.token())
				&& !tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
			throw new IllegalArgumentException("Compilation Error. Invalid type for subRoutine variable declaration.");
		}
		type = tknzr.token();
		processType();
		String token = tknzr.token();
		while (!token.equals(";")) {
			if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
				throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + token);
			} else {
				name = token;
				subRoutineTable.define(name, type, SymbolTable.Kind.VAR);
				token = tknzr.token();
				tknzr.advance();
				token = tknzr.token();

				try {
					process(",");
				} catch (Exception ex) {
					if (token.equals(";")) {
						continue;
					} else {
						throw new IllegalArgumentException("Compilation Error. Illegal Statement");
					}
				}
				token = tknzr.token();
			}
		}
		printXMLToken(token);
		tknzr.advance();
		compileVarDec();
	}

	private void compileStatements() {
		if (LexicalElements.statementTypes.contains(tknzr.token())) {
			String stmt = tknzr.token();
			switch (stmt) {
			case LexicalElements.LET:
				compileLetStatement();
				break;
			case LexicalElements.DO:
				compileDoStatement();
				break;
			case LexicalElements.RETURN:
				compileReturnStatement();
				break;
			case LexicalElements.WHILE:
				compileWhileStatement();
				break;
			case LexicalElements.IF:
				compileIfStatement();
				break;
			}
		} else {
			return;
		}
		compileStatements();
	}

	private void compileLetStatement() {
		process("let");
		if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
			throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + tknzr.token());
		} else {
			String lhs = tknzr.token();
			tknzr.advance();
			if (tknzr.token().equals("[")) {
				process("[");
				SymbolTable table = null;
				if (subRoutineTable.isPresent(lhs)) {
					table = subRoutineTable;
				} else if (classTable.isPresent(lhs)) {
					table = classTable;
				} else {
					throw new IllegalArgumentException("Invalid symbol " + lhs);
				}
				vmWriter.writePush(table.getSegment(lhs), table.indexOf(lhs));
				compileExpression();
				vmWriter.writeAirthmetic("add");
				process("]");
				process("=");
				compileExpression();
				vmWriter.writePop("temp", 0);
				vmWriter.writePop("pointer", 1);
				vmWriter.writePush("temp", 0);
				vmWriter.writePop("that", 0);

				process(";");
			} else {
				SymbolTable table = null;
				if (subRoutineTable.isPresent(lhs)) {
					table = subRoutineTable;
				} else if (classTable.isPresent(lhs)) {
					table = classTable;
				} else {
					throw new IllegalArgumentException("Invalid symbol " + lhs);
				}
				process("=");
				compileExpression();
				vmWriter.writePop(table.getSegment(lhs), table.indexOf(lhs));
				process(";");
			}
		}

	}

	private void compileDoStatement() {
		process("do");
		compileExpression();
		vmWriter.writePop("temp", 0);
		process(";");
	}

	private void compileReturnStatement() {
		process(LexicalElements.RETURN);
		if (!tknzr.token().equals(";")) {
			compileExpression();
		} else {
			vmWriter.writePush(CONSTANT_SEGMENT, 0);
		}
		if (subRoutineType.equalsIgnoreCase("constructor")) {
			vmWriter.writePush("pointer", 0);
		}
		vmWriter.writeReturn();
		process(";");
	}

	private void compileWhileStatement() {
		String label0 = getLabelName();
		String label1 = getLabelName();

		vmWriter.writeLabel(label0);
		process(LexicalElements.WHILE);
		process("(");
		compileExpression();
		vmWriter.writeAirthmetic("not");
		vmWriter.writeIf(label1);
		process(")");
		process("{");
		compileStatements();
		vmWriter.writeGoto(label0);
		vmWriter.writeLabel(label1);
		process("}");
	}

	private void compileIfStatement() {
		String label0 = getLabelName();
		String label1 = getLabelName();
		process(LexicalElements.IF);
		process("(");
		compileExpression();
		vmWriter.writeAirthmetic("not");
		process(")");
		process("{");
		vmWriter.writeIf(label0);
		compileStatements();
		vmWriter.writeGoto(label1);
		vmWriter.writeLabel(label0);
		process("}");
		// optional elsee part
		if (tknzr.token().equals(LexicalElements.ELSE)) {
			process(LexicalElements.ELSE);
			process("{");
			compileStatements();
			process("}");
		}
		vmWriter.writeLabel(label1);
	}

	private void compileTerm() {
		if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER) && !tknzr.token().equals("(")
				&& !LexicalElements.keyWordConstants.contains(tknzr.token())
				&& tknzr.tokenType() != LexicalElements.INT_CONST && tknzr.tokenType() != LexicalElements.STRING_CONST
				&& !LexicalElements.unaryOps.contains(tknzr.token())) {
			throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + tknzr.token());
		} else {
			if (LexicalElements.unaryOps.contains(tknzr.token())) {
				String op = tknzr.token();
				process(tknzr.token());
				compileTerm();
				vmWriter.writeAirthmetic(getUnaryOpCommand(op));
			} else if (tknzr.tokenType() == LexicalElements.IDENTIFIER) {
				String currentToken = tknzr.token();
				tknzr.advance();
				if (tknzr.token().equals("[")) {
					process("[");
					SymbolTable table = null;
					if (subRoutineTable.isPresent(currentToken)) {
						table = subRoutineTable;
					} else if (classTable.isPresent(currentToken)) {
						table = classTable;
					} else {
						throw new IllegalArgumentException("Invalid symbol: " + currentToken);
					}
					vmWriter.writePush(table.getSegment(currentToken), table.indexOf(currentToken));
					compileExpression();
					vmWriter.writeAirthmetic("add");
					vmWriter.writePop("pointer", 1);
					vmWriter.writePush("that", 0);
					process("]");
				}

				else if (tknzr.token().equals("(")) {
					if (tknzr.token().equals("(")) {
						process("(");
						int nArgs = 0;
						vmWriter.writePush("pointer", 0);
						if (!tknzr.token().equals(")")) {
							nArgs = compileExpressionList(0);
						}
						String subroutineName = className + "." + currentToken;
						vmWriter.writeCall(subroutineName, nArgs + 1);
						process(")");
					}

				} else if (tknzr.token().equals(".")) {
					process(".");
					if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
						throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + tknzr.token());
					} else {
						String methodName = tknzr.token();
						process(tknzr.token());
						process("(");
						int nArgs = 0;
						String subRoutineName = "";
						//
						if (subRoutineTable.isPresent(currentToken)) {
							subRoutineName = subRoutineTable.typeOf(currentToken) + "." + methodName;
							vmWriter.writePush(subRoutineTable.getSegment(currentToken),
									subRoutineTable.indexOf(currentToken));
							++nArgs;
						} else if (classTable.isPresent(currentToken)) {
							subRoutineName = classTable.typeOf(currentToken) + "." + methodName;
							vmWriter.writePush(classTable.getSegment(currentToken), classTable.indexOf(currentToken));
							++nArgs;
						} else {
							subRoutineName = currentToken + "." + methodName;
						}
						//
						if (!tknzr.token().equals(")")) {
							nArgs += compileExpressionList(0);
						}
						vmWriter.writeCall(subRoutineName, nArgs);
						process(")");
					}
				} else {
					SymbolTable table = null;
					if (subRoutineTable.isPresent(currentToken)) {
						table = subRoutineTable;
					} else if (classTable.isPresent(currentToken)) {
						table = classTable;
					} else {
						throw new IllegalArgumentException("Invalid symbol: " + currentToken);
					}
					vmWriter.writePush(table.getSegment(currentToken), table.indexOf(currentToken));
				}
			} else if (tknzr.token().equals("(")) {
				process(tknzr.token());
				compileExpression();
				process(")");
			} else {
				if (tknzr.tokenType().equalsIgnoreCase(LexicalElements.INT_CONST))
					vmWriter.writePush(CONSTANT_SEGMENT, Integer.parseInt(tknzr.token()));
				else if (tknzr.tokenType().equalsIgnoreCase(LexicalElements.STRING_CONST)) {
					vmWriter.writePush(CONSTANT_SEGMENT, tknzr.token().length() - 2);
					vmWriter.writeCall("String.new", 1);
					for (char x : tknzr.token().substring(1, tknzr.token().length() - 1).toCharArray()) {
						vmWriter.writePush(CONSTANT_SEGMENT, (int) x);
						vmWriter.writeCall("String.appendChar", 2);
					}
				} else if (LexicalElements.booleanValues.contains(tknzr.token())) {
					int val = tknzr.token().equalsIgnoreCase(LexicalElements.TRUE) ? 1 : 0;
					vmWriter.writePush(CONSTANT_SEGMENT, val);
					if (val == 1) {
						vmWriter.writeAirthmetic("neg");
					}
				} else if (LexicalElements.NULL.equalsIgnoreCase(tknzr.token())) {
					vmWriter.writePush(CONSTANT_SEGMENT, 0);
				} else if (LexicalElements.THIS.equalsIgnoreCase(tknzr.token())
						&& subRoutineType.equalsIgnoreCase("method")) {
					vmWriter.writePush(subRoutineTable.getSegment(tknzr.token()),
							subRoutineTable.indexOf(tknzr.token()));
				}
				tknzr.advance();
			}
		}
	}

	private void compileExpression() {
		compileTerm();
		String token = tknzr.token();
		if (LexicalElements.Ops.contains(token)) {
			process(tknzr.token());
			compileTerm();
			vmWriter.writeAirthmetic(getOpCommand(token));
		}
	}

	private int compileExpressionList(int count) {
		compileExpression();
		++count;
		if (tknzr.token().equals(",")) {
			process(",");
			count = compileExpressionList(count);
		}
		return count;
	}

	private void process(String token) {
		if (token.equals(tknzr.token())) {
			// printXMLToken(tknzr.token());
			tknzr.advance();
		} else {
			throw new IllegalArgumentException("Compilation Error. Missing Token: " + token);
		}

	}

	private String processType() {
		String token = tknzr.token();
		if (!token.equals(LexicalElements.INT) && !token.equals(LexicalElements.CHAR)
				&& !token.equals(LexicalElements.BOOLEAN) && !tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
			throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + token);
		} else {
			printXMLToken(token);
			tknzr.advance();
		}
		return token;
	}

	private String processReturnType() {
		tknzr.advance();
		String returnType = tknzr.token();
		if (!returnType.equals(LexicalElements.INT) && !returnType.equals(LexicalElements.CHAR)
				&& !returnType.equals(LexicalElements.VOID) && !returnType.equals(LexicalElements.BOOLEAN)
				&& !tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
			throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + returnType);
		} else {
			printXMLToken(returnType);
		}
		return returnType;
	}

	private void printXMLToken(String token) {
		String tokenType = tknzr.tokenType();
		switch (tokenType) {
		case LexicalElements.KEYWORD:
			break;
		case LexicalElements.IDENTIFIER:
			break;
		case LexicalElements.INT_CONST:
			break;
		case LexicalElements.STRING_CONST:
			break;
		case LexicalElements.SYMBOL:
			break;
		}

	}

	private String getOpCommand(String op) {
		switch (op) {
		case "+":
			return "add";
		case "-":
			return "sub";
		case "=":
			return "eq";
		case "<":
			return "lt";
		case ">":
			return "gt";
		case "&":
			return "and";
		case "|":
			return "or";
		case "*":
			return new String("call Math.multiply 2");
		case "/":
			return new String("call Math.divide 2");
		default:
			return null;
		}
	}

	private String getUnaryOpCommand(String op) {
		switch (op) {
		case "-":
			return "neg";
		case "~":
			return "not";
		default:
			return null;
		}
	}

	private String getLabelName() {
		return className + "_" + labelCount++;
	}

	private boolean isMethod() {
		return subRoutineType.equalsIgnoreCase("method");
	}

}
