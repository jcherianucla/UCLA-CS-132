package context;

import java.util.*;

public class MJClass implements Identifier {
    private String className;
    public Set<MJType> fields = new LinkedHashSet<>();
    private Map<String, MJMethod> methods = new HashMap<>();
    private MJClass parent = null;
    public boolean isMain = false;
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

    public MJClass getParent() {
        return parent;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public String getClassName() {
        return className;
    }

    public MJMethod getClassMethod (String methodName) {
        if(methods.get(methodName) != null) {
            MJMethod method = methods.get(methodName);
            MRUMethod = method;
            return method;
        }
        return null;
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

    public Set<MJType> getMethodType(String methodName) {
        MJMethod method = methods.get(methodName);
        if (method == null && this.hasParent()) {
            return parent.getMethodType(methodName);
        } else if (method != null) {
            return method.params;
        } else {
            return null;
        }
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
       for(MJType field : fields) {
           field.printMJType();
       }
        System.out.println("METHODS");
       for(MJMethod method : methods.values()) {
           method.printMJMethod();
       }
    }
}
