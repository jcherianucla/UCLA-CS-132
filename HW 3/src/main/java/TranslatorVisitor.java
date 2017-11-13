import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.ArrayList;
import java.util.HashMap;

public class TranslatorVisitor extends GJDepthFirst<StringBuilder, HashMap<String, VClass>> {

    private int indentLevel = 0;
    private int var_counter = 0;
    private String currentClass;
    private String currentMethod;
    private StringBuilder vapor = new StringBuilder();

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
    public StringBuilder visit(Goal n, HashMap<String, VClass> argu) {
        vapor.append(n.f0.accept(this, argu));
        for(Node _class : n.f1.nodes){
            vapor.append(_class.accept(this, argu));
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
    public StringBuilder visit(MainClass n, HashMap<String, VClass> argu) {
        StringBuilder main = new StringBuilder("func Main()\n");
        currentClass = "main";
        for(Node _vars : n.f14.nodes) {
            main.append(_vars.accept(this, argu));
        }
        for(Node _stmt : n.f15.nodes) {
            main.append(_stmt.accept(this, argu));
        }
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
    public StringBuilder visit(TypeDeclaration n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(ClassDeclaration n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(ClassExtendsDeclaration n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(VarDeclaration n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(MethodDeclaration n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(FormalParameterList n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(FormalParameter n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(FormalParameterRest n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(Type n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(ArrayType n, HashMap<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "boolean"
     *
     * @param n
     * @param argu
     */
    @Override
    public StringBuilder visit(BooleanType n, HashMap<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "int"
     *
     * @param n
     * @param argu
     */
    @Override
    public StringBuilder visit(IntegerType n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(Statement n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(Block n, HashMap<String, VClass> argu) {
        StringBuilder block = new StringBuilder<>();
        for(Node _stmt : n.f1.nodes) {
            block.append(_stmt.accept(this, argu));
        }
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
    public StringBuilder visit(AssignmentStatement n, HashMap<String, VClass> argu) {
        String id = n.f0.f0.toString();
        StringBuilder assignment = new StringBuilder<>();
        // If its not local it must be instance - guaranteed typecheck
        if(argu.get(currentClass).getMethod(currentMethod).locals.indexOf(id) == -1) {
            int idx = argu.get(currentClass).members.indexOf(id);
            int offset = 4 + 4*idx;
            assignment.append("[this + "+offset+"] = ");
        } else {
            assignment.append(id + " = ");
        }
        assignment.append(n.f2.accept(this, argu));
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
    public StringBuilder visit(ArrayAssignmentStatement n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(IfStatement n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(WhileStatement n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(PrintStatement n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(Expression n, HashMap<String, VClass> argu) {
        return n.f0.accept(this, argu);
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
    public StringBuilder visit(AndExpression n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(CompareExpression n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(PlusExpression n, HashMap<String, VClass> argu) {
        StringBuilder add = new StringBuilder("Add(");
        StringBuilder lhs = n.f0.accept(this, argu).append(" ")
        StringBuilder rhs = n.f2.accept(this, argu).append(")");
        add.append(lhs);
        add.append(rhs);
        return add;
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
    public StringBuilder visit(MinusExpression n, HashMap<String, VClass> argu) {
        StringBuilder sub = new StringBuilder("Sub(");
        StringBuilder lhs = n.f0.accept(this, argu).append(" ")
        StringBuilder rhs = n.f2.accept(this, argu).append(")");
        sub.append(lhs);
        sub.append(rhs);
        return sub;
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
    public StringBuilder visit(TimesExpression n, HashMap<String, VClass> argu) {
        StringBuilder mul = new StringBuilder("MulS(");
        StringBuilder lhs = n.f0.accept(this, argu).append(" ")
        StringBuilder rhs = n.f2.accept(this, argu).append(")");
        mul.append(lhs);
        mul.append(rhs);
        return mul;
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
    public StringBuilder visit(ArrayLookup n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(ArrayLength n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(MessageSend n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(ExpressionList n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(ExpressionRest n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(PrimaryExpression n, HashMap<String, VClass> argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     *
     * @param n
     * @param argu
     */
    @Override
    public StringBuilder visit(IntegerLiteral n, HashMap<String, VClass> argu) {
        return new StringBuilder(n.f0.f0.toString());
    }

    /**
     * f0 -> "true"
     *
     * @param n
     * @param argu
     */
    @Override
    public StringBuilder visit(TrueLiteral n, HashMap<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> "false"
     *
     * @param n
     * @param argu
     */
    @Override
    public StringBuilder visit(FalseLiteral n, HashMap<String, VClass> argu) {
        return null;
    }

    /**
     * f0 -> <IDENTIFIER>
     *
     * @param n
     * @param argu
     */
    @Override
    public StringBuilder visit(Identifier n, HashMap<String, VClass> argu) {
        return new StringBuilder(n.f0.f0.toString());
    }

    /**
     * f0 -> "this"
     *
     * @param n
     * @param argu
     */
    @Override
    public StringBuilder visit(ThisExpression n, HashMap<String, VClass> argu) {
        return new StringBuilder(n.f0);
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
    public StringBuilder visit(ArrayAllocationExpression n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(AllocationExpression n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(NotExpression n, HashMap<String, VClass> argu) {
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
    public StringBuilder visit(BracketExpression n, HashMap<String, VClass> argu) {
        return n.f1.accept(this, argu);
    }
}
