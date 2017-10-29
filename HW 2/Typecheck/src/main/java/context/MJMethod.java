package context;

import java.util.LinkedHashSet;
import java.util.Set;

public class MJMethod {
    public Set<MJType> params = new LinkedHashSet<>();
    public Set<MJType> vars = new LinkedHashSet<>();
    private String methodName;
    private MJType returnType;

    public MJMethod(String name, MJType returnType) {
        this.methodName = name;
        this.returnType = returnType;
    }
    public MJMethod(String name, Set<MJType> params, Set<context.MJType> vars) {
        this.methodName = name;
        this.params = params;
        this.vars = vars;
    }

    public MJType findInLocals(MJType var) {
        return find(var, vars);
    }
    public MJType findInParams(MJType var) {
        return find(var, params);
    }

    public MJType find(MJType var, Set<MJType> collection) {
        MJType foundVar = null;
        for(MJType variable : collection) {
            if (var.equals(variable)) {
                foundVar = variable;
                break;
            }
        }
        return foundVar;
    }

    public MJType getReturnType() {
        return returnType;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void printMJMethod() {
        System.out.println("Methodname: " + methodName);
        returnType.printMJType();
        System.out.println("PARAMETERS");
        for(MJType mjType : params) {
            mjType.printMJType();
        }
        System.out.println("LOCAL VARIABLES");
        for(MJType mjType : vars) {
            mjType.printMJType();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if ((o instanceof MJMethod)) {
            return this.methodName.equals(((MJMethod) o).methodName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 17;
        return prime * (result + methodName.hashCode());
    }
}
