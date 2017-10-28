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
