import syntaxtree.Goal;

import java.util.LinkedList;

public class J2V {

    private static final String PARSE_ERROR = "Parse error";

    public static void printVapor(LinkedList<String> vapor) {
        for(String line : vapor) {
            System.out.println(line);
        }
    }

    public static void main (String [] args) {
        try {
            // Start parse
            Goal goal = new MiniJavaParser(System.in).Goal();
            ContextVisitor contextVisitor = new ContextVisitor();
            // Build Context Table
            goal.accept(contextVisitor);
            // Print out the VMT
            contextVisitor.printVMTs();
            TranslatorVisitor translatorVisitor = new TranslatorVisitor();
            // Generate Vapor AST
            LinkedList<String> vaporCode = goal.accept(translatorVisitor, contextVisitor.classes);
            // Print out vapor
            printVapor(vaporCode);
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println(PARSE_ERROR);
            return;
        }
    }

}
