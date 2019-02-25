package entity;

import java.util.List;
import java.util.Map;

import tool.iDependencyContainer;

public interface iEntity {
	public void dependencies(iDependencyContainer dependencies);
	public boolean hasAttribute(String attributeName);
	public Object id();
	public boolean validateAttribute(String attributeName, Map<String,Object> attribute);
	public List<String> attributeNames();
}
