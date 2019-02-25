package tool;

public interface iDependencyContainer {
	public Object get(String dependencyName);
	public void put(String dependencyName, Object dependency);
}
