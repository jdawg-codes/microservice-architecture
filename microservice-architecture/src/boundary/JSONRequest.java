package boundary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONRequest implements iRequest {
	private JSONObject json = null;
	private Map<String, Object> data;
	
	public JSONRequest(String input) {
		JSONParser parser = new JSONParser();		
		
		try {
			this.json = (JSONObject) parser.parse(input);
		} catch (ParseException e) {
			System.out.println("Unable to parse the request model!");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			this.toMap();
		} catch (Exception e) {
			System.out.println("Unable to convert request model to HashMap!");
		}
	}
	
	public JSONObject toJSONObject() {
		return this.json;
	}
	
	public Map<String, Object> toMap() {
		Map<String, Object> map = constructMapObject(this.json);

		this.data = map;

		return this.data;		
	}
	
	private Map<String, Object> constructMapObject(Object json) {
		Map<String, Object> data = new HashMap<String, Object>();
		JSONObject JSONObj = (JSONObject) json;
		
		if(json instanceof JSONObject) {
			Set<?> propertyNames = JSONObj.keySet();
			
			for(Object propertyName : propertyNames) {
				Object property = JSONObj.get(propertyName);
				
				if(property instanceof JSONObject) {
					data.put(propertyName.toString(),this.constructMapObject(property));
				} else if(property instanceof JSONArray) {
					data.put(propertyName.toString(),this.constructMapArray(property));
				} else {
					data.put(propertyName.toString(), JSONObj.get(propertyName));
				}
			}
		} 
		
		return data;
	}
	
	private Collection<Object> constructMapArray(Object json) {
		JSONArray array = (JSONArray) json;
		
		Collection<Object> list = new ArrayList<Object>();
		
		for(Object element: array) {
			if(element instanceof JSONObject) {
				list.add(this.constructMapObject(element));
			} else if(element instanceof JSONArray) {
				list.add(this.constructMapArray(element));
			} else {
				list.add(element);
			}
		}
		
		return list;
	}
	
	public Object get(String element) {
		return this.data.get(element);
	}
	
	public Set<String> keySet() {
		return this.data.keySet();
	}
}
