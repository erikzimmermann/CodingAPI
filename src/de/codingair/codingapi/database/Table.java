package de.codingair.codingapi.database;

import java.sql.SQLException;
import java.util.HashMap;

public class Table {
	private MySQL MySQL;
	private String table;
	private HashMap<Integer, Object> entry_key = new HashMap<>();
	private HashMap<Integer, Object> entry_value = new HashMap<>();
	private int entryID = 0;
	
	public Table(MySQL MySQL, String table) {
		this.MySQL = MySQL;
		this.table = table;
	}
	
	public void addEntry(String name, Object value){
		this.entry_key.put(entryID, name);
		this.entry_value.put(entryID, value);
		this.entryID++;
	}
	
	public void create() throws SQLException {
		String query = "CREATE TABLE IF NOT EXISTS "+this.table+" (";
		
		for(int id = 0; id < entryID; id++){
			query = query + entry_key.get(id) + " "+ this.entry_value.get(id) +", ";
		}
		
		query = query.substring(0, query.length()-2) + ")";
		
		this.MySQL.queryUpdate(query);
	}
}
