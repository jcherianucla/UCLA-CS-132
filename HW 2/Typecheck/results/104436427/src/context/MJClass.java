package context;

import java.util.*;

public class MJClass {
    private String className;
    public Set<MJType> fields = new LinkedHashSet<>();
    private Map<String, MJMethod> methods = new HashMap<>();
    // When we declare a method, start a stack of all the methods we'll
    // see within this method when called on an object type
    public Stack<MJMethod> callingMethodStack = new Stack<>();
    private MJClass parent = null;
    public boolean isMain = false;
    // This is the most recently used method when visiting all method
    // declarations
    private MJMethod MRUMethod = null;

    public MJClass(String name){
        this.className = name;
    }

    public void setParent(MJClass parent) throws MJTypeCheckException {
        // Check for a self cycle between two classes
        if (this.equals(parent))
            throw new MJTypeCheckException("Self inheritance found");
        this.parent = parent;
    }

    public MJType findVarInFields(MJType var) {
        MJType foundVar = null;
        for (MJType variable : this.getFields()) {
            if (var.equals(variable)){
                foundVar = variable;
                break;
            }
        }
        return foundVar;
    }


    public MJClass getParent() {
        return parent;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public String getClassName() {
        return className;
    }

    public void setMRUMethod(MJMethod MRUMethod) {
        this.MRUMethod = MRUMethod;
    }

    public MJMethod getClassMethod (String methodName) {
        if(methods.get(methodName) != null) {
            MJMethod method = methods.get(methodName);
            MRUMethod = method;
            return method;
        } else if (this.hasParent()) {
            MJMethod parentMethod = parent.getClassMethod(methodName);
            if (parentMethod != null) {
                MRUMethod = parentMethod;
                return parentMethod;
            }
        }
        return null;
    }

    public List<MJMethod> getAllMethods() {
        return new ArrayList<>(methods.values());
    }

    public boolean hasMethods() {
        return !methods.isEmpty();
    }

    public MJMethod getMRUMethod() {
        return MRUMethod;
    }

    public void addMethod(MJMethod method) {
        methods.put(method.getMethodName(), method);
        MRUMethod = method;
    }

    public Tuple2<MJClass, MJClass> linkSet() {
        return this.hasParent() ? new Tuple2<>(this, this.parent) : null;
    }

    public Set<MJType> getFields() {
        if(this.hasParent()) {
            Set<MJType> allFields = fields;
            // Add parent fields if not overridden in child
            for (MJType field: this.parent.getFields()) {
                if(!allFields.contains(field)) {
                    allFields.add(field);
                }
            }
            return allFields;
        }
        return this.fields;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if ((o instanceof MJClass)) {
            return this.className.equals(((MJClass) o).className);
        }
        return false;
    }

    public void printMJClass() {
       System.out.println("Class: " + className);
       if(this.hasParent()) {
           System.out.println("Extends: " + parent.getClassName());
       }
       System.out.println("FIELDS");
       for(MJType field : fields) {
           field.printMJType();
       }
        System.out.println("METHODS");
       for(MJMethod method : methods.values()) {
           method.printMJMethod();
       }
    }
}
