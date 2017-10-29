package context;

import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * TypecheckVisitor runs the second pass through the JTB generated Abstract
 * Syntax Tree. It utilizes the ContextTable built upon first pass to relate
 * everything to scoped contexts. For speed of development, we throw
 * runtime exceptions for type errors (alternatively could be done using Optional<MJType>)
 *
 * In all instances, n represents the Node we are at in the AST and argu is our
 * global context table. We return MJType for being able to pass around expression types.
 */
public class TypecheckVisitor extends GJDepthFirst<MJType, ContextTable> {

  /**
   * f0 -> MainClass()
   * f1 -> ( TypeDeclaration() )*
   * f2 -> <EOF>
   *
   * @param n
   * @param argu
   */
  @Override
  public MJType visit(Goal n, ContextTable argu) {
    // Check for cycles
    if (!argu.acyclic()) {
      throw new MJTypeCheckException("Inheritance cycle found");
    }
    // Check Main Class
    MJType ret = n.f0.accept(this, argu);
    // Check through all classes
    for(Node _class: n.f1.nodes) {
      ret = _class.accept(this, argu);
    }
    return ret;
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
  public MJType visit(MainClass n, ContextTable argu) {
      String mainClassName = n.f1.accept(this, argu).getName();
      argu.getClass(mainClassName);
      MJClass currentClass = argu.getCurrentClass();
      currentClass.isMain = true;
      currentClass.callingMethodStack.push(currentClass.getClassMethod("main"));
      for (Node statement: n.f15.nodes) {
          statement.accept(this, argu);
      }
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
    public MJType visit(TypeDeclaration n, ContextTable argu) {
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
    public MJType visit(ClassDeclaration n, ContextTable argu) throws MJTypeCheckException {
        MJClass currClass = argu.getClass(n.f1.accept(this, argu).getName());
        if (currClass == null) {
            throw new MJTypeCheckException("Could not find requested class");
        }
        MJType ret = null;
        // Check through methods
        for (Node method : n.f4.nodes)
            ret = method.accept(this, argu);
        return ret;
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
    public MJType visit(ClassExtendsDeclaration n, ContextTable argu) throws MJTypeCheckException {
        MJClass currClass = argu.getClass(n.f1.accept(this, argu).getName());
        MJClass parentClass = argu.getClass(n.f3.accept(this, argu).getName());
        if (currClass == null || parentClass == null) {
            throw new MJTypeCheckException("Could not find requested class");
        }
        // Ensure no overloading
        for (MJMethod method : currClass.getAllMethods()) {
            if (!argu.noOverloading(currClass, parentClass, method.getMethodName()))
                throw new MJTypeCheckException("Overloading found");
        }
        MJType ret = null;
        // Check through methods
        for (Node method : n.f6.nodes) {
            ret = method.accept(this, argu);
        }
        return ret;
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
    public MJType visit(VarDeclaration n, ContextTable argu) {
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
    public MJType visit(MethodDeclaration n, ContextTable argu) throws MJTypeCheckException {
        // Get the name of the method
        String currentMethodName = n.f2.accept(this, argu).getName();
        // Find the method in the context of the current class
        MJClass currentClass = argu.getCurrentClass();
        MJMethod currentMethod = currentClass.getClassMethod(currentMethodName);
        // Declaration so start call stack
        currentClass.callingMethodStack.clear();
        currentClass.callingMethodStack.push(currentMethod);
        // Get the return type of the method
        MJType returnExpression = n.f10.accept(this, argu);
        boolean found = false;
        if (returnExpression.getType() == MJType.Type.IDENT) {
            // Check through class context
            if (currentClass.findVarInFields(returnExpression) != null) {
                found = true;
                returnExpression = currentClass.findVarInFields(returnExpression);
            }
            // Check through parameters
            if (currentMethod.findInParams(returnExpression) != null) {
                found = true;
                returnExpression = currentMethod.findInParams(returnExpression);
            }
            // Check through local variables - most recent scope
            if (currentMethod.findInLocals(returnExpression) != null) {
                found = true;
                returnExpression = currentMethod.findInLocals(returnExpression);
            }
            if (!found) {
                throw new MJTypeCheckException("Could not find return identifier");
            }
        }
        if (returnExpression.getType() != currentMethod.getReturnType().getType()) {
            throw new MJTypeCheckException("Invalid return type");
        }
        // Complete all statements in method
        for (Node statement : n.f8.nodes) {
            statement.accept(this, argu);
        }
        // We are done with the method, so clear the stack
        currentClass.callingMethodStack.pop();
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
    public MJType visit(FormalParameterList n, ContextTable argu) {
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
    public MJType visit(FormalParameter n, ContextTable argu) {
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
    public MJType visit(FormalParameterRest n, ContextTable argu) {
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
    public MJType visit(Type n, ContextTable argu) {
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
    public MJType visit(ArrayType n, ContextTable argu) {
        return null;
    }

    /**
     * f0 -> "boolean"
     *
     * @param n
     * @param argu
     */
    @Override
    public MJType visit(BooleanType n, ContextTable argu) {
        return null;
    }

    /**
     * f0 -> "int"
     *
     * @param n
     * @param argu
     */
    @Override
    public MJType visit(IntegerType n, ContextTable argu) {
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
    public MJType visit(Statement n, ContextTable argu) {
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
    public MJType visit(Block n, ContextTable argu) {
        // Type check the statements in block
        for(Node statement : n.f1.nodes) {
            statement.accept(this, argu);
        }
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
    public MJType visit(AssignmentStatement n, ContextTable argu) {
        MJClass currentClass = argu.getCurrentClass();
        MJType identifier = n.f0.accept(this, argu);
        // Identifier must exist in the top level method or in class
        MJMethod topMethod = currentClass.callingMethodStack.get(0);
        boolean foundRHS = false;
        boolean foundLHS = false;
        MJType matchedIdentifier = n.f2.accept(this, argu);
        // Look through class fields again for reassurance
        if (currentClass.findVarInFields(matchedIdentifier) != null) {
            foundRHS = true;
            matchedIdentifier = currentClass.findVarInFields(matchedIdentifier);
        }
        if (currentClass.findVarInFields(identifier) != null) {
            foundLHS= true;
            identifier = currentClass.findVarInFields(identifier);
        }
        // Look through method params again for reassurance
        if (topMethod.findInParams(matchedIdentifier) != null) {
            foundRHS = true;
            matchedIdentifier = topMethod.findInParams(matchedIdentifier);
        }
        if (topMethod.findInParams(identifier) != null) {
            foundLHS = true;
            identifier = topMethod.findInParams(identifier);
        }
        // Look through method local variables again for reassurance
        if (topMethod.findInLocals(matchedIdentifier) != null) {
            foundRHS = true;
            matchedIdentifier = topMethod.findInLocals(matchedIdentifier);
        }
        if (topMethod.findInLocals(identifier) != null) {
            foundLHS = true;
            identifier = topMethod.findInLocals(identifier);
        }
        // Couldn't find a match
        if (foundRHS && foundLHS) {
            if (identifier.getType() != matchedIdentifier.getType()) {
                throw new MJTypeCheckException("Incompatible type assignment");
            }
        } else {
            throw new MJTypeCheckException("Invalid assignment: identifier not found");
        }
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
    public MJType visit(ArrayAssignmentStatement n, ContextTable argu) throws MJTypeCheckException {
        MJType array = n.f0.accept(this, argu);
        MJType arrayIndex = n.f2.accept(this, argu);
        MJType arrayValue = n.f5.accept(this, argu);
        // Has to be of type array with ints for the index and value
        if (array.getType() == MJType.Type.ARRAY
                && arrayIndex.getType() == MJType.Type.INT
                && arrayValue.getType() == MJType.Type.INT) {
            return null;
        }
        throw new MJTypeCheckException("Invalid array assignment");
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
    public MJType visit(IfStatement n, ContextTable argu) throws MJTypeCheckException {
        MJType conditionalExpression = n.f2.accept(this, argu);
        // Conditional has to be boolean
        if (conditionalExpression.getType() == MJType.Type.BOOLEAN) {
            // Type check both statements
            n.f4.accept(this, argu);
            n.f6.accept(this, argu);
            return null;
        }
        throw new MJTypeCheckException("Need a boolean within if/else statement");
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
    public MJType visit(WhileStatement n, ContextTable argu) throws MJTypeCheckException {
        MJType conditionalExpression = n.f2.accept(this, argu);
        // Conditional has to be boolean
        if (conditionalExpression.getType() != MJType.Type.BOOLEAN)
            throw new MJTypeCheckException("Need a boolean within while statement");
        // Type check following statement
        return n.f4.accept(this, argu);
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
    public MJType visit(PrintStatement n, ContextTable argu) throws MJTypeCheckException {
        MJType printExpression = n.f2.accept(this, argu);
        // Print expression must be of type int for successful printing
        if (printExpression.getType() != MJType.Type.INT)
            throw new MJTypeCheckException("Need an integer for printing");
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
    public MJType visit(Expression n, ContextTable argu) {
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
    public MJType visit(AndExpression n, ContextTable argu) throws MJTypeCheckException {
        MJType lvalue = n.f0.accept(this, argu);
        MJType rvalue = n.f2.accept(this, argu);
        // Both sides of the binary expression must be boolean
        if (lvalue.getType() == MJType.Type.BOOLEAN && rvalue.getType() == MJType.Type.BOOLEAN) {
            return new MJType(null, MJType.Type.BOOLEAN);
        }
        throw new MJTypeCheckException("Invalid expression: both values are not boolean");
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
    public MJType visit(CompareExpression n, ContextTable argu) {
        MJType lvalue = n.f0.accept(this, argu);
        MJType rvalue = n.f2.accept(this, argu);
        // Both sides of the binary expression must be ints
        if (lvalue.getType() == MJType.Type.INT && rvalue.getType() == MJType.Type.INT) {
            return new MJType(null, MJType.Type.BOOLEAN);
        }
        throw new MJTypeCheckException("Invalid expression: both values are not integers");
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
    public MJType visit(PlusExpression n, ContextTable argu) {
        MJType lvalue = n.f0.accept(this, argu);
        MJType rvalue = n.f2.accept(this, argu);
        // Both sides of the binary expression must be ints
        if (lvalue.getType() == MJType.Type.INT && rvalue.getType() == MJType.Type.INT) {
            return new MJType(null, MJType.Type.INT);
        }
        throw new MJTypeCheckException("Invalid expression: both values are not integers");
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
    public MJType visit(MinusExpression n, ContextTable argu) {
        MJType lvalue = n.f0.accept(this, argu);
        MJType rvalue = n.f2.accept(this, argu);
        // Both sides of the binary expression must be ints
        if (lvalue.getType() == MJType.Type.INT && rvalue.getType() == MJType.Type.INT) {
            return new MJType(null, MJType.Type.INT);
        }
        throw new MJTypeCheckException("Invalid expression: both values are not integers");
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
    public MJType visit(TimesExpression n, ContextTable argu) {
        MJType lvalue = n.f0.accept(this, argu);
        MJType rvalue = n.f2.accept(this, argu);
        // Both sides of the binary expression must be ints
        if (lvalue.getType() == MJType.Type.INT && rvalue.getType() == MJType.Type.INT) {
            return new MJType(null, MJType.Type.INT);
        }
        throw new MJTypeCheckException("Invalid expression: both values are not integers");
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
    public MJType visit(ArrayLookup n, ContextTable argu) throws MJTypeCheckException {
        MJType array = n.f0.accept(this, argu);
        MJType arrayValue = n.f2.accept(this, argu);
        // Array lookup must act on an array with an int based index
        if (array.getType() == MJType.Type.ARRAY && arrayValue.getType() == MJType.Type.INT) {
            return new MJType(null, MJType.Type.INT);
        }
        throw new MJTypeCheckException("Invalid array lookup");
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
    public MJType visit(ArrayLength n, ContextTable argu) throws MJTypeCheckException {
        MJType array = n.f0.accept(this, argu);
        if (array.getType() == MJType.Type.ARRAY)
            return new MJType(null, MJType.Type.INT);
        throw new MJTypeCheckException("Invalid array length");
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
    public MJType visit(MessageSend n, ContextTable argu) throws MJTypeCheckException {
        // Can only work on defined class instances
        MJType classInstance = n.f0.accept(this, argu);
        if (!classInstance.hasSubtype()) {
            System.out.println(classInstance.getSubtype());
            throw new MJTypeCheckException("Method can only be called on a valid class instance");
        }
        MJClass currentClass = argu.getCurrentClass();
        // This sets the most recent class to be the instantiated one
        MJClass instanceClassType = argu.getClass(classInstance.getSubtype());
        String methodName = n.f2.accept(this, argu).getName();
        // Find the method based on this name in the instance class type
        MJMethod calledMethod = instanceClassType.getClassMethod(methodName);
        if (calledMethod == null) {
            throw new MJTypeCheckException("Invalid method call on given class");
        }
        // We are within a method declaration so push onto call stack
        currentClass.callingMethodStack.push(calledMethod);
        if (n.f4 == null) {
            // No arguments supplied only valid if original method had no parameters
            if (calledMethod.params.size() != 0) {
                throw new MJTypeCheckException("Incorrect number of arguments supplied");
            }
        } else {
            // Check each parameter with its matching type in the called method
            n.f4.accept(this, argu);
        }
        // Reset the current class
        argu.setCurrentClass(currentClass);
        // Remove the method from the classes call stack
        argu.getCurrentClass().callingMethodStack.pop();
        return calledMethod.getReturnType();
    }

    /**
     * f0 -> Expression()
     * f1 -> ( ExpressionRest() )*
     *
     * @param n
     * @param argu
     */
    @Override
    public MJType visit(ExpressionList n, ContextTable argu) throws MJTypeCheckException {
        // Convert params to a List so as to preserve ordering and retrieve in O(1)
        Set<MJType> params = null;
        MJClass currentClass = argu.getCurrentClass();
        if (!currentClass.callingMethodStack.empty()) {
            params = currentClass.callingMethodStack.peek().params;
        } else {
            params = currentClass.getMRUMethod().params;
        }
        List<MJType> methodParams = new ArrayList<>(params);
        if (!methodParams.isEmpty()) {
            if (methodParams.size() != 1 + n.f1.size()) {
                throw new MJTypeCheckException("Incorrect number of arguments supplied");
            }
            // Compare first argument
            if (methodParams.get(0).equals(n.f0.accept(this, argu))) {
                int i = 1;
                // Compare rest if they exist
                for(Node expression : n.f1.nodes) {
                    if (!methodParams.get(i).equals(expression.accept(this, argu))) {
                        throw new MJTypeCheckException("Invalid expression to parameter match");
                    }
                    i++;
                }
            }
        }
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
    public MJType visit(ExpressionRest n, ContextTable argu) {
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
    public MJType visit(PrimaryExpression n, ContextTable argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     *
     * @param n
     * @param argu
     */
    @Override
    public MJType visit(IntegerLiteral n, ContextTable argu) {
        return new MJType(n.f0.toString(), MJType.Type.INT);
    }

    /**
     * f0 -> "true"
     *
     * @param n
     * @param argu
     */
    @Override
    public MJType visit(TrueLiteral n, ContextTable argu) {
        return new MJType(n.f0.toString(), MJType.Type.BOOLEAN);
    }

    /**
     * f0 -> "false"
     *
     * @param n
     * @param argu
     */
    @Override
    public MJType visit(FalseLiteral n, ContextTable argu) {
        return new MJType(n.f0.toString(), MJType.Type.BOOLEAN);
    }

    /**
     * f0 -> <IDENTIFIER>
     *
     * @param n
     * @param argu
     */
    @Override
    public MJType visit(Identifier n, ContextTable argu) {
        String identifierName = n.f0.toString();
        MJClass currentClass = argu.getCurrentClass();
        // The identifier already exists
        // in the current class' field set
        for(MJType field : currentClass.getFields()) {
            if(field.getName().equals(identifierName)) {
                return field;
            }
        }
        if(!currentClass.callingMethodStack.empty()) {
            MJMethod currMethod = currentClass.callingMethodStack.get(0);
            // in the current called method's parameters
            for (MJType var : currMethod.vars) {
                if (var.getName().equals(identifierName)) {
                    return var;
                }
            }
            // or local variables
            for (MJType param : currMethod.params) {
                if (param.getName().equals(identifierName)) {
                    return param;
                }
            }
        }
        // Otherwise it is a new assignment
        MJType newIdentifier = new MJType(identifierName, MJType.Type.IDENT);
        return newIdentifier;
    }

    /**
     * f0 -> "this"
     *
     * @param n
     * @param argu
     */
    @Override
    public MJType visit(ThisExpression n, ContextTable argu) throws MJTypeCheckException {
        MJClass currentClass = argu.getCurrentClass();
        String className = currentClass.getClassName();
        if (currentClass.isMain) {
            throw new MJTypeCheckException("Cannot reference this from static context");
        }
        return new MJType(n.f0.toString(), MJType.Type.IDENT, className);
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
    public MJType visit(ArrayAllocationExpression n, ContextTable argu) throws MJTypeCheckException {
        MJType arrayIndex = n.f3.accept(this, argu);
        // The array index must be of type int
        if (arrayIndex.getType() != MJType.Type.INT)
            throw new MJTypeCheckException("Cannot allocate a non integer amount");
        return new MJType(null, MJType.Type.ARRAY);
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
    public MJType visit(AllocationExpression n, ContextTable argu) {
        MJType newIdentifier = n.f1.accept(this, argu);
        // For a new class the subtype is the same as the class name
        newIdentifier.setSubtype(newIdentifier.getName());
        return newIdentifier;
    }

    /**
     * f0 -> "!"
     * f1 -> Expression()
     *
     * @param n
     * @param argu
     */
    @Override
    public MJType visit(NotExpression n, ContextTable argu) throws MJTypeCheckException {
        MJType expression = n.f1.accept(this, argu);
        // Logical NOT must be applied on a boolean
        if (expression.getType() != MJType.Type.BOOLEAN)
            throw new MJTypeCheckException("Logical NOT can only apply on booleans");
        return expression;
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
    public MJType visit(BracketExpression n, ContextTable argu) {
        return n.f1.accept(this, argu);
    }
}
