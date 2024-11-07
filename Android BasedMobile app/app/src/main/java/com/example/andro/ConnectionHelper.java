package com.example.andro;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionHelper {
    Connection connection;
    String ip, port, db, un, pass;

    @SuppressLint("NewApi")
    public Connection conclass() {
        // Correct the IP and Port format
        ip = "xxx"; // Use IP only, no port here
        port = "xx"; // Use the correct port
        db = "xxx";
        un = "xx";
        pass = "xx!";

        // Allow all thread policies
        StrictMode.ThreadPolicy tpolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(tpolicy);

        Connection con = null;
        String ConnectionURL = null;

        try {
            // Load the JTDS driver
            Class.forName("net.sourceforge.jtds.jdbc.Driver");

            // Build the connection URL
            ConnectionURL = "jdbc:jtds:sqlserver://" + ip + ":" + port + "/" + db + ";user=" + un + ";password=" + pass + ";";

            // Get the connection
            con = DriverManager.getConnection(ConnectionURL);
        } catch (ClassNotFoundException e) {
            Log.e("Class not found error: ", e.getMessage());
        } catch (SQLException e) {
            Log.e("SQL Exception: ", e.getMessage());
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return con;
    }
}
