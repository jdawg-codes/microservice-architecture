package tool;

import java.util.HashMap;
import java.util.Map;

public class DependencyContainer implements iDependencyContainer {
	private Map <String,Object> dependencyMap;
	
	public DependencyContainer() {
		this.dependencyMap = new HashMap<String, Object>();
	}
	
	@Override
	public Object get(String dependencyName) {
		if(this.dependencyMap.containsKey(dependencyName)) {
			return this.dependencyMap.get(dependencyName);
		}
		
		return null;
		
		/* consider instantiating new objects of the requested type
		String fullDependencyName = null;
				
		if(dependencyName=="output-boundary" || dependencyName=="request" || dependencyName=="response") {
			fullDependencyName = "boundary." + dependencyName;						
		}
		
		try {	
			return Class.forName(fullDependencyName).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		*/		
	}

	@Override
	public void put(String dependencyName, Object dependency) {
		try {
			this.dependencyMap.put(dependencyName, dependency);
		} catch(UnsupportedOperationException | ClassCastException | NullPointerException | IllegalArgumentException e) {
			System.out.println("Could not put dependency: " + e.getMessage());
			
			for(int i=0; i<e.getStackTrace().length; i++) {
				System.out.println(e.getStackTrace()[i]);
			}
			
		}		
	}
}
