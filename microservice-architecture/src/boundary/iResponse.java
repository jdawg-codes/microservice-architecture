package boundary;

import java.util.Map;

public interface iResponse extends Map<String, Object> {
	public Object put(String name, Object element);
	
	public String toJSONString();
	public String toXMLString();
}
