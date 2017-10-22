package Components;

/**
 * A NonTerminal is a type of Symbol that is unique
 * from a terminal in that it can have a start and nullable
 * state. NonTerminals are what the parser will parse through to
 * validate the tokenized terminals. Contains hardcoded types.
 */
public class NonTerminal extends Symbol {
    public enum Type {
        STATEMENT,
        L,
        CONDITIONAL,
        EMPTY
    }

    private Type type;
    public boolean isNullable = false;
    public boolean isStart = false;
    public NonTerminal(String symbol, Type type) {
        super(symbol);
        this.type = type;
        isNullable = type == Type.EMPTY;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isTerminal() {
        return false;
    }
}
