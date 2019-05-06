package de.codingair.codingapi.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueryUpdate {
	private MySQL MySQL;
	private String table;
	private String where = null;
	private String value = null;
	private HashMap<String, Object> entries = new HashMap<>();
	
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
	
	public void send() throws SQLException {
		if(where != null && value != null) this.MySQL.queryUpdate("DELETE FROM `"+this.table+"` WHERE "+this.where+" = '"+this.value+"'");
		
		StringBuilder query = new StringBuilder("INSERT INTO `" + this.table + "` (");

		for(String key : this.entries.keySet()) {
			query.append("`").append(key).append("`, ");
		}
		
		query = new StringBuilder(query.substring(0, query.length() - 2) + ") VALUES (");

		for(Object value : this.entries.values()) {
			query.append("'").append(value).append("', ");
		}
		
		query = new StringBuilder(query.substring(0, query.length() - 2) + ")");
		
		this.MySQL.queryUpdate(query.toString());
		this.entries.clear();
	}
	
	public void addEntry(String key, Object value){
		this.entries.put(key, value);
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

	public void setWhere(String where) {
		this.where = where;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
