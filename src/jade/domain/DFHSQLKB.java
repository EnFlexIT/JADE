package jade.domain;

//#J2ME_EXCLUDE_FILE

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * <code>DFHSQLKB</code> realizes a knowledge base used by the DF which stores it's content <br />
 * in a HSQL database. The SQL commands are optimized for the HSQL database.
 * 
 * @author Roland Mungenast - Profactor
 */
public class DFHSQLKB extends DFDBKB {

  private static String db_driver = "org.hsqldb.jdbcDriver";
  private static String db_url = "jdbc:hsqldb:file:dfdb";
  private static String db_user = "sa";
  private static String db_passwd = "";
  
  /**
   * Constructor
   * @param maxResultLimit internal limit for the number of maximum search results.
   * @throws SQLException if a database access error occurs
   */
  public DFHSQLKB(int maxResultLimit) throws SQLException {
    super(maxResultLimit, db_driver, db_url, db_user, db_passwd);
  }
  
  protected void setup() throws SQLException {
    
    super.setup();
  }
 
  protected void setDBConnection(String drv, String url, String user, String passwd) throws SQLException {
    Properties props = new Properties();
    props.put("user", user);
    props.put("passwd", passwd);
    props.put("hsqldb.cache_scale", "8");
    props.put("hsqldb.cache_size_scale", "8");
    props.put("hsqldb.gc_interval", "10000");
    
    conn = DriverManager.getConnection(url, props);
  }
 
  
  protected void createTable(Statement stmt, String name, String[] entries) throws SQLException {
    String sql = "CREATE CACHED TABLE " + name + " (";
    for (int i = 0; i < entries.length; i++) {
      sql += entries[i];
      if (i < entries.length - 1)
        sql += ", ";
      else
        sql += ")";
    }
    stmt.executeUpdate(sql);
    conn.commit();
  }
}