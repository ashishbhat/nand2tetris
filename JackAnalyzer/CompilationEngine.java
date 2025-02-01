import java.nio.file.Path;

public class CompilationEngine {
	private enum ClassVarDecState {
		START, QUALIFIER, TYPE, IDENTIFIER, CLOSE, TERMINATE;
	}

	private JackTokenizer tknzr;
	private Path sourcePath;
	private StringBuilder compiledCode = new StringBuilder();

	public CompilationEngine(Path sourcePath) {
		super();
		this.sourcePath = sourcePath;
		tknzr = new JackTokenizer(sourcePath);
	}

	public void compile() {
		compileClass();
		System.out.print(compiledCode);
	}

	private void compileClass() {
		String token = null;
		String tokenType = null;
		compiledCode.append("<class>\n");
		tknzr.advance();
		process("class");
		token = tknzr.token();
		tokenType = tknzr.tokenType();
		if (tokenType.equals(LexicalElements.IDENTIFIER)) {
			printXMLToken(token);
		} else {
			throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + token);
		}
		tknzr.advance();
		process("{");
		compileClassVarDec();
		compileSubroutine();
		compiledCode.append("</class>\n");
	}

	private void compileClassVarDec() {
		ClassVarDecState state = ClassVarDecState.START;
		String token = tknzr.token();
		while (true) {
			switch (state) {
			case START:
				if (token.equals(LexicalElements.STATIC) || token.equals(LexicalElements.FIELD)) {
					compiledCode.append("<classVarDec>\n");
					printXMLToken(token);
					state = ClassVarDecState.QUALIFIER;
					tknzr.advance();
				} else {
					if (token.equals(LexicalElements.METHOD) || token.equals("}")) {
						state = ClassVarDecState.TERMINATE;
					} else {
						throw new IllegalArgumentException(
								"Compilation Error. class var declaration must start with static or field.");
					}

				}
				break;
			case QUALIFIER:
				processType();
				state = ClassVarDecState.TYPE;
				break;
			case TYPE:
				token = tknzr.token();
				if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
					throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + token);
				} else {
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
				compiledCode.append("</classVarDec>\n");
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
		String token = tknzr.token();
		if (!token.equals(LexicalElements.METHOD) && !token.equals(LexicalElements.FUNCTION)
				&& !token.equals(LexicalElements.CONSTRUCTOR)) {
			return;
		} else {
			compiledCode.append("<subroutineDec>\n");
			printXMLToken(token);
		}
		processReturnType();
		tknzr.advance();
		token = tknzr.token();
		if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
			throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + token);
		} else {
			printXMLToken(token);
		}
		tknzr.advance();
		process("(");
		compiledCode.append("<parameterList>\n");
		compileParameterList();
		compiledCode.append("</parameterList>\n");
		process(")");
		compileSubroutineBody();
		compiledCode.append("</subroutineDec>\n");
		compileSubroutine();
	}

	private void compileParameterList() {
		String token = tknzr.token();
		while (!token.equals(")")) {
			processType();

			////
			if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
				throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + token);
			} else {
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

	private void compileSubroutineBody() {
		compiledCode.append("<subRoutineBody>\n");

		process("{");
		compileVarDec();
		compiledCode.append("<statements>\n");
		compileStatements();
		compiledCode.append("</statements>\n");
		process("}");

		compiledCode.append("</subRoutineBody>\n");

	}

	private void compileVarDec() {
		try {
			if (LexicalElements.builtinTypes.contains(tknzr.token())
					|| tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
				compiledCode.append("<varDec>\n");
			}
			processType();
		} catch (Exception ex) {
			return;
		}
		String token = tknzr.token();
		while (!token.equals(";")) {
			if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
				throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + token);
			} else {
				token = tknzr.token();
				printXMLToken(token);
				tknzr.advance();
				token = tknzr.token();
			}
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
		printXMLToken(token);
		compiledCode.append("</varDec>\n");
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
		compiledCode.append("<letStatement>\n");
		process("let");
		if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
			throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + tknzr.token());
		} else {
			printXMLToken(tknzr.token());
			tknzr.advance();
		}
		process("=");
		compiledCode.append("<expression>\n");
		compileExpression();
		compiledCode.append("</expression>\n");
		process(";");
		compiledCode.append("</letStatement>\n");

	}

	private void compileDoStatement() {
		compiledCode.append("<doStatement>\n");
		process("do");
		if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
			throw new IllegalArgumentException("Compilation Error. Invalid do statement: " + tknzr.token());
		} else {
			process(tknzr.token());
			if (tknzr.token().equals("(")) {
				if (tknzr.token().equals("(")) {
					process("(");
					compiledCode.append("</expressionList>\n");
					compileExpressionList();
					compiledCode.append("</expressionList>\n");
					process(")");
					process(";");
				}

			} else if (tknzr.token().equals(".")) {
				process(".");
				if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
					throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + tknzr.token());
				} else {
					process(tknzr.token());
					process("(");
					compiledCode.append("<expressionList>\n");
					compileExpressionList();
					compiledCode.append("</expressionList>\n");
					process(")");
					process(";");
				}
			}
		}
		compiledCode.append("</doStatement>\n");
	}

	private void compileReturnStatement() {
		compiledCode.append("<returnStatement>\n");
		process(LexicalElements.RETURN);
		if (!tknzr.token().equals(";")) {
			compiledCode.append("<expression>\n");
			compileExpression();
			compiledCode.append("</expression>\n");
		}
		process(";");
		compiledCode.append("</returnStatement>\n");
	}

	private void compileWhileStatement() {
		compiledCode.append("<whileStatement>\n");
		process(LexicalElements.WHILE);
		process("(");
		compiledCode.append("<expression>\n");
		compileExpression();
		compiledCode.append("</expression>\n");
		process(")");
		process("{");
		compiledCode.append("<statements>\n");
		compileStatements();
		compiledCode.append("</statements>\n");
		process("}");
		compiledCode.append("<whileStatement>\n");
	}

	private void compileIfStatement() {
		compiledCode.append("<ifStatement>\n");
		process(LexicalElements.IF);
		process("(");
		compiledCode.append("<expression>\n");
		compileExpression();
		compiledCode.append("</expression>\n");
		process(")");
		process("{");
		compiledCode.append("<statements>\n");
		compileStatements();
		compiledCode.append("</statements>\n");
		process("}");
		// optional elsee part
		if (tknzr.token().equals(LexicalElements.ELSE)) {
			process(LexicalElements.ELSE);
			process("{");
			compiledCode.append("<statements>\n");
			compileStatements();
			compiledCode.append("</statements>\n");
			process("}");
		}
		compiledCode.append("<ifStatement>\n");
	}

	private void compileTerm() {
		if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER) && !tknzr.token().equals("(")
				&& !LexicalElements.keyWordConstants.contains(tknzr.token())
				&& tknzr.tokenType() != LexicalElements.INT_CONST && tknzr.tokenType() != LexicalElements.STRING_CONST
				&& !LexicalElements.unaryOps.contains(tknzr.token())) {
			throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + tknzr.token());
		} else {
			compiledCode.append("<term>\n");
			if (LexicalElements.unaryOps.contains(tknzr.token())) {
				process(tknzr.token());
				compileTerm();
			} else if (tknzr.tokenType() == LexicalElements.IDENTIFIER) {
				String currentToken = tknzr.token();
				printXMLToken(currentToken);
				tknzr.advance();
				if (tknzr.token().equals("[")) {
					process("[");
					compiledCode.append("<expression>\n");
					compileExpression();
					compiledCode.append("</expression>\n");
					process("]");
				}

				else if (tknzr.token().equals("(")) {
					if (tknzr.token().equals("(")) {
						process("(");
						compiledCode.append("</expressionList>\n");
						compileExpressionList();
						compiledCode.append("</expressionList>\n");
						process(")");
					}

				} else if (tknzr.token().equals(".")) {
					process(".");
					if (!tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
						throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + tknzr.token());
					} else {
						process(tknzr.token());
						process("(");
						compiledCode.append("<expressionList>\n");
						compileExpressionList();
						compiledCode.append("</expressionList>\n");
						process(")");
					}
				}
			} else if (tknzr.token().equals("(")) {
				process(tknzr.token());
				compileExpression();
				process(")");
			} else {
				printXMLToken(tknzr.token());
				tknzr.advance();
			}
			compiledCode.append("</term>\n");
		}
	}

	private void compileExpression() {
		compileTerm();
		if (LexicalElements.Ops.contains(tknzr.token())) {
			process(tknzr.token());
			compileTerm();
		}
	}

	private void compileExpressionList() {
		compiledCode.append("<expression>\n");
		compileExpression();
		compiledCode.append("</expression>\n");
		if (tknzr.token().equals(",")) {
			process(",");
			compileExpressionList();
		}
	}

	private void process(String token) {
		if (token.equals(tknzr.token())) {
			printXMLToken(tknzr.token());
			tknzr.advance();
		} else {
			throw new IllegalArgumentException("Compilation Error. Missing Token: " + token);
		}

	}

	private void processType() {
		String token = tknzr.token();
		if (!token.equals(LexicalElements.INT) && !token.equals(LexicalElements.CHAR)
				&& !token.equals(LexicalElements.BOOLEAN) && !tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
			throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + token);
		} else {
			printXMLToken(token);
			tknzr.advance();
		}
	}

	private void processReturnType() {
		tknzr.advance();
		String token = tknzr.token();
		if (!token.equals(LexicalElements.INT) && !token.equals(LexicalElements.CHAR)
				&& !token.equals(LexicalElements.VOID) && !token.equals(LexicalElements.BOOLEAN)
				&& !tknzr.tokenType().equals(LexicalElements.IDENTIFIER)) {
			throw new IllegalArgumentException("Compilation Error. Unexpected Token: " + token);
		} else {
			printXMLToken(token);
		}
	}

	private void printXMLToken(String token) {
		String tokenType = tknzr.tokenType();
		switch (tokenType) {
		case LexicalElements.KEYWORD:
			compiledCode.append("<keyword> " + token + " </keyword>\n");
			break;
		case LexicalElements.IDENTIFIER:
			compiledCode.append("<identifier> " + token + " </identifier>\n");
			break;
		case LexicalElements.INT_CONST:
			compiledCode.append("<integerConstant> " + token + " </integerConstant>\n");
			break;
		case LexicalElements.STRING_CONST:
			compiledCode.append("<stringConstant> " + token + " </stringConstant>\n");
			break;
		case LexicalElements.SYMBOL:
			compiledCode.append("<symbol> " + token + " </symbol>\n");
			break;
		}

	}

}
