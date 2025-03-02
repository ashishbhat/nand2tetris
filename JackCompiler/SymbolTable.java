import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
	public enum Kind {
		FIELD, STATIC, VAR, ARG, NONE
	}

	public static final String INT = "int";
	public static final String CHAR = "char";
	public static final String BOOLEAN = "boolean";

	static class SymbolData {
		public String type;
		public Kind kind;
		public Integer index;

		public SymbolData(String type, Kind kind, Integer count) {
			super();
			this.type = type;
			this.kind = kind;
			this.index = count;
		}
	}

	private Map<String, SymbolData> symbols;
	private static int staticCount = 0;

	public SymbolTable() {
		symbols = new HashMap<String, SymbolData>();
	}

	void reset() {
		symbols.clear();
	}

	void define(String name, String type, Kind kind) {

		int index = (kind == Kind.STATIC) ? staticCount++ : varCount(kind);
		symbols.put(name, new SymbolData(type, kind, index));
	}

	// returns the number of variables of a given type already defined in the table.
	int varCount(Kind kind) {
		if (kind == Kind.STATIC) {
			return staticCount;
		} else {
			return (int) symbols.entrySet().stream().filter(e -> e.getValue().kind == kind).count();
		}
	}

	Kind kindOf(String name) {
		return symbols.get(name) == null ? Kind.NONE : symbols.get(name).kind;
	}

	String typeOf(String name) {
		return symbols.get(name).type;
	}

	// returns the index of a variable within its scope and kind
	int indexOf(String name) {
		return symbols.get(name) == null ? -1 : symbols.get(name).index;
	}

	public boolean isPresent(String name) {
		return symbols.containsKey(name);
	}

	public String getSegment(String name) {
		Kind kind = symbols.get(name).kind;
		if (kind == Kind.FIELD)
			return "this";
		else if (kind == Kind.STATIC)
			return "static";
		else if (kind == Kind.VAR)
			return "local";
		else if (kind == Kind.ARG)
			return "argument";
		else
			return null;
	}
}
