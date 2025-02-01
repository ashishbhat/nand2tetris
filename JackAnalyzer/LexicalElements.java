import java.util.List;

public final class LexicalElements {
	// The Lexical Elements of Jack Language
	public static final String CLASS = "class";
	public static final String METHOD = "method";
	public static final String CONSTRUCTOR = "constructor";
	public static final String FUNCTION = "function";
	public static final String WHILE = "while";
	public static final String IF = "if";
	public static final String ELSE = "else";
	public static final String DO = "do";
	public static final String RETURN = "return";
	public static final String LET = "let";
	public static final String FIELD = "field";
	public static final String VAR = "var";
	public static final String BOOLEAN = "boolean";
	public static final String CHAR = "char";
	public static final String VOID = "void";
	public static final String INT = "int";
	public static final String NULL = "null";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String THIS = "this";
	public static final String STATIC = "static";

	public static final String KEYWORD = "keyword";
	public static final String SYMBOL = "symbol";
	public static final String INT_CONST = "integerConstant";
	public static final String STRING_CONST = "stringConstant";
	public static final String IDENTIFIER = "identifier";

	public static final String[] KEYWORDS_LIST = { WHILE, IF, ELSE, DO, RETURN, LET, FIELD, VAR, BOOLEAN, CHAR, VOID,
			INT, NULL, CLASS, METHOD, FUNCTION, CONSTRUCTOR, TRUE, FALSE, THIS, STATIC };

	public static final char[] SYMBOLS = { '{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&', '|',
			'<', '>', '=', '~' };

	public static final List<String> builtinTypes = List.of("int", "char", "boolean");
	public static final List<String> subRoutineTypes = List.of("method", "function", "constructor");
	public static final List<String> statementTypes = List.of("if", "while", "do", "let", "return");
	public static final List<String> keyWordConstants = List.of(TRUE, FALSE);
	public static final List<String> unaryOps = List.of("-", "~");
	public static final List<String> Ops = List.of("+", "-", "*", "/", "&", "*", "<", ">", "=");

}
