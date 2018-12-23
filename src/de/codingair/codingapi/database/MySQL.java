package de.codingair.codingapi.database;

import org.bukkit.plugin.Plugin;

import java.sql.*;

public class MySQL {
    private String host;
    private int port;
    private String database;
    private String user;
    private String password;

    private boolean autoReconnect = false;
    private boolean allowMultiQueries = false;
    private Connection con;

    @Deprecated
    public MySQL(Plugin plugin, String host, int port, String database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    public MySQL(String host, int port, String database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    public Connection openConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?allowMultiQueries=" + allowMultiQueries + "&autoReconnect=" + this.autoReconnect, this.user, this.password);
            return con;
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Connection getConnection() {
        return con;
    }

    public void queryUpdate(String query) throws SQLException {
        PreparedStatement st = null;
        try {
            st = con.prepareStatement(query);
            st.execute();
        } finally {
            closeResources(null, st);
        }
    }

    public void queryUpdate(String... query) throws SQLException {
        if(query.length == 0) return;
        if(query.length == 1) {
            queryUpdate(query[0]);
            return;
        }

        StringBuilder builder = new StringBuilder();

        for(String s : query) {
            if(s == null || s.isEmpty()) continue;
            builder.append(s);
            if(!s.endsWith(";")) builder.append(";");
        }

        if(builder.toString().isEmpty()) return;

        queryUpdate(builder.toString());
    }

    public void closeResources(ResultSet rs, PreparedStatement st) throws SQLException {
        if(rs != null) rs.close();
        if(st != null) st.close();
    }

    public void closeConnection() throws SQLException {
        if(con != null) con.close();
    }

    public ResultSet query(String query) throws SQLException {
        if(!isConnected()) return null;

        ResultSet rs;
        Statement st = con.createStatement();
        rs = st.executeQuery(query);

        return rs;
    }

    public boolean isConnected() {
        try {
            return this.con != null && !this.con.isClosed();
        } catch(SQLException e) {
            return false;
        }
    }

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public boolean isAllowMultiQueries() {
        return allowMultiQueries;
    }

    public void setAllowMultiQueries(boolean allowMultiQueries) {
        this.allowMultiQueries = allowMultiQueries;
    }
}
