import syntaxtree.*;
import visitor.DepthFirstVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * ContextVisitor is a DFS based visitor running through the MiniJava Parsed
 * AST to assign scoping of classes and methods with their variable types.
 */
public class ContextVisitor extends DepthFirstVisitor{
    // Actual context
    public Map<String, VClass> classes = new HashMap<>();
    // State for current class to look through
    private VClass currentClass;
    // State for current method to look through
    private VMethod currentMethod;
    // Add to member variables or method variables
    private boolean classVar = true;

    /**
     * Gets the string representation of the type for a Node
     * @param type Type Node
     * @return string representation
     */
    private String getType(Type type) {
        switch (type.f0.which) {
            case 0:
                return "int[]";
            case 1:
                return "boolean";
            case 2:
                return "int";
            case 3:
                return ((Identifier)type.f0.choice).f0.toString();
            default:
                return null;
        }
    }

    /**
     * Debugging function to view the context
     */
    public void printContext() {
        for(VClass _class : classes.values()) {
            _class.printClass();
        }
    }

    /**
     * Prints out the Virtual Method Table
     */
    public void printVMTs() {
        for(VClass _class : classes.values()) {
            //if (!_class.className.equals("main"))
            _class.printVMT();
        }
    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     *
     * @param n
     */
    @Override
    public void visit(Goal n) {
        n.f0.accept(this);
        for(Node _class : n.f1.nodes) {
            _class.accept(this);
        }
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
     */
    @Override
    public void visit(MainClass n) {
        String className = n.f1.f0.toString();
        VMethod mainMethod = new VMethod("main");
        VClass main = new VClass(className);
        main.methods.add(mainMethod);
        classes.put(className, main);
        classVar = false;
        currentClass = main;
        currentMethod = mainMethod;
        for(Node _var : n.f14.nodes) {
            _var.accept(this);
        }
    }

    /**
     * f0 -> ClassDeclaration()
     * | ClassExtendsDeclaration()
     *
     * @param n
     */
    @Override
    public void visit(TypeDeclaration n) {
        n.f0.accept(this);
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
     */
    @Override
    public void visit(ClassDeclaration n) {
        String className = n.f1.f0.toString();
        // Work on dummy if it exists
        VClass curr = classes.get(className) != null ? classes.get(className) : new VClass(className);
        classes.put(className, curr);
        currentClass = curr;
        classVar = true;
        for(Node _members : n.f3.nodes) {
            _members.accept(this);
        }
        classVar = false;
        for(Node _methods : n.f4.nodes) {
            _methods.accept(this);
        }
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
     */
    @Override
    public void visit(ClassExtendsDeclaration n) {
        String currClassName = n.f1.f0.toString();
        String parentClassName = n.f3.f0.toString();
        VClass curr = classes.get(currClassName) != null ? classes.get(currClassName) : new VClass(currClassName);
        classes.put(currClassName, curr);
        currentClass = curr;
        if(classes.get(parentClassName) != null) {
            // Get real parent
            curr.setParent(classes.get(parentClassName));
        } else {
            // Store dummy parent
            VClass parent = new VClass(parentClassName);
            classes.put(parentClassName, parent);
            curr.setParent(parent);
        }
        classVar = true;
        for(Node _members : n.f5.nodes) {
            _members.accept(this);
        }
        classVar = false;
        for(Node _methods : n.f6.nodes) {
            _methods.accept(this);
        }
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     *
     * @param n
     */
    @Override
    public void visit(VarDeclaration n) {
        String variableName = n.f1.f0.toString();
        if(classVar) {
            currentClass.members.add(variableName);
            currentClass.types.put(variableName, getType(n.f0));
        } else {
            currentMethod.locals.add(variableName);
            currentMethod.localTypes.put(variableName, getType(n.f0));
        }
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
     */
    @Override
    public void visit(MethodDeclaration n) {
        String methodName = n.f2.f0.toString();
        VMethod curr = new VMethod(methodName);
        currentClass.methods.add(curr);
        currentMethod = curr;
        currentMethod.returnType = getType(n.f1);
        n.f4.accept(this);
        for(Node _local : n.f7.nodes) {
            _local.accept(this);
        }
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> ( FormalParameterRest() )*
     *
     * @param n
     */
    @Override
    public void visit(FormalParameterList n) {
        n.f0.accept(this);
        for(Node _param : n.f1.nodes) {
            _param.accept(this);
        }
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     *
     * @param n
     */
    @Override
    public void visit(FormalParameter n) {
        String paramName = n.f1.f0.toString();
        currentMethod.params.add(paramName);
        currentMethod.paramTypes.put(paramName, getType(n.f0));
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     *
     * @param n
     */
    @Override
    public void visit(FormalParameterRest n) {
        n.f1.accept(this);
    }
}
