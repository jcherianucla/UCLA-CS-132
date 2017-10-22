# LL1 Parser

This project contains source code for an LL1 Parser, using a Parsing Table. The project was written using Intellij IDEA and therefore contains some extraneous files. The most important areas are:
* ```src/main/java/*```- Contains all the source files for the parser.
* ```out/production/Parse/Parse.class``` - This is the entry point of the program. This directory contains all the compiled sources.
* ```testcases``` - Where the test files exist

### Source Code Breakdown

* ```Parse``` - This is the main class, which builds the static grammar, reads the test file from ```stdin```, tokenizes the input, produces the parse table and validates the input for parsability.
* ```Components/*``` - Contains all the key building blocks for the parser
    * ```Symbol``` - The abstract wrapper around the string representing some symbol from the grammar.
        * ```Terminal``` - The implementation of a terminating symbol in the grammar
        * ```NonTerminal``` - The implementation of a nonterminating symbol in the grammar, that should resolve to a terminal.
    * ```Production``` - A wrapper around a set of symbols that represent the production (resolution paths) for the nonterminals.
    * ```Grammar``` - Primarily a static object that has the list of valid terminals and non terminals for the LL1 grammar.
    * ```Tuple2``` - A convenience object to represent a pair.
* ```Tokenizer``` - The tokenizing object that takes in a string to produce a list of terminals if valid.
* ```Parser``` - All parsers implemented by this program
    * ```LL1Parser``` - The interface for any general LL1 Parser
    * ```LL1PredictiveParserImpl``` - An implementation of a predictive LL1 Parser using parsing tables from a grammar.


### How to build

From the root of the project, run gradle.

```bash
$ gradle build
```

### How to test

The tests for this are very minimal. The testing framework was provided by the class. Unfortunately, right now there are no unit tests written, due to time constraints.
This testing framework also creates a buildable distribution based on student ID for submission. You can add your own tests by creating files in the ```testcases``` directory.
```bash
$ gradle :pregrade
```

