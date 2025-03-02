import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class JackTokenizer {
	private List<String> tokens = new ArrayList<>();
	private int tokenIndex = -1;

	JackTokenizer(Path sourcePath) {
		getTokens(sourcePath);
	}

	private void getTokens(Path sourcePath) {
		boolean stringMode = false;
		boolean lineCommentMode = false;
		boolean blockCommentMode = false;
		char[] charArray = null;
		try {
			charArray = new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8).toCharArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String token = "";
		for (int i = 0; i < charArray.length; ++i) {
			char c = charArray[i];
			if (!stringMode && c == '/') {
				if (charArray[i + 1] == '/') {
					lineCommentMode = true;
					continue;
				} else if (charArray[i + 1] == '*' && charArray[i + 2] == '*') {
					blockCommentMode = true;
					continue;
				}
			}
			if (lineCommentMode) {
				if (c == '\n') {
					lineCommentMode = false;
					continue;
				}
			}
			if (blockCommentMode) {
				if (charArray[i] == '*' && charArray[i + 1] == '/') {
					blockCommentMode = false;
					i = i + 2;
					continue;
				}
			}
			if (lineCommentMode || blockCommentMode) {
				continue;
			}

			if ((char) c == '"') {
				stringMode = !stringMode;
			}

			if (stringMode) {
				token = token + c;
				continue;
			} else if (isSymbol(c)) {
				if (!token.isBlank()) {
					tokens.add(token);
				}
				tokens.add(Character.toString(c));
				token = "";
				continue;
			} else if (Character.isWhitespace(c)) {
				if (!token.isBlank()) {
					tokens.add(token);
				}
				token = "";
				continue;
			}
			token = token + c;
		}
	}

	private boolean isSymbol(char c) {
		for (char x : LexicalElements.SYMBOLS) {
			if (x == c) {
				return true;
			}
		}
		return false;
	}

	/*-
	public String keyword(String token) {
		if (currentToken.equals(LexicalElements.CLASS))
			return LexicalElements.CLASS;
		else if (currentToken.equals(LexicalElements.METHOD))
			return LexicalElements.METHOD;
		else if (currentToken.equals(LexicalElements.CONSTRUCTOR))
			return LexicalElements.CONSTRUCTOR;
		else if (currentToken.equals(LexicalElements.FUNCTION))
			return LexicalElements.FUNCTION;
		else if (currentToken.equals(LexicalElements.IF))
			return LexicalElements.IF;
		else if (currentToken.equals(LexicalElements.ELSE))
			return LexicalElements.ELSE;
		else if (currentToken.equals(LexicalElements.WHILE))
			return LexicalElements.WHILE;
		else if (currentToken.equals(LexicalElements.DO))
			return LexicalElements.DO;
		else if (currentToken.equals(LexicalElements.VAR))
			return LexicalElements.VAR;
		else if (currentToken.equals(LexicalElements.INT))
			return LexicalElements.INT;
		else if (currentToken.equals(LexicalElements.CHAR))
			return LexicalElements.CHAR;
		else if (currentToken.equals(LexicalElements.CLASS))
			return LexicalElements.CLASS;
		else if (currentToken.equals(LexicalElements.THIS))
			return LexicalElements.THIS;
		else if (currentToken.equals(LexicalElements.TRUE))
			return LexicalElements.TRUE;
		else if (currentToken.equals(LexicalElements.FALSE))
			return LexicalElements.FALSE;
		else if (currentToken.equals(LexicalElements.NULL))
			return LexicalElements.NULL;
		else if (currentToken.equals(LexicalElements.VOID))
			return LexicalElements.VOID;
		else if (currentToken.equals(LexicalElements.RETURN))
			return LexicalElements.RETURN;
		else if (currentToken.equals(LexicalElements.STATIC))
			return LexicalElements.STATIC;
		else if (currentToken.equals(LexicalElements.LET))
			return LexicalElements.LET;
		else if (currentToken.equals(LexicalElements.FIELD))
			return LexicalElements.FIELD;
		else if (currentToken.equals(LexicalElements.BOOLEAN))
			return LexicalElements.BOOLEAN;
		else
			return null;
	}
	*/

	public String symbol(String token) {
		char c = token.charAt(0);
		if (c == '"')
			return "&quot;";
		else if (c == '<')
			return "&lt;";
		else if (c == '>')
			return "&gt;";
		else if (c == '&')
			return "&amp;";
		else
			return String.valueOf(c);
	}

	public String stringVal(String token) {
		return token.substring(1, token.length() - 1);
	}

	public int intVal(String token) {
		return Integer.parseInt(token);
	}

	public String indentifier(String token) {
		return token;
	}

	public boolean hasMoreTokens() {
		if (tokenIndex < tokens.size()) {
			return true;
		} else {
			return false;
		}

	}

	public void advance() {
		++tokenIndex;
	}

	public String token() {
		return tokens.get(tokenIndex);
	}

	public String tokenType() {
		Arrays.sort(LexicalElements.KEYWORDS_LIST);
		if (Arrays.binarySearch(LexicalElements.KEYWORDS_LIST, token()) >= 0) {
			return LexicalElements.KEYWORD;
		} else if (token().length() == 1 && isSymbol(token().charAt(0))) {
			return LexicalElements.SYMBOL;
		} else if (isInteger()) {
			return LexicalElements.INT_CONST;
		} else if (isString()) {
			return LexicalElements.STRING_CONST;
		} else {
			return LexicalElements.IDENTIFIER;
		}

	}

	private boolean isString() {
		return Pattern.matches("\".*\"", token());
	}

	private boolean isInteger() {
		try {
			Integer.parseInt(token());
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
