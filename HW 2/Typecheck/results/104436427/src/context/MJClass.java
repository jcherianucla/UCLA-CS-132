package context;

import java.util.*;

/**
 * MJClass represents a MiniJava Class type that houses
 * fields, methods and a parent. It can be thought of as the wrapper
 * for all class specific scopes.
 */
public class MJClass {
    private String className;
    // Store all the fields in an ordered set
    public Set<MJType> fields = new LinkedHashSet<>();
    private Map<String, MJMethod> methods = new HashMap<>();
    // When we declare a method, start a stack of all the methods we'll
    // see within this method when called on an object type
    public Stack<MJMethod> callingMethodStack = new Stack<>();
    // Points back to parent to create a chain of inheritance
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

    /**
     * Finds the instance of a variable in the class fields if it exists
     * @param var The variable we are looking for
     * @return The variable we found
     */
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

    /**
     * Looks through the chain of inheritance for the requested
     * method base on name. Note that self takes priority over parents.
     * That is the precedence is bottom up.
     * @param methodName Name of the method we are looking for
     * @return The corresponding method if found
     */
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

    /**
     * Provides a pair of classes that showcases a link
     * between the child and a parent.
     * @return The pair of child and parent
     */
    public Tuple2<MJClass, MJClass> linkSet() {
        return this.hasParent() ? new Tuple2<>(this, this.parent) : null;
    }

    /**
     * Returns a set of every field in the class, inclusive of
     * its parents fields if not overriden.
     * @return Set of field types
     */
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

    /**
     * Convenience function for debugging purposes
     */
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
