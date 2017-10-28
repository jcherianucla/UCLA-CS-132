package context;

import java.util.HashSet;
import java.util.Set;

public class MJMethod {
    public Set<MJType> params = new HashSet<>();
    public Set<MJType> vars = new HashSet<>();
    private String methodName;
    private MJType.Type returnType;

    public MJMethod(String name, int returnTypeChoice) {
        this.methodName = name;
        this.returnType = MJType.Type.fromInteger(returnTypeChoice);
    }
    public MJMethod(String name, Set<MJType> params, Set<context.MJType> vars) {
        this.methodName = name;
        this.params = params;
        this.vars = vars;
    }

    public MJType getReturnType() {
        return new MJType(methodName, returnType);
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void printMJMethod() {
        System.out.println("Methodname: " + methodName + " returns: " + returnType.toString());
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
