package Parser;

import Components.*;

import java.util.*;

/**
 * LL1PredictiveParserImpl is the implementation of an LL1Parser
 * based on an LL1 Parsing Table (Predictive). It utilizes 
 * FIRST and FOLLOW to construct the table. Relies on 1 look ahead.
 */
public class LL1PredictiveParserImpl implements LL1Parser {

    private Grammar grammar;
    private Map<NonTerminal, Set<Tuple2<Terminal, Integer>>> nonTerminalFirstSetMap = new HashMap<>();
    private Map<NonTerminal, Set<Tuple2<Terminal, Integer>>> nonTerminalFollowSetMap = new HashMap<>();
    private HashMap<NonTerminal, HashMap<Terminal, Production>> parseTable = new HashMap<>();
    private final Terminal END_TERMINAL = new Terminal("$", Terminal.Type.END);
    private final Terminal EMPTY_TERMINAL = new Terminal("#", Terminal.Type.EMPTY);
    private final Production EMPTY_PRODUCTION = new Production();

    /**
     * Constructs an empty parse table mapping each NonTerminal to 
     * a set of all valid terminals. Each cell in this table represents
     * the Production (or path) that the nonterminal can "traverse" to 
     * reach the terminal.
     * @param grammar: The provided grammar from where we get the set of terminals
     * and nonTerminals.
     */
    public LL1PredictiveParserImpl(Grammar grammar) {
        this.grammar = grammar;
            for(NonTerminal nonTerminal : grammar.getGrammarMap().keySet()) {
                HashMap<Terminal, Production> terminals = new HashMap<>();
                for(Map.Entry<String, Terminal.Type> entry: grammar.terminalToType.entrySet()) {
                    terminals.put(new Terminal(entry.getKey(), entry.getValue()), null);
                }
                parseTable.put(nonTerminal, terminals);
            }
    }

    /**
     * For each nonterminal find its first terminal for each of its productions.
     * This requires looking for the first terminal, or resolving the first 
     * NonTerminal in its production to its firstSet.
     * @param nonTerminal: The nonTerminal we want to get the first set for
     * @return: A Set of the terminal and its position within that nonterminals, production set.
     */
    private Set<Tuple2<Terminal, Integer>> first(NonTerminal nonTerminal) {
      	// It is important to maintain order
        Set<Tuple2<Terminal, Integer>> firstSet = new LinkedHashSet<>();
        // Represent Empty as a special terminal
        if(nonTerminal.isNullable) {
            if(nonTerminal.equals(EMPTY_TERMINAL)) {
                return firstSet;
            }
            firstSet.add(new Tuple2<>(EMPTY_TERMINAL, -1));
        }
        List<Production> productions = new ArrayList<>(grammar.getGrammarMap().get(nonTerminal));

        for(int i = 0; i < productions.size(); i++) {
            List<Symbol> symbols = productions.get(i).getSymbols();
            if(symbols.get(0).isTerminal()) {
                firstSet.add(new Tuple2<>((Terminal)symbols.get(0), i));
            } else {
                NonTerminal currNonTerminal = (NonTerminal)symbols.get(0);
                firstSet.addAll(nonTerminalFirstSetMap.get(currNonTerminal) == null ?
                        first(currNonTerminal) :
                        nonTerminalFirstSetMap.get(currNonTerminal));
            }
        }
        return firstSet;
    }

    /**
     * For each nonterminal find its following terminal for each of its productions.
     * This requires looking for the very next (1 lookahead) terminal, or resolving the next 
     * NonTerminal in its production to its followSet. It is imperative to look through all productions
     * to find all the occurrences of its follows.
     * @param nonTerminal: The nonTerminal we want to get the follow set for
     * @param allProductions: All The productions to look through to find the following terminal for
     * the occurrence of that nonterminal.
     * @return: A Set of the terminal and its position within that nonterminals, production set.
     */
    private Set<Tuple2<Terminal, Integer>> follow(NonTerminal nonTerminal, Collection<Set<Production>> allProductions) {
        Set<Tuple2<Terminal, Integer>> followSet = new LinkedHashSet<>();
        // Represent the End terminal "$" as a special terminal
        if(nonTerminal.isStart) {
            followSet.add(new Tuple2<>(END_TERMINAL, -1));
        }

        for(Set<Production> productions : allProductions) {
            List<Production> productionList = new ArrayList<>(productions);
            for(int j = 0; j < productionList.size(); j++) {
                List<Symbol> symbols = productionList.get(j).getSymbols();
                int symbolLen = symbols.size();
                for (int i = 0; i < symbolLen; i++) {
                  	// Shouldn't be the very last element.
                    if (symbols.get(i).equals(nonTerminal) && i != symbolLen - 1) {
                        if (symbols.get(i + 1).isTerminal()) {
                            followSet.add(new Tuple2<>((Terminal)symbols.get(i + 1), j));
                        } else {
                            NonTerminal currNonTerminal = (NonTerminal) symbols.get(i + 1);
                            followSet.addAll(nonTerminalFollowSetMap.get(currNonTerminal) == null ?
                                    follow(currNonTerminal, allProductions) :
                                    nonTerminalFollowSetMap.get(currNonTerminal));
                        }
                    }
                }
            }
        }
        return followSet;

    }

    /**
     * Generate all the first sets for each NonTerminal
     */
    private void constructFirstSets() {
        for(NonTerminal nonTerminal : grammar.getGrammarMap().keySet()) {
            nonTerminalFirstSetMap.put(nonTerminal, first(nonTerminal));
        }
    }

    /**
     * Generate all the follow sets for each NonTerminal
     */
    private void constructFollowSets() {
        for(NonTerminal nonTerminal : grammar.getGrammarMap().keySet()) {
            nonTerminalFollowSetMap.put(nonTerminal, follow(nonTerminal, grammar.getGrammarMap().values()));
        }
    }

    public Map<NonTerminal, Set<Tuple2<Terminal, Integer>>> getFirstSets() {
        return nonTerminalFirstSetMap;
    }

    public Map<NonTerminal, Set<Tuple2<Terminal, Integer>>> getFollowSets() {
        return nonTerminalFollowSetMap;
    }

    /**
     * A function to pre-process the parse table based on the first and follow sets.
     * This gives us access to the dedicated path a nonterminal will expand to resolve
     * the terminal if it exists.
     * @return: The filled in parseTable.
     */
    public HashMap<NonTerminal, HashMap<Terminal, Production>> constructParseTable() {
        if(nonTerminalFirstSetMap.isEmpty())
            constructFirstSets();
        if(nonTerminalFollowSetMap.isEmpty())
            constructFollowSets();
        for(NonTerminal nonTerminal : parseTable.keySet()) {
            List<Production> productions = new ArrayList<>(grammar.getGrammarMap().get(nonTerminal));
            for(Tuple2<Terminal, Integer> firstTerminalAndProdIdx : nonTerminalFirstSetMap.get(nonTerminal)) {
                Terminal terminal = firstTerminalAndProdIdx.first;
                /* If the nonTerminal is nullable then we shouldn't look through its resolved set of
                 * firsts, but rather the nonTerminal that was used to resolve it. For this grammar,
                 * this index is the very first element in the production set for the nonTerminal.
                 */
                int productionIdx = nonTerminal.isNullable ? 0 : firstTerminalAndProdIdx.second;
                /* For non empty terminals, we simply look if the specified terminal exists in the
                 * first set for that nonTerminal.
                 */
                if(!terminal.equals(EMPTY_TERMINAL)) {
                    parseTable.get(nonTerminal).put(terminal, productions.get(productionIdx));
                } else {
                	 /*
                	 	* Otherwise, we need to look through the follow set since the non terminal will resolve
                	 	* to empty.
                	 	*/
                    for(Tuple2<Terminal, Integer> followTerminalAndProdIdx : nonTerminalFollowSetMap.get(nonTerminal)) {
                        terminal = followTerminalAndProdIdx.first;
                        parseTable.get(nonTerminal).put(terminal, EMPTY_PRODUCTION);
                    }
                }
            }
        }
        return parseTable;
    }

    /**
     * Parses through the tokenized input to see if the input is parsable.
     * This utilizes a stack of the symbols to eventually try to resolve to
     * the input token stream. In the end only the end Terminals should remain.
     * @param tokens: the list of valid terminals
     * @return: Whether the parse is successful or not
     */
    public boolean parse(List<Terminal> tokens){
        tokens.add(END_TERMINAL);
        Stack<Symbol> symbolStack = new Stack<Symbol>(){{
            push(END_TERMINAL);
            push(Grammar.findStartSymbol());
        }};
        while(!symbolStack.peek().equals(END_TERMINAL)) {
            Symbol currToken = symbolStack.pop();
            // Ignore empty terminal
            if(currToken.equals(EMPTY_TERMINAL)) {
                continue;
            }
            // If we've run out of tokens while still trying to resolve, the input is not parsable
            if(tokens.isEmpty()) {
                return false;
            }
            // Pop the token from the input
            if(currToken.isTerminal()) {
                if(tokens.get(0).equals(currToken)) {
                    tokens.remove(0);
                } else {
                    return false;
                }
            } else if(parseTable.get(currToken).get(tokens.get(0)) != null) {
                // A path to the resolution of the NonTerminal to said token exists
                List<Symbol> symbols = parseTable.get(currToken).get(tokens.get(0)).getSymbols();
                // Add them in reverse for Top Down Approach
                for(int i = symbols.size() - 1; i >= 0; i--) {
                    symbolStack.push(symbols.get(i));
                }
            } else {
                return false;
            }
        }
        // If their are still terminals to resolve, then it means we can't generate the valid resolutions.
        return symbolStack.size() == 1 && tokens.size() == 1;
    }
}
