package Tokenizer;

import Components.Grammar;
import Components.Terminal;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Tokenizer goes through an input string to convert
 * into a set of terminals if recognized by the Grammar.
 * Here the set of valid terminals has been hardcoded to comply
 * with our LL1 Grammar.
 */
public class Tokenizer {
  	/**
  	 * A Token is simply a wrapper for patterns
  	 */
    private final class Token {
        private final Pattern pattern;

        public Token(Pattern pattern) {
            this.pattern = pattern;
        }
    }
    // Must start with the provided pattern
    private static final String BASE_REGEX = "^(\\s*%s)";
    // Used for the pattern matching
    private static final List<String> TERMINAL_PATTERNS = Arrays.asList(
            "\\{",
            "\\}",
            "\\(",
            "\\)",
            "!",
            ";",
            "if",
            "else",
            "while",
            "true",
            "false",
            "System.out.println"
    );
    private final List<Token> tokens = new ArrayList<Token>();
    private final String input;
    /**
     * Takes a string and adds them as tokens
     * @param input: String representation of terminals
     */
    public Tokenizer(String input) {
        this.input = input.replaceAll("\\r\\n|\\r|\\n", "");
        for(int i = 0; i < TERMINAL_PATTERNS.size(); i++) {
            addToken(TERMINAL_PATTERNS.get(i));
        }
    }

    /**
     * @param regex: Compiles the pattern as per the regex Pattern
     */
    private void addToken(String regex) {
        tokens.add(new Token(Pattern.compile(String.format(BASE_REGEX, regex))));
    }

    /**
     * Generates a list of terminals if valid. It does this by 
     * trying to match the input with any given token, while removing
     * the matched elements from the input.
     * @return: The list of valid terminals
     * @exception: If an invalid terminal is found, then we throw an exception
     */
    public List<Terminal> generateTerminals() throws IOException{
        List<Terminal> terminals = new ArrayList<>();
        String localInput = new String(input);
        while(!localInput.equals("")) {
            boolean didMatch = false;
          	// Try to match against all tokens
            for (Token token : tokens) {
                Matcher matcher = token.pattern.matcher(localInput);
                if (matcher.find()) {
                    didMatch = true;
                    String sequence = matcher.group().trim();
                    terminals.add(new Terminal(sequence, Grammar.terminalToType.get(sequence)));
                    // Remove matched elements from the input
                    localInput = matcher.replaceFirst("");
                    break;
                }
            }
            if(!didMatch) {
                throw new IOException("Could not find any valid tokens");
            }
        }
        return terminals;
    }
}
