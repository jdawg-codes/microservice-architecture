package boundary;

import java.util.Map;
import java.util.Set;

public interface iRequestElement extends Map<String, Object> {
	public Set<String> keySet();
	public Object get(String elementName);
}
