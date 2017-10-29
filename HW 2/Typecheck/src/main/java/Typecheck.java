import context.ContextVisitor;
import context.MJTypeCheckException;
import context.TypecheckVisitor;
import syntaxtree.*;

/**
 * Typecheck runs the overall program by first building out a
 * context table for all the symbols and then running a type checker
 * with MiniJava Type rules in play with the global context.
 */
public class Typecheck{

    private static final String ERROR = "Type error";
    private static final String SUCCESS = "Program type checked successfully";
    
    public static void main (String [] args) {
        try {
            // Parse
            Goal goal = new MiniJavaParser(System.in).Goal();
            // Build context table
            ContextVisitor ctxVisitor = new ContextVisitor();
            // Run context builder with DFS from Goal
            goal.accept(ctxVisitor, null);
            // Create a type checker
            TypecheckVisitor typecheckVisitor = new TypecheckVisitor();
            // Run Type checking DFS from Goal
            goal.accept(typecheckVisitor, ctxVisitor.context);
        } catch (Exception e) {
            System.out.println(ERROR);
            return;
        }
        System.out.println(SUCCESS);
    }

}
