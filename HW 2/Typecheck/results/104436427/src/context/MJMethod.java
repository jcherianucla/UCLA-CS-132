package context;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * MJMethod represents a MiniJava Method type that houses
 * parameters, local variables and the return type of any
 * method declared in MiniJava. It can be thought of as the wrapper
 * for all method specific scopes.
 */
public class MJMethod {
    // All the method parameters in an ordered set
    public Set<MJType> params = new LinkedHashSet<>();
    // All the method local variables in an ordered set
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

    /**
     * Find the instance of the variable amongst the local variables
     * @param var Variable we're looking for
     * @return The variable type if it exists
     */
    public MJType findInLocals(MJType var) {
        return find(var, vars);
    }

    /**
     * Find the instance of the variable amongst the parameters
     * @param var Variable we're looking for
     * @return The variable type if it exists
     */
    public MJType findInParams(MJType var) {
        return find(var, params);
    }

    /**
     * General find function amongst sets - treating them as arrays
     * @param var Variable we're looking for
     * @param collection The set to search through
     * @return The variable type if it exists
     */
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

    /**
     * Convenience function for debugging
     */
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
}
