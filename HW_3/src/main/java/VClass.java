import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VClass {
    private VClass parent = null;
    public String className;
    public List<String> members = new ArrayList<>();
    public HashMap<String, String> types = new HashMap<>();
    public List<VMethod> methods = new ArrayList<>();

    public VClass(String className) {
        this.className = className;
    }
    public VClass(String className, List<VMethod> methods, List<String> members) {
        this.className = className;
        this.members = members;
        this.methods = methods;
    }

    /**
     * Gives the byte size of a class. The pointer to the class
     * itself is 4 bytes, so its always with a 4 byte offset.
     * @return byte size as multiples of 4
     */
    public int size() {
        return 4 + 4*getMembers().size();
    }

    public boolean hasParent() {
        return this.parent != null;
    }

    public void setParent(VClass parent) {
        this.parent = parent;
    }

    /**
     * Gets method objects, which are of type VMethod
     * @return List of methods as VMethods
     */
    public List<VMethod> getMethods() {
        return this.get(methods, VMethod.class);
    }

    public VMethod getMethod(String methodName) {
        List<VMethod> allMethods = getMethods();
        for(VMethod method : allMethods) {
            if(method.methodName.equals(methodName))
                return method;
        }
        return null;
    }

    /**
     * Gets member objects, which are of type String
     * @return List of members as Strings
     */
    public List<String> getMembers() {
        return this.get(members, String.class);
    }

    /**
     * Gets all the objects of type T for the current class including it's
     * parent's objects in order. The filter should ensure that
     * overridden objects remain as per the current class' implementation.
     * @return List of all objects
     */
    private <T> List<T> get(List<T> baseObjs, Class<T> type) {
        if (this.hasParent()) {
            List<T> parentObjs = type.equals(String.class) ? ((List<T>)parent.getMembers()) : ((List<T>)parent.getMethods());
            Set<T> currObjs = new HashSet<>(baseObjs);
            List<T> baseObjsCopy = baseObjs;
            // Remove the same objects from parent - overriding
            //parentObjs = parentObjs.stream().filter(obj -> !currObjs.contains(obj)).collect(Collectors.toList());
            //parentObjs.addAll(baseObjs);
            for(int i = 0; i < parentObjs.size(); i++) {
                if(currObjs.contains(parentObjs.get(i))) {
                    baseObjs.remove(baseObjs.indexOf(parentObjs.get(i)));
                }
            }
            parentObjs.addAll(baseObjs);
            return parentObjs;
        }
        return baseObjs;
    }

    private String getMethodsClass(VMethod method) {
        if(this.methods.indexOf(method) != -1) {
            return this.className;
        } else {
            return parent.getMethodsClass(method);
        }
    }

    public void printVMT() {
        System.out.println("const vmt_" + className);
        System.out.println(this.getMethods());
        for(VMethod method : this.getMethods()) {
            String className = this.className;
            if(hasParent()) {
                className = this.getMethodsClass(method);
            }
            System.out.println("\t:"+className+"."+method.methodName);
        }
    }

    public void printClass() {
        System.out.println("CLASS NAME: " + className);
        if(hasParent()) {
            System.out.println("PARENT NAME: " + parent.className);
        }
        List<String> members = getMembers();
        List<VMethod> methods = getMethods();
        for(int i = 0; i < members.size(); i++) {
            System.out.println("VAR " + (i+1) + ": " + members.get(i));
        }
        for(VMethod _method : methods) {
            _method.printMethod();
        }
    }
}
