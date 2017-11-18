import java.util.ArrayList;
import java.util.List;

public class VMethod {
    public String methodName;
    public List<String> params = new ArrayList<>();
    public List<String> locals = new ArrayList<>();

    public VMethod(String methodName) {
        this.methodName = methodName;
    }

    public VMethod(String methodName, List<String> params, List<String> locals) {
        this.methodName = methodName;
        this.params = params;
        this.locals = locals;
    }

    public void printMethod() {
        System.out.println("METHOD NAME: " + methodName);
        for(int i = 0; i < params.size(); i++) {
            System.out.println("PARAM " + (i+1) + ": " + params.get(i));
        }
        for(int i = 0; i < locals.size(); i++) {
            System.out.println("LOCAL " + (i+1) + ": " + locals.get(i));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if ((o instanceof VMethod)) {
            return methodName.equals(((VMethod) o).methodName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * this.methodName.hashCode();
    }
}
