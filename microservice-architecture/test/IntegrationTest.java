import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

public class IntegrationTest {

	public void deleteMockDatabase() {	
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e2) {
			fail("MySQL Driver for Test Database Not Available: " + e2.getMessage());
		}

		Connection conn = null;
		
		try {
			String connectionString = "jdbc:mysql://localhost:3306/MicroserviceTest";
			conn = DriverManager.getConnection(connectionString,"testuser", "password");
		} catch (SQLException e1) {
			fail("No Test Database Available: " + e1.getMessage());
		}
		
		String query = "DELETE FROM University;";
		
		try {
			Statement statement = conn.createStatement();
			statement.execute(query);
			conn.close();
		} catch (SQLException e1) {
			System.out.println("a: " + e1.getMessage());
			fail("Test Database Statement creation failed: " + e1.getMessage());
		}
	}
	
	public void insertMockDatabase() {	
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e2) {
			fail("MySQL Driver for Test Database Not Available: " + e2.getMessage());
		}

		Connection conn = null;
		
		try {
			String connectionString = "jdbc:mysql://localhost:3306/MicroserviceTest";
			conn = DriverManager.getConnection(connectionString,"testuser", "password");
		} catch (SQLException e1) {
			fail("No Test Database Available: " + e1.getMessage());
		}
		
		String query = "INSERT INTO University (id, name) VALUES ('deb94afe-35dd-4bed-bc33-6431bf5f0cd0','Michigan Tech');";
		
		try {
			Statement statement = conn.createStatement();
			statement.execute(query);
			conn.close();
		} catch (SQLException e1) {
			System.out.println("a: " + e1.getMessage());
			fail("Test Database Statement creation failed: " + e1.getMessage());
		}
	}
	
	@Test
	public void testCanCreateEntityWithMySQLGateway() {		
		deleteMockDatabase();
		
		long startTime = System.nanoTime();
		
		try {
			String expectedResult = "\\{\"id\":\"[0-9a-f-]+\",\"message\":\"Created\",\"entity\":\"University\",\"status\":201\\}";

			Main main = new Main();
			String result = main.main("{\"method\":\"post\",\"entity\":\"University\",\"attributes\":{\"name\":\"Michigan Tech\"}}");
							
			assertTrue(result.toString().matches(expectedResult));
		} catch(Exception e) {			
			fail("Not yet implemented: " + e.getMessage());
		}
		
		long endTime = System.nanoTime();		
		System.out.println((endTime - startTime)/1000000 + " milliseconds");
	}

	@Test
	public void testCanReadEntityWhereContainsWithMySQLGateway() {
		deleteMockDatabase();
		insertMockDatabase();
		
		long startTime = System.nanoTime();
				
		try {
			String expectedResult = "\\{\"message\":\"OK\",\"results\":\\{\"data\":\\[\\{\"name\":\"Michigan Tech\"\\}\\]\\},\"entity\":\"University\",\"status\":200\\}";
			
			Main main = new Main();
			String result = main.main("{\"method\":\"get\",\"entity\":\"University\",\"fields\":[\"name\"],\"conditions\":{\"name\":{\"operator\":\"contains\",\"value\":\"Tech\"}}}");
			
			System.out.println(expectedResult);
			System.out.println(result.toString());
			
			assertTrue(result.toString().matches(expectedResult));
		} catch(Exception e) {			
			fail("Not yet implemented: " + e.getMessage());
		}
		
		long endTime = System.nanoTime();		
		System.out.println((endTime - startTime)/1000000 + " milliseconds");
	}
	
	@Test
	public void testCanReadEntityWhereEqualsWithMySQLGateway() {
		deleteMockDatabase();
		insertMockDatabase();
		
		long startTime = System.nanoTime();
				
		try {
			String expectedResult = "\\{\"message\":\"OK\",\"results\":\\{\"data\":\\[\\{\"name\":\"Michigan Tech\",\"id\":\"[0-9a-f-]+\"\\}\\]\\},\"entity\":\"University\",\"status\":200\\}";
			
			Main main = new Main();
			String result = main.main("{\"method\":\"get\",\"entity\":\"University\",\"conditions\":{\"name\":{\"operator\":\"equals\",\"value\":\"Michigan Tech\"}}}");
			
			System.out.println(expectedResult);
			System.out.println(result.toString());
			
			assertTrue(result.toString().matches(expectedResult));
		} catch(Exception e) {			
			fail("Not yet implemented: " + e.getMessage());
		}
		
		long endTime = System.nanoTime();		
		System.out.println((endTime - startTime)/1000000 + " milliseconds");
	}
	
	@Test
	public void testCanUpdateEntityWithMySQLGateway() {
		deleteMockDatabase();
		insertMockDatabase();
		
		long startTime = System.nanoTime();
				
		try {
			String expectedResult = "\\{\"message\":\"OK\",\"entity\":\"University\",\"status\":200\\}";
			
			Main main = new Main();
			String result = main.main("{\"method\":\"put\",\"entity\":\"University\",\"attributes\": {\"name\":\"Michigan Technological University\"},\"filters\": {\"id\": 123}}");
			
			System.out.println(expectedResult);
			System.out.println(result.toString());
			
			assertTrue(result.toString().matches(expectedResult));
		} catch(Exception e) {			
			fail("Not yet implemented: " + e.getMessage());
		}
		
		long endTime = System.nanoTime();		
		System.out.println((endTime - startTime)/1000000 + " milliseconds");
	}
	
	@Test
	public void testCanDeleteEntityWithMySQLGateway() {
		deleteMockDatabase();
		insertMockDatabase();
		
		long startTime = System.nanoTime();
				
		try {
			String expectedResult = "\\{\"message\":\"Successfully deleted the entity\",\"entity\":\"University\",\"status\":200\\}";
			
			Main main = new Main();
			String result = main.main("{\"method\":\"delete\",\"entity\":\"University\"}");
			
			System.out.println(expectedResult);
			System.out.println(result.toString());
			
			assertTrue(result.toString().matches(expectedResult));
		} catch(Exception e) {			
			fail("Not yet implemented: " + e.getMessage());
		}
		
		long endTime = System.nanoTime();		
		System.out.println((endTime - startTime)/1000000 + " milliseconds");
	}
}
