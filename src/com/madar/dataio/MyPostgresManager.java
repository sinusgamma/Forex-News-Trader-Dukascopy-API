
package com.madar.dataio;

import com.madar.library.newsstraddle.MyCurrency;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MyPostgresManager{
 
    private static final String url = "jdbc:postgresql://localhost/postgres";
    private static final String user = "postgres";
    private static final String password = "password";
 
    
    public static void main(String[] args) {
//        MyPostgresManager postgreConnector = new MyPostgresManager();
//        postgreConnector.readForexCalendarDB("Manufacturing PMI", MyCurrency.CHF, 40, "2010-05-20 00:00:00");
    }
    
    /**
     * Connect to the PostgreSQL database
     *
     * @return a Connection object 
     */
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
 
    
    public static List<MySingleEventHolder> readForexCalendarDB(String newsname, MyCurrency currency, int limit, String earliestDate, String lastDate){
        
        List<MySingleEventHolder> collection = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String SQL =    "SELECT news_time, currency, news_name FROM\n" +
                        "(SELECT * FROM newsdata.fxs_news\n" +
                        "WHERE news_name = '" + newsname + "' AND currency = '" + currency + "' AND news_time > '" + earliestDate +  "' AND news_time < '" + lastDate + "'\n" +
                        "ORDER BY news_time DESC\n" +
                        "LIMIT " + limit + ") tempdata\n" +
                        "ORDER BY tempdata.news_time ASC\n" +
                        ";";        
        try (Connection conn = connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SQL)) {       
            boolean firstStep = true;
            // display information
            while(rs.next()){
                if(firstStep){
                    System.out.println("Using events of: " + rs.getString("news_name") + " | " + rs.getString("currency"));
                }
                firstStep = false;               
                // Accessing values by the names assigned to each column                
                String strDateTime = rs.getString("news_time");
                LocalDateTime ldt = LocalDateTime.parse(strDateTime, formatter);
                ZonedDateTime zdt = ldt.atZone(ZoneId.of("UTC"));
                String newsName = rs.getString("news_name");
                MyCurrency myCurrency = MyCurrency.valueOf(rs.getString("currency"));          
                MySingleEventHolder singleEventHolder = new MySingleEventHolder(zdt, newsName, myCurrency);
                collection.add(singleEventHolder);                               
            }       
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }      
        System.out.println("event number: " + collection.size());
        System.out.println("event number: " + collection.size() + " from " + collection.get(0).getEventTime() + " to " + collection.get(collection.size() - 1).getEventTime());   
        return collection;
    }
}
