package Parser;

import Components.Terminal;

import java.util.List;

/**
 * Represents the minimal functionality required by any
 * LL1Parser. LL1Parsers are special Top Down Parse
 * that avoid backtracking, and work with LL1 grammars.
 */
public interface LL1Parser {
    boolean parse(List<Terminal> tokens);
}
