package context;

import syntaxtree.*;
import syntaxtree.Identifier;
import visitor.GJDepthFirst;

import java.text.ParseException;

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
    MJType ret = null;
    for(Node statement: n.f15.nodes)
      ret = statement.accept(this, argu);
    return ret;
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
  public MJType visit(ClassDeclaration n, ContextTable argu) {
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
  public MJType visit(ClassExtendsDeclaration n, ContextTable argu) {
    return null;
  }

  /**
   * f0 -> MJType()
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
   * f1 -> MJType()
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
  public MJType visit(MethodDeclaration n, ContextTable argu) {
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
   * f0 -> MJType()
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
      MJType ret = null;
      for(Node statement : n.f1.nodes) {
          ret = statement.accept(this, argu);
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
        MJClass curr = argu.getCurrentClass();
        MJType identifier = n.f0.accept(this, argu);
        MJType matchedIdentifier = null;
        if(curr.getMRUMethod().vars.contains(identifier)) {
            for (MJType variable : curr.getMRUMethod().vars) {
                if (variable.equals(identifier)) {
                    matchedIdentifier = variable;
                    break;
                }
            }
        } else if (curr.getMRUMethod().params.contains(identifier)) {
            for(MJType parameter : curr.getMRUMethod().params) {
                if (parameter.equals(identifier)) {
                    matchedIdentifier = parameter;
                    break;
                }
            }
        } else if (curr.getFields().contains(identifier)) {
            for (MJType field : curr.getFields()) {
                if (field.equals(identifier)) {
                    matchedIdentifier = field;
                    break;
                }
            }
        } else {
            throw new MJTypeCheckException("Invalid assignment: identifier not found");
        }
        if (!matchedIdentifier.equals(n.f2.accept(this, argu))) {
            throw new MJTypeCheckException("Invalid assignment: invalid expression type");
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
    public MJType visit(ArrayAssignmentStatement n, ContextTable argu) {
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
    public MJType visit(IfStatement n, ContextTable argu) {
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
    public MJType visit(WhileStatement n, ContextTable argu) {
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
    public MJType visit(PrintStatement n, ContextTable argu) {
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
        if (n.f0.accept(this, argu).equals(n.f2.accept(this, argu))) {
            return new MJType(null, MJType.Type.BOOLEAN);
        } else {
            throw new MJTypeCheckException("Invalid expression: both values are not boolean");
        }
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
        if (n.f0.accept(this, argu).equals(n.f2.accept(this, argu))) {
            return new MJType(null, MJType.Type.INT);
        } else {
            throw new MJTypeCheckException("Invalid expression: both values are not integers");
        }
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
        if (n.f0.accept(this, argu).equals(n.f2.accept(this, argu))) {
            return new MJType(null, MJType.Type.INT);
        } else {
            throw new MJTypeCheckException("Invalid expression: both values are not integers");
        }
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
        if (n.f0.accept(this, argu).equals(n.f2.accept(this, argu))) {
            return new MJType(null, MJType.Type.INT);
        } else {
            throw new MJTypeCheckException("Invalid expression: both values are not integers");
        }
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
        if (n.f0.accept(this, argu).equals(n.f2.accept(this, argu))) {
            return new MJType(null, MJType.Type.INT);
        } else {
            throw new MJTypeCheckException("Invalid expression: both values are not integers");
        }
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
        // Arrays are only of type int[], thus must return an int
        if (n.f0.accept(this, argu).getType() == MJType.Type.ARRAY &&
                n.f2.accept(this, argu).getType() == MJType.Type.INT) {
            return new MJType(null, MJType.Type.INT);
        } else {
            throw new MJTypeCheckException("Invalid array lookup");
        }
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
        if (n.f0.accept(this, argu).getType() == MJType.Type.ARRAY)
            return new MJType(null, MJType.Type.INT);
        else
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
    public MJType visit(MessageSend n, ContextTable argu) {
        if (n.f0.accept(this, argu).getType() == MJType.Type.IDENT) {
            MJMethod callingMethod = argu.
                    getCurrentClass().
                    getClassMethod(n.f2.accept(this, argu).getName());
            if (callingMethod != null) {
                // Check each param with its matching type - with MRU method
                n.f4.accept(this, argu);
                // Return method return type with method name
                return callingMethod.getReturnType();
            } else {
                throw new MJTypeCheckException("Cannot find matching method name");
            }
        } else {
            throw new MJTypeCheckException("Cannot call a method on non-identifier");
        }
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
        return new MJType(n.f0.toString(), MJType.Type.IDENT);
    }

    /**
     * f0 -> "this"
     *
     * @param n
     * @param argu
     */
    @Override
    public MJType visit(ThisExpression n, ContextTable argu) {
        return new MJType(argu.getCurrentClass().getClassName(), MJType.Type.IDENT);
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
    public MJType visit(ArrayAllocationExpression n, ContextTable argu) {
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
    public MJType visit(AllocationExpression n, ContextTable argu) {
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
    public MJType visit(NotExpression n, ContextTable argu) {
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
    public MJType visit(BracketExpression n, ContextTable argu) {
        return null;
    }
}
