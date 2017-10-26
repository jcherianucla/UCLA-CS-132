package typecheck;

import context.ContextVisitor;
import context.MJTypeCheckException;
import syntaxtree.Goal;

import java.text.ParseException;

public class Main {
    
    public static void main (String [] args) {
        try {
            // Parse
            Goal goal = new MiniJavaParser(System.in).Goal();
            // Build context table
            ContextVisitor ctxVisitor = new ContextVisitor();
            goal.accept(ctxVisitor, null);
            ctxVisitor.context.printContextTable();
            // Perform Type Check here
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (MJTypeCheckException e) {
            System.out.print(e.getMessage());
            System.exit(-1);
        }
    }

}
