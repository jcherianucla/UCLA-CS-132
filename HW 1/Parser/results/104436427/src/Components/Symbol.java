package Components;

/**
 * Symbol represents any symbol a parser can read.
 * There are two types of Symbol: Terminal and NonTerminal
 * All comparisons are based solely on the underlying string symbol.
 */
public abstract class Symbol {
    protected String symbol;

    public Symbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public abstract boolean isTerminal();

    @Override
    public boolean equals(Object o) {
        if(o instanceof Symbol) {
            return this.symbol != null && this.symbol.equals(((Symbol) o).symbol);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        return prime + (this.symbol == null ? 0 : this.symbol.hashCode());
    }
}
