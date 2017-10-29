package context;

import java.util.*;

public class ContextTable {
	public Map<String, MJClass> classes;
	private MJClass currentClass = null;

	public ContextTable() {
		classes = new HashMap<>();
	}

	public ContextTable(Map<String, MJClass> classes) {
		this.classes = classes;
	}

	public MJClass getClass(String className) {
		MJClass requestedClass = classes.get(className);
		if (requestedClass != null)
		    currentClass = requestedClass;
		return requestedClass;
	}

	public void addClass(MJClass mjClass) {
		classes.put(mjClass.getClassName(), mjClass);
		currentClass = mjClass;
	}

    public void setCurrentClass(MJClass currentClass) {
        this.currentClass = currentClass;
    }

    public boolean acyclic() {
		List<Tuple2<MJClass, MJClass>> allLinkSets = new ArrayList<>();
		for(MJClass mjClass : classes.values()) {
		    Tuple2<MJClass, MJClass> linkSet = mjClass.linkSet();
		    if(linkSet != null)
				allLinkSets.add(linkSet);
		}
		for(int i = 0; i < allLinkSets.size(); i++) {
			for(int j = i+1; j < allLinkSets.size(); j++) {
				if(allLinkSets.get(i).first.equals(allLinkSets.get(j).second))
					return false;
			}
		}
		return true;
	}

	public boolean noOverloading(MJClass childClass, MJClass parentClass, String methodName) {
	    MJMethod childMethod = childClass.getClassMethod(methodName);
	    MJMethod parentMethod = parentClass.getClassMethod(methodName);
	    if (childMethod != null && parentMethod != null) {
	        return childMethod.equals(parentMethod);
        }
        return true;
    }

	public MJClass getCurrentClass() {
		return currentClass;
	}

	public void printContextTable() {
		System.out.println("Current class: " + currentClass.getClassName());
		for(MJClass mjClass : classes.values()) {
			mjClass.printMJClass();
		}
	}
}
