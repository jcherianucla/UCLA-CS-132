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
    public Stack<MJClass> classStack = new Stack<>();
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
     * When building the context table, we might have some nodes set to
     * dummy parents that at the time had yet to be initialized. Reset
     * those dummy parents to the actual reference
     */
	public void resetDummyParents() {
	    for(Map.Entry<String, MJClass> _class : classes.entrySet()) {
	        String className = _class.getKey();
	        MJClass actualClass = _class.getValue();
	       if(actualClass.hasParent()
                   && actualClass.getParent().preInitialize) {
	           String parentName = actualClass.getParent().getClassName();
               classes.get(className).setParent(classes.get(parentName));
           }
        }
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
     * Finds if a subclassing relationship exists
     * @param className1
     * @param className2
     * @return
     */
    public boolean linkExists(String className1, String className2) {
        List<Tuple2<MJClass, MJClass>> allLinkSets = new ArrayList<>();
        for(MJClass mjClass : classes.values()) {
            Tuple2<MJClass, MJClass> linkSet = mjClass.linkSet();
            if(linkSet != null)
                allLinkSets.add(linkSet);
        }
        for(Tuple2<MJClass, MJClass> linkset : allLinkSets) {
            String childName = linkset.first.getClassName();
            String parentName = linkset.second.getClassName();
            if((childName.equals(className1) && parentName.equals(className2))
                    || (childName.equals(className2) && parentName.equals(className1))) {
                return true;
            }
        }
        return false;
    }

    /**
     * For each class, perform a DFS using it as a starting node in the
     * class directed graph. If we find a cycle (i.e. we have already visited
     * a node) then return the existence of the cycle.
     * @return If the context is cycle free
     */
    public boolean acyclic() {
        // Perform dfs on every class
        for(Map.Entry<String, MJClass> _class : classes.entrySet()) {
            Set<String> visited = new HashSet<>();
            visited.add(_class.getKey());
            if (!dfs(_class.getValue(), visited)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Runs a recursive DFS on each node keeping count of visited nodes
     * for finding a cycle.
     * @param visiting MJClass currently being inspected
     * @param visited All class names that have been visited
     * @return Whether a cycle exists
     */
    private boolean dfs(MJClass visiting, Set<String> visited) {
        if(visiting == null) {
            return true;
        }
        if (visiting.hasParent()) {
            if(visited.contains(visiting.getParent().getClassName())) {
                return false;
            }
            visited.add(visiting.getParent().getClassName());
        }
        return dfs(visiting.getParent(), visited);
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
        if (!classStack.empty())
            return classStack.peek();
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
