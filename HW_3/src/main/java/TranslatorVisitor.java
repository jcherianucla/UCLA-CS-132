import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

public class TranslatorVisitor extends GJDepthFirst<LinkedList<String>, Map<String, VClass>> {

    private int indentLevel = 0;
    private int varCounter = 0;
    private int nullCounter = 1;
    private int elseCounter = 1;
    private int whileCounter = 1;
    private int boundsCounter = 1;
    private boolean shouldPrintAlloc = false;
    private Stack<String> classStack = new Stack<>();
    private String currentMethod;
    private LinkedList<String> vapor = new LinkedList<>();
    private LinkedList<String> arguments = new LinkedList<>();

    private boolean isLiteral(LinkedList<String> expr) {
        try{
            Integer.parseInt(expr.getLast());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean isClassType(String type) {
        return type != null && !(type.equals("int") || type.equals("int[]") || type.equals("boolean"));
    }

    private boolean isSingleVar(LinkedList<String> expr) {
        return expr.size() == 1 && expr.getLast().split("=").length == 1;
    }

    private boolean isSingle(LinkedList<String> expr) {
        return expr.size() == 1 && (expr.getLast().length() == 1 || isLiteral(expr) || isSingleVar(expr));
    }

    private boolean isCall(LinkedList<String> expr) {
        return expr.size() == 1 && (expr.getLast().contains("call"));
    }

    private String extractLastVar(LinkedList<String> expr) {
        String lhs = expr.getLast().split("=")[0].trim();
        lhs = lhs.replaceAll("[\\[\\]]", "");
        return lhs;
    }

    private String idToString(LinkedList<String> identifier) {
        if(isSingle(identifier)) {
            return identifier.getLast();
        } else {
            return identifier.get(0);
        }
    }

    private void sanitize(LinkedList<String> expr) {
        if(expr.getFirst().split(" ").length == 1) {
            expr.removeFirst();
        }
    }

    private String getVal(LinkedList<String> expr, LinkedList<String> curr) {
        if(!isSingle(expr)) {
            sanitize(expr);
            curr.addAll(expr);
            return extractLastVar(curr);
        }
        return expr.getLast();
    }

    private LinkedList<String> arrayAlloc() {
        LinkedList<String> allocFunc = new LinkedList<>();
        allocFunc.add("func AllocArray(size)");
        indentLevel++;
        allocFunc.add(indent() + "bytes = MulS(size 4)");
        allocFunc.add(indent() + "bytes = Add(bytes 4)");
        allocFunc.add(indent() + "v = HeapAllocZ(bytes)");
        allocFunc.add(indent() + "[v] = size");
        allocFunc.add(indent() + "ret v");
        indentLevel--;
        return allocFunc;
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

    private String error(boolean oob) {
        String base = indent() + "Error(";
        return oob ? base + "\"array index out of bounds\")" : base + "\"null pointer\")";
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
        if(shouldPrintAlloc) {
            vapor.add("\n");
            vapor.addAll(arrayAlloc());
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
        LinkedList<String> main = new LinkedList<>(Arrays.asList("func Main()"));
        indentLevel++;
        //classStack.push("main");
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
        return n.f0.accept(this, argu);
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
        LinkedList<String> _class = new LinkedList<>();
        String className = idToString(n.f1.accept(this, argu));
        classStack.clear();
        classStack.push(className);
        for(Node _method : n.f4.nodes) {
            _class.addAll(_method.accept(this, argu));
        }
        return _class;
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
        LinkedList<String> _class = new LinkedList<>();
        String className = idToString(n.f1.accept(this, argu));
        classStack.clear();
        classStack.push(className);
        for(Node _method : n.f6.nodes) {
            _class.addAll(_method.accept(this, argu));
        }
        return _class;
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
        clearCounter();
        LinkedList<String> _method = new LinkedList<>();
        currentMethod = idToString(n.f2.accept(this, argu));
        String callingClass = classStack.get(0);
        // Find the current method from current class
        VMethod curr = argu.get(callingClass).getMethod(currentMethod);
        String declaration = "func " + callingClass + "." + currentMethod + "(this";
        int paramSize = curr.params.size();
        // Put down params
        for(int i = 0; i < paramSize; i++) {
            declaration += " " + curr.params.get(i);
        }
        declaration += ")";
        _method.add(declaration);
        indentLevel++;
        // Grab all statements
        for(Node _stmt : n.f8.nodes) {
            LinkedList<String> currStmt = _stmt.accept(this, argu);
            _method.addAll(currStmt);
        }
        // Add return statement
        LinkedList<String> retExpr = n.f10.accept(this, argu);
        String retVal = getVal(retExpr, _method);
        _method.add(indent() + "ret " + retVal);
        indentLevel--;
        return _method;
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
            LinkedList<String> currStmt = _stmt.accept(this, argu);
            if (!isSingle(currStmt) && !isCall(currStmt))
                block.addAll(currStmt);
        }
        indentLevel--;
        return block;
    }

    private boolean isLocal(String id, Map<String, VClass> argu) {
        String currentClass = classStack.get(0);
        VMethod _method = argu.get(currentClass).getMethod(currentMethod);
        return (_method.locals.indexOf(id) != -1) || (_method.params.indexOf(id) != -1);
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
        LinkedList<String> assignment = new LinkedList<>();
        String id = idToString(n.f0.accept(this, argu));
        LinkedList<String> expression = n.f2.accept(this, argu);
        String val = getVal(expression, assignment);
        boolean local = isLocal(id, argu);
        String currentClass = classStack.get(0);
        // If its not local it must be instance - guaranteed typecheck
        if(!local) {
            int idx = argu.get(currentClass).getMembers().indexOf(id);
            int offset = 4 + 4*idx;
            id = "[this+" + offset + "]";
        }
        assignment.add(indent() + id + " = " + val);
        return assignment;
    }


    private LinkedList<String> arrDeref(String id, String var, Map<String, VClass> argu) {
        LinkedList<String> arr = new LinkedList<>();
        String currentClass = classStack.get(0);
        // If its not local it must be instance - guaranteed typecheck
        if(!isLocal(id, argu)) {
            if(argu.get(currentClass).getMembers().indexOf(id) != -1) {
                int instanceIdx = argu.get(currentClass).getMembers().indexOf(id);
                int offset = 4 + 4 * instanceIdx;
                arr.add(indent() + var + " = [this + " + offset + "]");
            }
        } else {
            arr.add(indent() + var + " = " + id);
        }
        return arr;
    }

    private LinkedList<String> nullPtrCheck(String var) {
        LinkedList<String> nullptr = new LinkedList<>();
        int currentNullCount = nullCounter++;
        nullptr.add(indent() + "if " + var + " goto :null" + currentNullCount);
        indentLevel++;
        nullptr.add(error(false));
        indentLevel--;
        nullptr.add(indent() + "null" + currentNullCount + ":");
        return nullptr;
    }

    private LinkedList<String> oobCheck(String var) {
        LinkedList<String> oob = new LinkedList<>();
        int currentBoundsCount = boundsCounter++;
        oob.add(indent() + "if " + var + " goto :bounds" + currentBoundsCount);
        indentLevel++;
        oob.add(error(true));
        indentLevel--;
        oob.add(indent() + "bounds" + currentBoundsCount + ":");
        return oob;
    }

    private LinkedList<String> arrayOp(String id, String idx, Map<String, VClass> argu) {
        LinkedList<String> arr = new LinkedList<>();
        String temp1 = createTemp();
        // Assign heap pointer to temp var
        arr.addAll(arrDeref(id, temp1, argu));
        // Null pointer check
        arr.addAll(nullPtrCheck(temp1));
        // Get size
        String temp2 = createTemp();
        arr.add(indent() + temp2 + " = [" + temp1 + "]");
        arr.add(indent() + temp2 + " = Lt(" + idx + " " + temp2 + ")");
        // Out of bounds check
        arr.addAll(oobCheck(temp2));
        // Get to index position
        arr.add(indent() + temp2 + " = MulS(" + idx + " 4)");
        arr.add(indent() + temp2 + " = Add(" + temp2 + " " + temp1 + ")");
        return arr;
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
        LinkedList<String> arrAssignment = new LinkedList<>();
        String id = idToString(n.f0.accept(this, argu));
        LinkedList<String> arrIdx = n.f2.accept(this, argu);
        String idx = getVal(arrIdx, arrAssignment);
        // Perform null check and oob check
        arrAssignment.addAll(arrayOp(id, idx, argu));
        int currentVarCount = varCounter;
        LinkedList<String> arrVal = n.f5.accept(this, argu);
        String val = getVal(arrVal, arrAssignment);
        // Actual assignment
        arrAssignment.add(indent() + "[t." + (currentVarCount - 1) + " + 4] = " + val);

        return arrAssignment;
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
        int currentElseCount = elseCounter++;
        LinkedList<String> expression = n.f2.accept(this, argu);
        LinkedList<String> ifelse = new LinkedList<>();
        String cond = getVal(expression, ifelse);
        ifelse.add(indent() + "if0 " + cond + " goto " + ":if" + currentElseCount + "_else");
        LinkedList<String> ifstmt = n.f4.accept(this, argu);
        indentLevel++;
        if(!isSingle(ifstmt)) {
            sanitize(ifstmt);
            ifelse.addAll(ifstmt);
        }
        ifelse.add(indent() + "goto :if" + currentElseCount + "_end");
        indentLevel--;
        ifelse.add(indent() + "if" + currentElseCount + "_else:");
        LinkedList<String> elsestmt = n.f6.accept(this, argu);
        indentLevel++;
        if(!isSingle(elsestmt)) {
            sanitize(elsestmt);
            ifelse.addAll(elsestmt);
        }
        indentLevel--;
        ifelse.add(indent() + "if" + currentElseCount + "_end:");
        return ifelse;
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
        int currentWhileCount = whileCounter++;
        LinkedList<String> _while = new LinkedList<>();
        _while.add(indent() + "while" + currentWhileCount + "_top:");
        LinkedList<String> expression = n.f2.accept(this, argu);
        indentLevel++;
        String cond = getVal(expression, _while);
        indentLevel--;
        _while.add(indent() + "if0 " + cond + " goto :while" + currentWhileCount + "_end");
        LinkedList<String> _whilestmt = n.f4.accept(this, argu);
        if(!isSingle(_whilestmt)) {
            sanitize(_whilestmt);
            _while.addAll(_whilestmt);
        }
        indentLevel++;
        _while.add(indent() + "goto :while" + currentWhileCount + "_top");
        indentLevel--;
        _while.add(indent() + "while" + currentWhileCount + "_end:");
        return _while;
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
        if(!isSingle(expression)) {
            sanitize(expression);
            print.addAll(expression);
        }
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
        if (!isSingle(lhs)) {
            sanitize(lhs);
            current.addAll(lhs);
        }
        String val1 = isSingle(lhs) ? lhs.getLast() : extractLastVar(current);
        String val2;
        if (!lhs.equals("0")) {
            LinkedList<String> rhs = r.accept(this, argu);
            if (!isSingle(rhs)) {
                sanitize(rhs);
                current.addAll(rhs);
            }
            val2 = isSingle(rhs) ? rhs.getLast() : extractLastVar(current);
        } else {
            val2 = "0";
        }
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
        LinkedList<String> and = new LinkedList<>();
        and.addAll(binaryOp(n.f0, n.f2, argu, "MulS"));
        and.add(indent() + createTemp() + " = Eq(1 " + extractLastVar(and) + ")");
        return and;
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
        return binaryOp(n.f0, n.f2, argu, "LtS");
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
        LinkedList<String> arrLookup = new LinkedList<>();
        LinkedList<String> arrIdx = n.f2.accept(this, argu);
        LinkedList<String> identifier = n.f0.accept(this, argu);
        String idx = getVal(arrIdx, arrLookup);
        String id;
        if(!isSingle(identifier)) {
            if(identifier.getFirst().split(" ").length == 1) {
                varCounter--;
                identifier.removeFirst();
            }
            arrLookup.addAll(identifier);
            id = extractLastVar(arrLookup);
        } else {
            id = identifier.getLast();
        }
        arrLookup.addAll(arrayOp(id, idx, argu));
        int currentVarCount = varCounter;
        arrLookup.add(indent() + createTemp() + " = [t." + (currentVarCount-1) + " + 4]");
        return arrLookup;
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
        LinkedList<String> arrLength = new LinkedList<>();
        LinkedList<String> identifier = n.f0.accept(this, argu);
        String id = getVal(identifier, arrLength);
        String temp = createTemp();
        // Dereference the array
        arrLength.addAll(arrDeref(id, temp, argu));
        // Null pointer check
        arrLength.addAll(nullPtrCheck(temp));
        int currentVarCount = varCounter;
        // Assign length value
        arrLength.add(indent() + createTemp() + " = [t." + (currentVarCount-1) + "]");
        return arrLength;
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
        LinkedList<String> msgSend = new LinkedList<>();
        LinkedList<String> _class = n.f0.accept(this, argu);
        String classPtr = extractLastVar(_class);
        if (!isSingle(_class)) {
            msgSend.addAll(_class);
        }
        // Nullptr check for class pointer
        if (!classPtr.equals("this")) {
            msgSend.addAll(nullPtrCheck(classPtr));
        }
        String methodName = n.f2.accept(this, argu).getLast();
        String currentClass = classStack.peek();
        // Find the method for the calling class
        VMethod currentMethod = argu.get(currentClass).getMethod(methodName);
        // Push on class return type for methods
        if(isClassType(currentMethod.returnType)) {
            classStack.push(currentMethod.returnType);
        }
        int methodIdx = 4 * argu.get(currentClass).getMethods().indexOf(currentMethod);
        int currentVarCount = varCounter;
        // Dereference class pointer
        msgSend.add(indent() + createTemp() + " = [" + classPtr + "]");
        // Get method pointer for class method
        msgSend.add(indent() + createTemp() + " = [t." + currentVarCount + "+" + methodIdx + "]");
        String methodPtr = "t." + ++currentVarCount;
        // Call method
        String methodCall = "call " + methodPtr + "(" + classPtr;
        // Add in arguments
        arguments.clear();
        LinkedList<String> exprList = n.f4.accept(this, argu);
        if (exprList != null)
            msgSend.addAll(exprList);
        int arguSize = arguments.size();
        for(int i = 0; i < arguSize; i++) {
            methodCall += " " + arguments.get(i);
        }
        methodCall += ")";
        // Set result of method call
        msgSend.add(indent() + createTemp() + " = " + methodCall);
        return msgSend;
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
        LinkedList<String> firstExpr = n.f0.accept(this, argu);
        LinkedList<String> exprList = new LinkedList<>();
        String firstArgu = isSingle(firstExpr) ? firstExpr.getLast() : extractLastVar(firstExpr);
        arguments.add(firstArgu);
        if (!isSingle(firstExpr)) {
            sanitize(firstExpr);
            exprList.addAll(firstExpr);
        }
        for(Node _expr : n.f1.nodes) {
            LinkedList<String> currExpr = _expr.accept(this, argu);
            if(!isSingle(currExpr)) {
                sanitize(currExpr);
            }
            String currArgu = isSingle(currExpr) ? currExpr.getLast() : extractLastVar(currExpr);
            arguments.add(currArgu);
            if (!isSingle(currExpr) && !isCall(currExpr))
                exprList.addAll(currExpr);
        }
        return exprList;
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
        return n.f1.accept(this, argu);
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
        return new LinkedList<>(Arrays.asList(n.f0.toString()));
    }

    /**
     * f0 -> "true"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(TrueLiteral n, Map<String, VClass> argu) {
        return new LinkedList<>(Arrays.asList("1"));
    }

    /**
     * f0 -> "false"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(FalseLiteral n, Map<String, VClass> argu) {
        return new LinkedList<>(Arrays.asList("0"));
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
        LinkedList<String> identifier = new LinkedList<>();
        identifier.add(id);
        if (!classStack.isEmpty()) {
            String currentClass = classStack.get(0);
            VMethod callingMethod = argu.get(currentClass).getMethod(currentMethod);
            if (callingMethod != null) {
                // Check local variables, then params then instance variables
                // If the type is not a primitive, pass it up
                if (callingMethod.locals.indexOf(id) != -1) {
                    if (isClassType(callingMethod.localTypes.get(id))) {
                        classStack.push(callingMethod.localTypes.get(id));
                    }
                } else if (callingMethod.params.indexOf(id) != -1) {
                    if (isClassType(callingMethod.paramTypes.get(id))) {
                        classStack.push(callingMethod.paramTypes.get(id));
                    }
                } else {
                    if (isClassType(argu.get(currentClass).types.get(id))) {
                        classStack.push(argu.get(currentClass).types.get(id));
                    }
                    if (argu.get(currentClass).getMembers().indexOf(id) != -1) {
                        int idx = argu.get(currentClass).getMembers().indexOf(id);
                        int offset = 4 + 4*idx;
                        identifier.add(indent() + createTemp() + " = [this+" + offset + "]");
                    }
                }
            }
        }
        return identifier;
    }

    /**
     * f0 -> "this"
     *
     * @param n
     * @param argu
     */
    @Override
    public LinkedList<String> visit(ThisExpression n, Map<String, VClass> argu) {
        classStack.push(classStack.get(0));
        return new LinkedList<>(Arrays.asList("this"));
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
        shouldPrintAlloc = true;
        LinkedList<String> expression = n.f3.accept(this,argu);
        LinkedList<String> arrAlloc = new LinkedList<>();
        // Rely on calling the array allocation function
        if(isSingle(expression)) {
            arrAlloc.add(indent() + createTemp() + " = call :AllocArray(" + expression.getLast() + ")");
        } else {
            arrAlloc.addAll(expression);
            arrAlloc.add(indent() + createTemp() + " = call :AllocArray(" + extractLastVar(arrAlloc) + ")");
        }
        return arrAlloc;
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
        String className = n.f1.accept(this, argu).getLast();
        classStack.push(className);
        String currentClass = classStack.peek();
        VClass curr = argu.get(currentClass);
        LinkedList<String> newClass = new LinkedList<>();
        String currVar = createTemp();
        // Allocate heap space
        newClass.add(indent() + currVar + " = HeapAllocZ(" + curr.size() + ")");
        // Set to class pointer
        newClass.add(indent() + "[" + currVar + "] = :vmt_" + currentClass);
        return newClass;
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
        LinkedList<String> not = new LinkedList<>();
        LinkedList<String> expr = n.f1.accept(this, argu);
        String exprVal = getVal(expr, not);
        // Subtraction will negate the boolean expression
        not.add(indent() + createTemp() + " = Sub(" + exprVal + " 1)");
        return not;
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
