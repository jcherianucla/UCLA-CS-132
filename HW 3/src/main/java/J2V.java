import syntaxtree.Goal;

import java.util.LinkedList;

public class J2V {

    private static final String PARSE_ERROR = "Parse error";

    public static void printVapor(LinkedList<String> vapor, ContextVisitor ctx) {
        ctx.printVMTs();
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
            // Test
            //contextVisitor.printContext();
            //System.out.println("-------------");
            TranslatorVisitor translatorVisitor = new TranslatorVisitor();
            LinkedList<String> vaporCode = goal.accept(translatorVisitor, contextVisitor.classes);
            printVapor(vaporCode, contextVisitor);
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println(PARSE_ERROR);
            return;
        }
    }

}
