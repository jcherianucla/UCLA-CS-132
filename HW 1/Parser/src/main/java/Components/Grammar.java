package Components;

import java.util.*;

/**
 * Grammar represents any grammar the user wishes to pass in.
 * Here the Grammar is limited to a set of hardcoded terminals and nonterminals
 * to create efficient lookups.
 */
public class Grammar {
    private static Map<NonTerminal, Set<Production>> grammarMap = new HashMap<>();
    // Maps all terminals to their underlying types - Hardcoded
    public static Map<String, Terminal.Type> terminalToType =
            new HashMap<String, Terminal.Type>() {{
                put("if", Terminal.Type.IF);
                put("else", Terminal.Type.ELSE);
                put("while", Terminal.Type.WHILE);
                put("true", Terminal.Type.TRUE);
                put("false", Terminal.Type.FALSE);
                put("!", Terminal.Type.NEGATION);
                put("{", Terminal.Type.OPEN_CURLY);
                put("}", Terminal.Type.CLOSE_CURLY);
                put("(", Terminal.Type.OPEN_PAREN);
                put(")", Terminal.Type.CLOSE_PAREN);
                put("System.out.println", Terminal.Type.PRINT);
                put(";", Terminal.Type.SEMICOLON);
            }};
    // Maps all nonterminals to their underlying types - Hardcoded
    public static Map<String, NonTerminal.Type> nonterminalToType =
            new HashMap<String, NonTerminal.Type>() {{
               put("S", NonTerminal.Type.STATEMENT);
               put("E", NonTerminal.Type.CONDITIONAL);
               put("L", NonTerminal.Type.L);
               put("#", NonTerminal.Type.EMPTY);
            }};

    /**
     * Takes in a grammar string in a modified EBNF format.
     * NonTerminal:Production|Production|Production
     * For each Production, we use the delimiter " " to distinguish between a
     * terminal and non terminal.
     * @param strGrammar: String representation of the grammar as per the above format
     */
    public Grammar(String strGrammar) {
        List<String> nonTerminalLines = new ArrayList<>(Arrays.asList(strGrammar.split("\\r?\\n")));
        int counter = 0;
        // Break into strings for each non terminal to production set
        for(String line : nonTerminalLines) {
          	/* 0 -- LHS: NonTerminal
          	 * 1 -- RHS: Set of Productions
          	 */
            String split[] = line.split("[:]");
            NonTerminal currNonTerminal = new NonTerminal(split[0], nonterminalToType.get(split[0]));
            if(counter == 0) {
                currNonTerminal.isStart = true;
            }
            grammarMap.put(currNonTerminal, generateProductions(split[1]));
            counter++;
        }
        // Mark Nonterminals as nullable if the production contains a nullable terminal or nonTerminal.
        for(Map.Entry<NonTerminal, Set<Production>> entry : grammarMap.entrySet()) {
            for(Production production : entry.getValue()) {
                entry.getKey().isNullable = production.hasNullable();
            }
        }
    }

    public static Map<NonTerminal, Set<Production>> getGrammarMap() {
        return grammarMap;
    }

    /**
     * Finds the start symbol (The very first read in non terminal)
     * @return: A NonTerminal if it exists, else null
     */
    public static NonTerminal findStartSymbol() {
        for(NonTerminal nonTerminal: grammarMap.keySet()) {
            if(nonTerminal.isStart) {
                return nonTerminal;
            }
        }
        return null;
    }

    /**
     * Creates a set of productions from the string representation
     * @param symbols: String representation of productions, delimited by "|"
     * @return: The Set of Productions
     */
    private Set<Production> generateProductions(String symbols) {
        Set<Production> productions = new LinkedHashSet<>();
        List<String> strProductions = new ArrayList<>(Arrays.asList(symbols.split("[|]")));
        for (String strProduction : strProductions) {
            productions.add(new Production(strProduction));
        }
        return productions;
    }
}
