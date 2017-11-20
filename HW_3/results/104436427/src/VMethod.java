import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * VMethod represents the idea of a method as in MiniJava, holding all
 * its local variables, parameters and type associations. It is
 * used for ease of scoping.
 */
public class VMethod {
    public String methodName;
    public List<String> params = new ArrayList<>();
    public List<String> locals = new ArrayList<>();
    public HashMap<String, String> localTypes = new HashMap<>();
    public HashMap<String, String> paramTypes = new HashMap<>();
    public String returnType;

    public VMethod(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Debugging function for pretty printing the method
     */
    public void printMethod() {
        System.out.println("METHOD NAME: " + methodName);
        for(int i = 0; i < params.size(); i++) {
            System.out.println("PARAM " + (i+1) + ": " + params.get(i));
        }
        for(int i = 0; i < locals.size(); i++) {
            System.out.println("LOCAL " + (i+1) + ": " + locals.get(i));
        }
    }

    // Equality for a method is based on its name in this project
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if ((o instanceof VMethod)) {
            return methodName.equals(((VMethod) o).methodName);
        }
        return false;
    }

    // Hashing for a method is based on its name in this project
    @Override
    public int hashCode() {
        return 31 * this.methodName.hashCode();
    }
}
