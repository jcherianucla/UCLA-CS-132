package context;

import syntaxtree.*;
import visitor.GJVoidDepthFirst;

import java.util.HashSet;
import java.util.Set;

public class ContextVisitor extends GJVoidDepthFirst<MJClass> {

    public ContextTable context = new ContextTable();


    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     *
     * @param n
     * @param argu
     */
    @Override
    public void visit(Goal n, MJClass argu) {
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
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
    public void visit(MainClass n, MJClass argu) {
        String className = n.f1.f0.toString();
        MJClass mainClass = new MJClass(className);
        mainClass.isMain = true;
        MJType mainParam = new MJType(n.f11.f0.toString(), MJType.Type.OTHER);
        MJMethod mainMethod = new MJMethod("main", MJType.Type.OTHER.ordinal());
        mainMethod.params.add(mainParam);
        mainClass.addMethod(mainMethod);
        context.addClass(mainClass);
        // Add local variables to main method for main class
        for(Node node : n.f14.nodes) {
            node.accept(this, mainClass);
        }
    }

    /**
     * f0 -> ClassDeclaration()
     * | ClassExtendsDeclaration()
     *
     * @param n
     * @param argu
     */
    @Override
    public void visit(TypeDeclaration n, MJClass argu) {
        n.f0.accept(this, argu);
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
    public void visit(ClassDeclaration n, MJClass argu) throws MJTypeCheckException {
        String className = n.f1.f0.toString();
        if (context.getClasses().get(className) != null) {
            throw new MJTypeCheckException("Duplicate class names");
        }
        MJClass newClass = new MJClass(className);
        context.addClass(newClass);
        // Add fields to class
        for(Node node : n.f3.nodes) {
            node.accept(this, newClass);
        }
        // Add methods to class
        for(Node node : n.f4.nodes) {
            node.accept(this, newClass);
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
     * @param argu
     */
    @Override
    public void visit(ClassExtendsDeclaration n, MJClass argu) throws MJTypeCheckException {
        String className = n.f1.f0.toString();
        MJClass childClass = new MJClass(className);
        String baseClassName = n.f3.f0.toString();
        MJClass baseClass = context.getClasses().getOrDefault(baseClassName, new MJClass(baseClassName));
        childClass.setParent(baseClass);
        context.addClass(baseClass);
        context.addClass(childClass);
        // Add fields to class
        for(Node node : n.f5.nodes) {
            node.accept(this, childClass);
        }
        // Add methods to class
        for(Node node : n.f6.nodes) {
            node.accept(this, childClass);
        }
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
    public void visit(VarDeclaration n, MJClass argu) throws MJTypeCheckException {
        MJType variable = new MJType(n.f1.f0.toString(), MJType.Type.fromInteger(n.f0.f0.which));
        // No methods --> Still looking through fields
        if(argu.getClassMethods().isEmpty()) {
            if(!argu.fields.add(variable)) {
                throw new MJTypeCheckException("Duplicate field name");
            }
        } else {
            if(!argu.getMRUMethod().vars.add(variable)) {
                throw new MJTypeCheckException("Duplicate local variable");
            }
        }
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
    public void visit(MethodDeclaration n, MJClass argu) throws MJTypeCheckException {
        String methodName = n.f2.f0.toString();
        MJMethod method = new MJMethod(methodName, n.f1.f0.which);
        if(argu.getClassMethods().get(methodName) != null) {
            throw new MJTypeCheckException("Duplicate method declaration");
        } else {
            argu.addMethod(method);
            // Add parameters to method
            n.f4.accept(this, argu);
            // Add variables to most recently used method
            for(Node node : n.f7.nodes) {
                node.accept(this, argu);
            }
        }


    }

    /**
     * f0 -> FormalParameter()
     * f1 -> ( FormalParameterRest() )*
     *
     * @param n
     * @param argu
     */
    @Override
    public void visit(FormalParameterList n, MJClass argu) {
        n.f0.accept(this, argu);
        for(Node node : n.f1.nodes) {
            node.accept(this, argu);
        }
    }

    /**
     * f0 -> MJType()
     * f1 -> Identifier()
     *
     * @param n
     * @param argu
     */
    @Override
    public void visit(FormalParameter n, MJClass argu) throws MJTypeCheckException {
        MJType param = new MJType(n.f1.f0.toString(), MJType.Type.fromInteger(n.f0.f0.which));
        if(!argu.getMRUMethod().params.add(param)) {
            throw new MJTypeCheckException("Duplicate parameter");
        }
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     *
     * @param n
     * @param argu
     */
    @Override
    public void visit(FormalParameterRest n, MJClass argu) {
        n.f1.accept(this, argu);
    }
}
