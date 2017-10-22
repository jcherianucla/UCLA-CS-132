package Components;

import java.util.*;

/**
 * Production represents a production for the non terminal. This
 * includes a set of terminals and nonterminals.
 */
public class Production {
    private List<Symbol> symbols;

    /**
     * Will create a Production object from string representation. Uses a single whitespace
     * as a delimiter to distinguish between NonTerminal and Terminal.
     * @param strProduction: String representation of Production
     */
    public Production(String strProduction) {
        this.symbols = new ArrayList<>();
        List<String> prodBreak = new ArrayList<>(Arrays.asList(strProduction.split("\\s+")));
        for(String symbol : prodBreak) {
            this.symbols.add(Grammar.terminalToType.get(symbol) != null ?
                    new Terminal(symbol, Grammar.terminalToType.get(symbol)) :
                    new NonTerminal(symbol, Grammar.nonterminalToType.get(symbol)));
        }
    }

    public Production() {
        this.symbols = new ArrayList<>();
    }

    /**
     * Finds if an occurrence of nullable symbols within a production exists.
     * @return: boolean that represents the presence of a nullable symbol
     */
    public boolean hasNullable() {
        for(Symbol symbol : symbols) {
            if(!symbol.isTerminal() && ((NonTerminal)symbol).isNullable) {
                return true;
            }
        }
        return false;
    }

    public List<Symbol> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<Symbol> symbols) {
        this.symbols = symbols;
    }
}
