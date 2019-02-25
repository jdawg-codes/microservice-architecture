package boundary;

import java.util.HashMap;
import org.json.simple.JSONObject;

public class JSONResponse extends HashMap<String,Object> implements iResponse {	
	@Override
	public String toJSONString() {
		try {
			JSONObject json = new JSONObject(this);		
			return json.toJSONString();
		} catch(Exception e) {
			return "{\"error\":[\"Unable to convert the response to JSON\"]}";
		}		
	}

	@Override
	public String toXMLString() {
		// TODO Auto-generated method stub
		return null;
	}
}
