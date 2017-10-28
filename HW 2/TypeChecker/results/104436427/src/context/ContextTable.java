package context;

import java.util.*;

public class ContextTable {
	private Map<String, context.MJClass> classes;
	private context.MJClass currentClass = null;

	public ContextTable() {
		classes = new HashMap<>();
	}

	public ContextTable(Map<String, context.MJClass> classes) {
		this.classes = classes;
	}

	public Map<String, context.MJClass> getClasses() {
		return classes;
	}

	public void addClass(context.MJClass mjClass) {
		classes.put(mjClass.getClassName(), mjClass);
		currentClass = mjClass;
	}

	public context.MJClass getCurrentClass() {
		return currentClass;
	}

	public void printContextTable() {
		System.out.println("Current class: " + currentClass.getClassName());
		for(context.MJClass mjClass : classes.values()) {
			mjClass.printMJClass();
		}
	}
}
