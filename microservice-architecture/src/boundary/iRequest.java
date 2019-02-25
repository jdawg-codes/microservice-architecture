package boundary;

import java.util.Map;

public interface iRequest {
	public Object toJSONObject();
	public Map<String, Object> toMap();
	public Object get(String element);
}
