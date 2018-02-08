package de.codingair.codingapi.database;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import org.bukkit.plugin.Plugin;

import java.sql.*;

public class MySQL {
    private Plugin plugin;
    private String host;
    private int port;
    private String database;
    private String user;
    private String password;

    private Connection con;

    @Deprecated
    public MySQL(Plugin plugin, String host, int port, String database, String user, String password) {
        this.plugin = plugin;
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
            this.con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.user, this.password);
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

    public void closeResources(ResultSet rs, PreparedStatement st) {
        if(rs != null) {
            try {
                rs.close();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }

        if(st != null) {
            try {
                st.close();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeConnection() {
        try {
            con.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
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
}
