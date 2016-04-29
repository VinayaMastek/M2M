package com.mastek.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.sf.json.JSONObject;

public class PostgresImpl {
	private Connection conn = null;
	private Statement stmt = null;

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public Statement getStmt() {
		return stmt;
	}

	public void setStmt(Statement stmt) {
		this.stmt = stmt;
	}

	public void connect() {
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/m2m",
					"m2m", "m2m");
			conn.setAutoCommit(false);
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

	}

	public void insertEvents(JSONObject eventData) {
		String quote = "'";
		String sql = "INSERT INTO " +"\""+"fitnessDetails"+"\"";
		String fieldStr = "(" + "personid" + "," + "timevalue" + "," + "datatype"
				+ "," + "datavalue" + "," + "longitude" + "," + "latitude" 
				+ ")";
		
   	     SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
		 Date dt = null;
		    try {
		        dt = sdf.parse(eventData.getString("timevalue"));
		    } catch (ParseException e) {
		        e.printStackTrace();
		    }
		    Calendar calendar = Calendar.getInstance();
		    calendar.setTime(dt);
		
		String valueStr = "(" 
				+ eventData.get("personid")
				+ "," + quote +calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+ quote
				+ "," + quote + eventData.getString("datatype") + quote 
				+ "," + eventData.getInt("datavalue")  
				+ "," + eventData.getDouble("longitude") 
				+ "," + eventData.getDouble("latitude")
 				+ ")";
		sql = sql + fieldStr + " VALUES " + valueStr;
		System.out.println(sql);
		executeSql(sql);
	}

	
	private void commit() {
		try {
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void executeSql(String sql) {
		if (!sql.isEmpty()) {
			try {
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				stmt.close();
				commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public void close() {
		if (conn != null)
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}

	public String fetchDetails()
	{
		JSONObject jobj = new JSONObject();
		
		if (conn == null)
			connect();

		String sql ="";
		ResultSet rs = null;
		sql = "SELECT fitnessid, personid, datatype, datavalue, longitude, latitude, timevalue FROM"+ "\"" +"fitnessDetails"+ "\"";

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()){
				jobj.put("datatype", rs.getString("datatype"));
				jobj.put("datavalue", rs.getInt("datavalue"));
				jobj.put("longitude", rs.getDouble("longitude"));
				jobj.put("latitude", rs.getDouble("latitude"));
				jobj.put("timevalue", rs.getString("timevalue"));
			}
			rs.close();
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return jobj.toString();
	}

		
}

/*
 http://localhost:8080/IOTWebClient/rest/apis/heartbeat
 {"personid": 1, "timevalue":"18:39:40","datatype":"HB", "datavalue":76, "longitude":100.90,"latitude":90.60}
 {"personid": 1, "timevalue":"18:45:30","datatype":"HB", "datavalue":80, "longitude":100.90,"latitude":90.60}
 {"personid": 1, "timevalue":"18:50:34","datatype":"HB", "datavalue":82, "longitude":100.90,"latitude":90.60}
 {"personid": 1, "timevalue":"19:01:01","datatype":"HB", "datavalue":77, "longitude":100.90,"latitude":90.60}
 {"personid": 1, "timevalue":"20:15:03","datatype":"HB", "datavalue":90, "longitude":100.90,"latitude":90.60}
	
 INSERT INTO "fitnessDetails"(
            personid, datatype, datavalue, longitude, latitude, 
            timevalue)
    VALUES ( 1, 'HB', 90, 10.0,29.0 ,'11:09');
    
*/