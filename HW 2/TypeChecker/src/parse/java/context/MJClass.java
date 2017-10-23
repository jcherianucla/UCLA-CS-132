package context;

import java.util.*;

public class MJClass {
    private String className;
    public Set<MJType> fields = new LinkedHashSet<>();
    public Map<String, MJMethod> methods = new HashMap<>();
    public MJClass parent = null;

    public MJClass(String name){
        this.className = name;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public String getClassName() {
        return className;
    }

    public Set<MJClass> linkSet(MJClass mjClass) {
        return this.hasParent() ? new HashSet<MJClass>() {{ add(mjClass); }} : null;
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
}
