package gateway;

import java.util.List;
import java.util.Map;

import boundary.iRequest;

public interface iGateway {
	public void connect();
	public void disconnect();
	public Map<String,Object> execute();
}
