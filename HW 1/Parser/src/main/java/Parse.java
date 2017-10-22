import Components.*;
import Parser.LL1PredictiveParserImpl;
import Tokenizer.Tokenizer;

import java.io.IOException;
import java.util.*;

public class Parse {

    private static final String ERROR = "Parse error";
    private static final String SUCCESS = "Program parsed successfully";

  	/**
  	 * Goes through the grammar to print each non-terminals productions. Used
  	 * primarily for debugging purposes.
  	 **/
    public static void prettyPrintGrammar(Grammar grammar) {
        for(Map.Entry<NonTerminal, Set<Production>> entry: grammar.getGrammarMap().entrySet()) {
            NonTerminal nt = entry.getKey();
            Set<Production> prods = entry.getValue();
            System.out.println("NON-TERMINAL: " + nt.getSymbol());
            for(Production prod : prods) {
                System.out.println("PROD:");
                for(Symbol vocab : prod.getSymbols()) {
                    System.out.println(vocab.getSymbol());
                }
            }
        }
    }

  	/**
  	 * Based on a predictive parser, this function prints the first or follow Set
  	 * for each nonTerminal. Used primarily for debugging purposes.
  	 **/
    public static void prettyPrintSets(LL1PredictiveParserImpl parser, boolean first) {
        Map<NonTerminal, Set<Tuple2<Terminal, Integer>>> setMap = first ? parser.getFirstSets() : parser.getFollowSets();
        for(Map.Entry<NonTerminal, Set<Tuple2<Terminal, Integer>>> entry : setMap.entrySet()) {
            NonTerminal nonTerminal = entry.getKey();
            String setType = first ? "First" : "Follow";
            System.out.println(setType + " Set for " + nonTerminal.getSymbol());
            Set<Tuple2<Terminal, Integer>> terminalSet = entry.getValue();
            for(Tuple2<Terminal, Integer> terminalAndProdIdx : terminalSet) {
                System.out.print("(" + terminalAndProdIdx.first.getSymbol() + ", " + terminalAndProdIdx.second.toString() +"), ");
            }
            System.out.print("\n");
        }
    }

    /**
     * Based on a predictive parser, this function prints the resulting pre processed
     * parse table for the supplied grammar. Used primarily for debugging purposes.
     */
    public static void prettyPrintParseTable(HashMap<NonTerminal, HashMap<Terminal, Production>> table) {
        for(NonTerminal nonTerminal : table.keySet()) {
            System.out.println("ROW " + nonTerminal.getSymbol());
            for(Map.Entry<Terminal, Production> entry : table.get(nonTerminal).entrySet()) {
                System.out.print("Terminal: " + entry.getKey().getSymbol() + "\t");
                System.out.print("Production: ");
                if(entry.getValue() == null) {
                    System.out.print("\"\"\n");
                    continue;
                }
                for(Symbol symbol : entry.getValue().getSymbols()) {
                    System.out.print(symbol.getSymbol() + ", ");
                }
                System.out.print("\n");
            }
        }
    }

    /**
     * Uses a scanner to read from standard input and return
     * the content as a string. Uses a general delimiter to preserve the exact
     * contents of the file - including whitespaces
     * @return
     */
    public static String readFromStdin() {
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\\A");
        String content = new String();
        while(scanner.hasNext())
            content += scanner.next();
        return content;
    }

    public static void main(String[] args) {
        // Grammar in a modified EBNF format
        String grammarStr = "S:{ L }|System.out.println ( E ) ;|if ( E ) S else S|while ( E ) S\nL:S L |#\nE:true|false|! E";
        // Generate the grammar
        Grammar grammar = new Grammar(grammarStr);
        // Create a predictive parser from the grammar
        LL1PredictiveParserImpl parser = new LL1PredictiveParserImpl(grammar);
        String fileContent = readFromStdin();
        // Tokenize the contents
        Tokenizer tokenizer = new Tokenizer(fileContent);
        try {
            List<Terminal> terminals = tokenizer.generateTerminals();
            // Creates the FIRST and FOLLOW sets and builds the parsing table
            parser.constructParseTable();
            // Check if parser can successfully parse
            System.out.println(parser.parse(terminals) ? SUCCESS : ERROR);
        } catch(IOException e) {
            System.out.println(ERROR);
        }
    }
}
