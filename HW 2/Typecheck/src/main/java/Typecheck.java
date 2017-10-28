import context.ContextVisitor;
import context.MJTypeCheckException;
import context.TypecheckVisitor;
import syntaxtree.*;

public class Typecheck{

    private static final String ERROR = "Type error";
    private static final String SUCCESS = "Program type checked successfully";
    
    public static void main (String [] args) {
        try {
            // Parse
            Goal goal = new MiniJavaParser(System.in).Goal();
            // Build context table
            ContextVisitor ctxVisitor = new ContextVisitor();
            goal.accept(ctxVisitor, null);
            ctxVisitor.context.printContextTable();
            TypecheckVisitor typecheckVisitor = new TypecheckVisitor();
            goal.accept(typecheckVisitor, ctxVisitor.context);
            // Perform Type Check here
        } catch (MJTypeCheckException e) {
            System.out.println(e.getMessage());
            System.out.print(ERROR);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        System.out.print(SUCCESS);
    }

}
