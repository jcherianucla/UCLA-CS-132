import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.ArrayList;
import java.util.HashMap;

public class TranslatorVisitor extends GJDepthFirst<ArrayList<String>, HashMap<String, VClass>> {

    private int indentLevel = 0;
    public void printFunc(VClass _class, VMethod _method) {
        System.out.print("func " + _class.className + "." + _method.methodName+"(this");
        int paramSize = _method.params.size();
        for(int i = 0; i < paramSize; i++) {
            System.out.print(" " + _method.params.get(i));
        }
        System.out.print(")\n");
    }

    public void printMain() {
        System.out.println("func Main()");
    }

    public void printAlloc(String label) {
        System.out.print("HeapAllocZ("+label+")\n");
    }

    public void printError(String error) {
        System.out.println("Error(\""+error+"\")");
    }

    public void printPrint(String label) {
        System.out.println("PrintIntS("+label+")");
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
    public ArrayList<String> visit(Goal n, HashMap<String, VClass> argu) {
        return null;
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
    public ArrayList<String> visit(MainClass n, HashMap<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> ClassDeclaration()
     * | ClassExtendsDeclaration()
     *
     * @param n
     * @param argu
     */
    @Override
    public ArrayList<String> visit(TypeDeclaration n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(ClassDeclaration n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(ClassExtendsDeclaration n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(VarDeclaration n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(MethodDeclaration n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(FormalParameterList n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(FormalParameter n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(FormalParameterRest n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(Type n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(ArrayType n, HashMap<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "boolean"
     *
     * @param n
     * @param argu
     */
    @Override
    public ArrayList<String> visit(BooleanType n, HashMap<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "int"
     *
     * @param n
     * @param argu
     */
    @Override
    public ArrayList<String> visit(IntegerType n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(Statement n, HashMap<String, VClass> argu) {
        return null;
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
    public ArrayList<String> visit(Block n, HashMap<String, VClass> argu) {
        return null;
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
    public ArrayList<String> visit(AssignmentStatement n, HashMap<String, VClass> argu) {
        return null;
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
    public ArrayList<String> visit(ArrayAssignmentStatement n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(IfStatement n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(WhileStatement n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(PrintStatement n, HashMap<String, VClass> argu) {
        return null;
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
    public ArrayList<String> visit(Expression n, HashMap<String, VClass> argu) {
        return null;
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
    public ArrayList<String> visit(AndExpression n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(CompareExpression n, HashMap<String, VClass> argu) {
        return null;
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
    public ArrayList<String> visit(PlusExpression n, HashMap<String, VClass> argu) {
        return null;
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
    public ArrayList<String> visit(MinusExpression n, HashMap<String, VClass> argu) {
        return null;
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
    public ArrayList<String> visit(TimesExpression n, HashMap<String, VClass> argu) {
        return null;
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
    public ArrayList<String> visit(ArrayLookup n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(ArrayLength n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(MessageSend n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(ExpressionList n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(ExpressionRest n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(PrimaryExpression n, HashMap<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     *
     * @param n
     * @param argu
     */
    @Override
    public ArrayList<String> visit(IntegerLiteral n, HashMap<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "true"
     *
     * @param n
     * @param argu
     */
    @Override
    public ArrayList<String> visit(TrueLiteral n, HashMap<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "false"
     *
     * @param n
     * @param argu
     */
    @Override
    public ArrayList<String> visit(FalseLiteral n, HashMap<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> <IDENTIFIER>
     *
     * @param n
     * @param argu
     */
    @Override
    public ArrayList<String> visit(Identifier n, HashMap<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "this"
     *
     * @param n
     * @param argu
     */
    @Override
    public ArrayList<String> visit(ThisExpression n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(ArrayAllocationExpression n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(AllocationExpression n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(NotExpression n, HashMap<String, VClass> argu) {
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
    public ArrayList<String> visit(BracketExpression n, HashMap<String, VClass> argu) {
        return null;
    }
}
