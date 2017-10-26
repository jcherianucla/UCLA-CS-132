package context;

import syntaxtree.*;
import visitor.GJVoidDepthFirst;

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
        MJMethod mainMethod = new MJMethod("main");
        mainMethod.params.add(mainParam);
        mainClass.addMethod(mainMethod);
        context.classes.put(className, mainClass);
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
        if (context.classes.get(className) != null) {
            throw new MJTypeCheckException("Duplicate class names");
        }
        MJClass newClass = new MJClass(className);
        context.classes.put(className, newClass);
        context.currentClass = newClass;
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
    public void visit(ClassExtendsDeclaration n, MJClass argu) {

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
    public void visit(MethodDeclaration n, MJClass argu) {

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

    }

    /**
     * f0 -> MJType()
     * f1 -> Identifier()
     *
     * @param n
     * @param argu
     */
    @Override
    public void visit(FormalParameter n, MJClass argu) {

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

    }
}
