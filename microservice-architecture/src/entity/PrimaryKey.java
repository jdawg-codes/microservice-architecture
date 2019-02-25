package entity;

import java.util.UUID;

public class PrimaryKey {
	public String get() {
		return UUID.randomUUID().toString();		
	}
}
