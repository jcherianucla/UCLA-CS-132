import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

public class TranslatorVisitor extends GJDepthFirst<LinkedList<String>, Map<String, VClass>> {

    private int indentLevel = 0;
    private int varCounter = 0;
    private int nullCounter = 1;
    private int elseCounter = 1;
    private String currentClass;
    private String currentMethod;
    private LinkedList<String> vapor = new LinkedList<String>();

    private boolean isLiteral(LinkedList<String> expr) {
        try{
            Integer.parseInt(expr.getLast());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean isSingle(LinkedList<String> expr) {
        return expr.size() == 1 && expr.getLast().length() == 1;
    }

    private String extractLastVar(LinkedList<String> expr) {
        return expr.getLast().split("=")[0].trim();
    }

    private String extractLastExpr(LinkedList<String> expr) {
        return expr.getLast().split("=")[1].trim();
    }

    private String indent() {
        String indent = "";
        for(int i = 0; i < indentLevel; i++)
            indent += "\t";
        return indent;
    }

    private void clearCounter() {
        varCounter = 0;
    }

    private String createTemp() {
        return "t." + varCounter++;
    }

    private String createNullLabel(boolean end) {
        return end ? "null" + nullCounter++ + ":" : ":null" + nullCounter;
    }

    private String createElseLabel(boolean end) {
        return end ? "if" + elseCounter++ + "_else:" : "if" + elseCounter + "_else";
    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(Goal n, Map<String, VClass> argu) {
        vapor.addAll(n.f0.accept(this, argu));
        for(Node _class : n.f1.nodes){
            vapor.addAll(_class.accept(this, argu));
        }
        return vapor;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(MainClass n, Map<String, VClass> argu) {
        LinkedList<String> main = new LinkedList<String>(Arrays.asList("func Main"));
        indentLevel++;
        currentClass = "main";
        currentMethod = "main";
        for(Node _stmt : n.f15.nodes) {
            main.addAll(_stmt.accept(this, argu));
        }
        main.add(indent() + "ret");
        indentLevel--;
        return main;
    }

    /**
     * f0 -> ClassDeclaration()
     * | ClassExtendsDeclaration()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(TypeDeclaration n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(ClassDeclaration n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(ClassExtendsDeclaration n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(VarDeclaration n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(MethodDeclaration n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> ( FormalParameterRest() )*
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(FormalParameterList n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(FormalParameter n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(FormalParameterRest n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> ArrayType()
     * | BooleanType()
     * | IntegerType()
     * | Identifier()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(Type n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(ArrayType n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "boolean"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(BooleanType n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "int"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(IntegerType n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> Block()
     * | AssignmentStatement()
     * | ArrayAssignmentStatement()
     * | IfStatement()
     * | WhileStatement()
     * | PrintStatement()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(Statement n, Map<String, VClass> argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(Block n, Map<String, VClass> argu) {
        LinkedList<String> block = new LinkedList<>();
        indentLevel++;
        for(Node _stmt : n.f1.nodes) {
            block.addAll(_stmt.accept(this, argu));
        }
        indentLevel--;
        return block;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(AssignmentStatement n, Map<String, VClass> argu) {
        String id = n.f0.f0.toString();
        LinkedList<String> assignment = new LinkedList<>();
        LinkedList<String> expression = n.f2.accept(this, argu);
        if(!isSingle(expression))
            assignment.addAll(expression);
        // If its not local it must be instance - guaranteed typecheck
        if(argu.get(currentClass).getMethod(currentMethod).locals.indexOf(id) == -1) {
            int idx = argu.get(currentClass).members.indexOf(id);
            int offset = 4 + 4*idx;
            assignment.add(indent() + createTemp() + " = [this + " + offset +"]");
        }
        String val = assignment.isEmpty() ? expression.getLast() : extractLastVar(assignment);
        assignment.add(indent() + id + " = " + val);
        return assignment;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(ArrayAssignmentStatement n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(IfStatement n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(WhileStatement n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(PrintStatement n, Map<String, VClass> argu) {
        LinkedList<String> expression = n.f2.accept(this, argu);
        LinkedList<String> print = new LinkedList<>();
        print.add(indent() + "PrintIntS(" + extractLastVar(expression) + ")");
        return print;
    }

    /**
     * f0 -> AndExpression()
     * | CompareExpression()
     * | PlusExpression()
     * | MinusExpression()
     * | TimesExpression()
     * | ArrayLookup()
     * | ArrayLength()
     * | MessageSend()
     * | PrimaryExpression()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(Expression n, Map<String, VClass> argu) {
        return n.f0.accept(this, argu);
    }

    private LinkedList<String> binaryOp(Node l, Node r, Map<String, VClass> argu, String type) {
        LinkedList<String> current = new LinkedList<>();
        LinkedList<String> lhs = l.accept(this, argu);
        LinkedList<String> rhs = r.accept(this, argu);
        if (!isSingle(lhs))
            current.addAll(lhs);
        String val1 = isSingle(lhs) ? lhs.getLast() : extractLastVar(current);
        if (!isSingle(rhs))
            current.addAll(rhs);
        String val2 = isSingle(rhs) ? rhs.getLast() : extractLastVar(current);
        current.add(indent() + createTemp() + " = " + type + "(" + val1 + " " + val2 + ")");
        return current;

    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(AndExpression n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(CompareExpression n, Map<String, VClass> argu) {
        return binaryOp(n.f0, n.f1, argu, "LtS");
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(PlusExpression n, Map<String, VClass> argu) {
        return binaryOp(n.f0, n.f2, argu, "Add");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(MinusExpression n, Map<String, VClass> argu) {
        return binaryOp(n.f0, n.f2, argu, "Sub");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(TimesExpression n, Map<String, VClass> argu) {
        return binaryOp(n.f0, n.f2, argu, "MulS");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(ArrayLookup n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(ArrayLength n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(MessageSend n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> Expression()
     * f1 -> ( ExpressionRest() )*
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(ExpressionList n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(ExpressionRest n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> IntegerLiteral()
     * | TrueLiteral()
     * | FalseLiteral()
     * | Identifier()
     * | ThisExpression()
     * | ArrayAllocationExpression()
     * | AllocationExpression()
     * | NotExpression()
     * | BracketExpression()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(PrimaryExpression n, Map<String, VClass> argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(IntegerLiteral n, Map<String, VClass> argu) {
        return new LinkedList<String>(Arrays.asList(n.f0.toString()));
    }

    /**
     * f0 -> "true"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(TrueLiteral n, Map<String, VClass> argu) {
        return new LinkedList<String>(Arrays.asList("1"));
    }

    /**
     * f0 -> "false"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(FalseLiteral n, Map<String, VClass> argu) {
        return new LinkedList<String>(Arrays.asList("0"));
    }

    /**
     * f0 -> <IDENTIFIER>
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(Identifier n, Map<String, VClass> argu) {
        String id = n.f0.toString();
        return new LinkedList<String>(Arrays.asList(id));
    }

    /**
     * f0 -> "this"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(ThisExpression n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(ArrayAllocationExpression n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(AllocationExpression n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "!"
     * f1 -> Expression()
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(NotExpression n, Map<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(BracketExpression n, Map<String, VClass> argu) {
        return n.f1.accept(this, argu);
    }
}
