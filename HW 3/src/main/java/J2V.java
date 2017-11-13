import syntaxtree.Goal;

public class J2V {

    private static final String PARSE_ERROR = "Parse error";
    public static void main (String [] args) {
        try {
            // Start parse
            Goal goal = new MiniJavaParser(System.in).Goal();
            ContextVisitor contextVisitor = new ContextVisitor();
            // Build Context Table
            goal.accept(contextVisitor);
            // Test
            contextVisitor.printContext();
            System.out.println("-------------");
            contextVisitor.printVMTs();
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println(PARSE_ERROR);
            return;
        }
    }

}
