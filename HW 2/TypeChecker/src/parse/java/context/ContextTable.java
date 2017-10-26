package context;

import java.util.*;

public class ContextTable {
	public Map<String, MJClass> classes;
	public MJClass currentClass = null;

	public ContextTable() {
		classes = new HashMap<>();
	}

	public ContextTable(Map<String, MJClass> classes) {
		this.classes = classes;
	}

}
