package context;

import java.util.*;

/**
 * Stores the global context within a given Java file. It is
 * more effectively a symbol table, mapping class names to
 * MJClasses. Allows us to maintain a static context while typechecking.
 */
public class ContextTable {
    // All classes mapped
	public Map<String, MJClass> classes;
	// The most recently touched class - grounds the context
	private MJClass currentClass = null;

	public ContextTable() {
		classes = new HashMap<>();
	}

	public ContextTable(Map<String, MJClass> classes) {
		this.classes = classes;
	}

    /**
     * Finds the requested class and sets it to the most recent
     * @param className Name of the class we are looking for
     * @return The class instance found in the map
     */
	public MJClass getClass(String className) {
		MJClass requestedClass = classes.get(className);
		if (requestedClass != null)
		    currentClass = requestedClass;
		return requestedClass;
	}

    /**
     * Adds the specified class to the global context, updating the
     * current class.
     * @param mjClass Class we are looking for
     */
	public void addClass(MJClass mjClass) {
		classes.put(mjClass.getClassName(), mjClass);
		currentClass = mjClass;
	}

    public void setCurrentClass(MJClass currentClass) {
        this.currentClass = currentClass;
    }

    /**
     * Goes through all linksets to look for inheritance cycles. These
     * occur when we find a parent being a child.
     * @return If the context is cycle free
     */
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

    /**
     * Ensures no overloading exists, by only permitting overriding.
     * @param childClass
     * @param parentClass
     * @param methodName
     * @return Whether the program is overloading free
     */
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

    /**
     * Convenience function for debugging purposes
     */
	public void printContextTable() {
		System.out.println("Current class: " + currentClass.getClassName());
		for(MJClass mjClass : classes.values()) {
			mjClass.printMJClass();
		}
	}
}
