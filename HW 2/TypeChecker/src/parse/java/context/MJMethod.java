package context;

import java.util.HashSet;
import java.util.Set;

public class MJMethod {
    public Set<MJType> params = new HashSet<>();
    public Set<MJType> vars = new HashSet<>();
    private String methodName;

    public MJMethod(String name) {
        this.methodName = name;
    }
    public MJMethod(String name, Set<MJType> params, Set<MJType> vars) {
        this.methodName = name;
        this.params = params;
        this.vars = vars;
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
}
