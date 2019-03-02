package gateway;

import java.util.List;

import entity.iEntity;

public interface iEntityGateway extends iGateway {
	public void entity(iEntity entity);
	public boolean hasAttributes(List<String> attributes);
}
