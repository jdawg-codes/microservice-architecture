package entity;

public class EntityFactory {
	public SimpleEntity newEntity(String entityName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String fullEntityName = "entity." + entityName;
		
		return (SimpleEntity) Class.forName(fullEntityName).newInstance();
	}
}
