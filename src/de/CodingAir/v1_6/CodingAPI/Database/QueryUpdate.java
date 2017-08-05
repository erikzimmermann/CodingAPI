package de.CodingAir.v1_6.CodingAPI.Database;

import java.util.HashMap;

public class QueryUpdate {
	private MySQL MySQL;
	private String table;
	private String where = null;
	private String value = null;
	private HashMap<Integer, Object> entry_key = new HashMap<>();
	private HashMap<Integer, Object> entry_value = new HashMap<>();
	private int entryID = 0;
	
	public QueryUpdate(MySQL MySQL, String table) {
		this.MySQL = MySQL;
		this.table = table;
	}
	
	public QueryUpdate(MySQL MySQL, String table, String where, String value) {
		this.MySQL = MySQL;
		this.table = table;
		this.where = where;
		this.value = value;
	}
	
	public void send(){
		if(where != null && value != null) this.MySQL.queryUpdate("DELETE FROM `"+this.table+"` WHERE "+this.where+" = '"+this.value+"'");
		
		String query = "INSERT INTO `"+this.table+"` (";
		
		for(int id = 0; id < this.entryID; id++){
			query = query + "`" + entry_key.get(id) + "`, ";
		}
		
		query = query.substring(0, query.length()-2) + ") VALUES (";
		
		for(int id = 0; id < this.entryID; id++){
			query = query + "'" + entry_value.get(id) + "', ";
		}
		
		query = query.substring(0, query.length()-2) + ")";
		
		this.MySQL.queryUpdate(query);
	}
	
	public void addEntry(String key, Object value){
		this.entry_key.put(entryID, key);
		this.entry_value.put(entryID, value);
		this.entryID++;
	}

	public MySQL getMySQL() {
		return MySQL;
	}

	public String getTable() {
		return table;
	}

	public String getWhere() {
		return where;
	}

	public String getValue() {
		return value;
	}
}
