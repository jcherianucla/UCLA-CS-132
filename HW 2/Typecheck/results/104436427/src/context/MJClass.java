package context;

import java.util.*;

public class MJClass implements context.Identifier {
    private String className;
    public Set<context.MJType> fields = new LinkedHashSet<>();
    private Map<String, context.MJMethod> methods = new HashMap<>();
    private MJClass parent = null;
    public boolean isMain = false;
    private context.MJMethod MRUMethod = null;

    public MJClass(String name){
        this.className = name;
    }

    public boolean setParent(MJClass parent) {
        // Check for a cycle between two classes
        if (parent.hasParent() && parent.getParent() == this) {
            return false;
        }
        this.parent = parent;
        return true;
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

    public Map<String, context.MJMethod> getClassMethods () {
        return methods;
    }

    public context.MJMethod getMRUMethod() {
        return MRUMethod;
    }

    public void addMethod(context.MJMethod method) {
        methods.put(method.getMethodName(), method);
        MRUMethod = method;
    }

    public Set<MJClass> linkSet(MJClass mjClass) {
        return this.hasParent() ? new HashSet<MJClass>() {{ add(mjClass); }} : null;
    }

    public Set<context.MJType> getFields() {
        if(this.hasParent()) {
            Set<context.MJType> allFields = fields;
            // Add parent fields if not overridden in child
            for (context.MJType field: this.parent.getFields()) {
                if(!allFields.contains(field)) {
                    allFields.add(field);
                }
            }
            return allFields;
        }
        return this.fields;
    }

    public Set<context.MJType> getMethodType(String methodName) {
        context.MJMethod method = methods.get(methodName);
        if (method == null && this.hasParent()) {
            return parent.getMethodType(methodName);
        } else if (method != null) {
            return method.params;
        } else {
            return null;
        }
    }

    @Override
    public boolean distinct(Object other) {
        if ((other instanceof MJClass)) {
            return this.className.equals(((MJClass)other).getClassName());
        }
        return false;
    }

    public void printMJClass() {
       System.out.println("Class: " + className);
       if(this.hasParent()) {
           System.out.println("Extends: " + parent.getClassName());
       }
       System.out.println("FIELDS");
       for(context.MJType field : fields) {
           field.printMJType();
       }
        System.out.println("METHODS");
       for(context.MJMethod method : methods.values()) {
           method.printMJMethod();
       }
    }
}
