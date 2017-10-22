package Components;

/**
 * A Terminal is any token that falls under the hardcoded
 * types. Terminals are what are returned by the Tokenizer
 * and what we use to find the leaf node of our parse tree.
 */
public class Terminal extends Symbol {
    public enum Type {
        IF,
        ELSE,
        WHILE,
        TRUE,
        FALSE,
        NEGATION,
        OPEN_CURLY,
        CLOSE_CURLY,
        OPEN_PAREN,
        CLOSE_PAREN,
        PRINT,
        SEMICOLON,
        EMPTY,
        END
    }
    private Type type;

    public Terminal(String symbol, Type type) {
        super(symbol);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isTerminal() { return true; }
}
