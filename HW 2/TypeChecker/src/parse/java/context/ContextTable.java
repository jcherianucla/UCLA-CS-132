package context;

import java.util.*;

public class ContextTable {
	private Map<String, MJClass> classes;
	private MJClass currentClass = null;

	public ContextTable() {
		classes = new HashMap<>();
	}

	public ContextTable(Map<String, MJClass> classes) {
		this.classes = classes;
	}

	public Map<String, MJClass> getClasses() {
		return classes;
	}

	public void addClass(MJClass mjClass) {
		classes.put(mjClass.getClassName(), mjClass);
		currentClass = mjClass;
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
