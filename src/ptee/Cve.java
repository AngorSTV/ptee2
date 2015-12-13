package ptee;

import java.util.HashSet;
import java.util.Set;

public class Cve {

	public String		name;
	public String		id;
	public String		description;
	public String		cvssBase;
	public String		cvssTemp;
	public String		status;
	public String		link;
	public String		moduleName;
	public String		moduleDescription;
	public Set<String>	node	= new HashSet<>();

	public Cve(String name) {
		this.name = name;
	}

}
