/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.domain.KBManagement;

//#J2ME_EXCLUDE_FILE

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class provides an abstract implementation of the 
 * <code>KB</code> interface where information are stored in
 * a database.
 * 
 * @author Elisabetta Cortese (TiLab S.p.A.)
 */
public abstract class DBKB extends KB {
	
	// il database e' generico per JADE, ora viene usato solo dal
	// DF ma in futuro potra' essere usato anche dall'AMS
	// Come paramaetri di default vengono usati quelli per accedere
	// ad un Database creato con Microsoft Access.

	private String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
	protected Connection conn = null;

	//protected LeaseManager lm;
	//protected SubscriptionResponder sr;
	
	// se il driver  l'indirizzo sono stati
	// forniti in input alla piattaforma usa 
	// questo costruttore
	public DBKB(String url, int maxResultLimit) throws SQLException {
		super(maxResultLimit);
    loadDBDriver(null);
	  setDBConnection(null, url, null, null);
	}

	public DBKB(String drv, String url, int maxResultLimit) throws SQLException {
		super(maxResultLimit);
    loadDBDriver(drv);
    setDBConnection(drv, url, null, null);
	}

	public DBKB(String drv, String url, String user, String passwd, int maxResultLimit) throws SQLException {
		super(maxResultLimit);
    loadDBDriver(drv);
    setDBConnection(drv, url, user, passwd);
    setup();
	}
  
  

  /**
   * Loads an JDBC driver
   * @param drv dirver name or <code>null</code> </ br>
   * if the default JDBC-ODBC driver should be used
   * @throws SQLException if the driver cannot be loaded
   */
  protected void loadDBDriver(String drv) throws SQLException {
    //  Load DB driver
    try {
      if(drv != null) {
        if(!drv.equals("null"))
          driver = drv;
      }
      Class.forName(driver).newInstance();
    }
    catch(Exception e){
      throw new SQLException("Error loading driver "+driver+". "+e);
    }
  }
  
  /**
   * Establishes a new connection to the database and stores a reference in the
   * local attribute <code>conn</code>
   * @param drv database driver
   * @param url database URL
   * @param user database user
   * @param passwd database password
   * @throws SQLException if a database access error occurs
   */
	protected void setDBConnection(String drv, String url, String user, String passwd) throws SQLException {
    
		// Connect to the DB
		if (user != null) {
			conn = DriverManager.getConnection(url, user, passwd);
		}
		else {
			conn = DriverManager.getConnection(url);
		}
	}
	
	
	// Called from constructor
	// 1. crea le tabelle se non esistono 
	// 2. fa una pulizia iniziale per togliere di mezzo le registrazioni scadute
	protected void setup() throws SQLException {
	}
}
