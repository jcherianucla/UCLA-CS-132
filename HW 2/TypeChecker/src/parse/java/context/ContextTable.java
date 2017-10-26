package context;

import java.util.*;

public class ContextTable {
	public Map<String, MJClass> classes;

	public ContextTable() {
		classes = new HashMap<String, MJClass>();
	}

	public ContextTable(Map<String, MJClass> classes) {
		this.classes = classes;
	}

}
