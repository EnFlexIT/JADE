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

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.proto.SubscriptionResponder;
import jade.util.Logger;
import jade.util.leap.List;
import jade.util.leap.ArrayList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * This class provides an abstract implementation of the 
 * <code>KB</code> interface where information are stored in
 * a database.
 * 
 * @author Roland Mungenast - Profactor
 */
public abstract class DBKB extends KB {
	
	/**
	 * Used database driver
	 */
	protected String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
	
	/**
	 * Used database connection
	 */
	protected Connection conn = null;
	
	private String url, username, password;
	
	/**
	 * Specifies whether the KB should delete all existing tables for the DF at startup
	 */
	protected boolean cleanTables;
	
	/**
	 * Constructs a new <code>DFKB</code> and establishes a connection to the database
	 * at the given URL using the <code>sun.jdbc.odbc.JdbcOdbcDriver</code> driver.
	 * 
	 * @param maxResultLimit internal limit for the number of maximum search results.
	 * @param url database URL
	 * @param maxResultLimit JADE internal limit for the maximum number of search results
	 * @param cleanTables specifies whether the KB should delete all existing tables for the DF at startup
	 * @throws SQLException an error occured while opening a connection to the database
	 */
	public DBKB(String url, int maxResultLimit, boolean cleanTables) throws SQLException {
		this(null, url, maxResultLimit, cleanTables);
	}
	
	/**
	 * Constructs a new <code>DFKB</code> and establishes a connection to the database.
	 * 
	 * @param maxResultLimit internal limit for the number of maximum search results.
	 * @param drv database driver
	 * @param url database URL
	 * @param user database user name
	 * @param passwd database password
	 * @param maxResultLimit JADE internal limit for the maximum number of search results
	 * @param cleanTables specifies whether the KB should delete all existing tables for the DF at startup
	 * @throws SQLException an error occured while opening a connection to the database
	 */
	public DBKB(String drv, String url, int maxResultLimit, boolean cleanTables) throws SQLException {
		this(drv, url, null, null, maxResultLimit, cleanTables);
	}
	
	/**
	 * Constructs a new <code>DFKB</code> and establishes a connection to the database.
	 * 
	 * @param maxResultLimit internal limit for the number of maximum search results.
	 * @param drv database driver
	 * @param url database URL
	 * @param user database user name
	 * @param passwd database password
	 * @param maxResultLimit JADE internal limit for the maximum number of search results
	 * @param cleanTables specifies whether the KB should delete all existing tables for the DF at startup
	 * @throws SQLException an error occured while opening a connection to the database
	 */
	public DBKB(String drv, String url, String username, String password, int maxResultLimit, boolean cleanTables) throws SQLException {
		super(maxResultLimit);
		this.cleanTables = cleanTables;
		loadDBDriver(drv);
		setDBConnection(url, username, password);
		setup();
	}
	
	/**
	 * Loads an JDBC driver
	 * @param drv driver name or <code>null</code> </ br>
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
	 * @param url database URL
	 * @param user database user
	 * @param passwd database password
	 * @throws SQLException if a database access error occurs
	 */
	protected void setDBConnection(String url, String username, String password) throws SQLException {
		// Connect to the DB
		if (username != null) {
			conn = DriverManager.getConnection(url, username, password);
		}
		else {
			conn = DriverManager.getConnection(url);
		}
		// Store these value for later connection recreation 
		this.url = url;
		this.username = username;
		this.password = password;
	}
	
	protected void refreshDBConnection() throws SQLException {
		try {conn.close();} catch (Exception e) {}
		setDBConnection(url, username, password);
	}
	
	/**
	 * This method is called by the constructor after a
	 * connection to the database has been established. 
	 */
	abstract protected void setup() throws SQLException;
	
	
	protected Object insert(Object name, Object fact) {
		try {
			return insertSingle(name, fact);
		}
		catch (SQLException sqle) {
			try {
				// Refresh the connection and retry.
				logger.log(Logger.WARNING, "Refreshing DB connection...");
				refreshDBConnection();
				logger.log(Logger.INFO, "DB connection correctly refreshed");
				return insertSingle(name, fact);
			}
			catch (Exception e) {
				// Log the original error
				logger.log(Logger.SEVERE,"DB error inserting DFD for agent "+((DFAgentDescription) fact).getName().getName(), sqle); 
			}
		}
		return null;
	}
	
	protected abstract Object insertSingle(Object name, Object fact) throws SQLException;
	
	protected Object remove(Object name) {
		try {
			return removeSingle(name);
		}
		catch (SQLException sqle) {
			try {
				// Refresh the connection and retry.
				logger.log(Logger.WARNING, "Refreshing DB connection...");
				refreshDBConnection();
				logger.log(Logger.INFO, "DB connection correctly refreshed");
				return removeSingle(name);
			}
			catch (Exception e) {
				// Log the original error
				logger.log(Logger.SEVERE,"DB error removing DFD for agent "+((AID) name).getName(), sqle); 
			}
		}
		return null;
	}

	protected abstract Object removeSingle(Object name) throws SQLException;
	
	public List search(Object template, int maxResult) {
		try {
			return searchSingle(template, maxResult);
		}
		catch (SQLException sqle) {
			try {
				// Refresh the connection and retry.
				logger.log(Logger.WARNING, "Refreshing DB connection...");
				refreshDBConnection();
				logger.log(Logger.INFO, "DB connection correctly refreshed");
				return searchSingle(template, maxResult);
			}
			catch (Exception e) {
				// Log the original error
				e.printStackTrace();
				logger.log(Logger.SEVERE,"DB error during search operation.", sqle); 
			}
		}
		return new ArrayList();
	}

	protected abstract List searchSingle(Object template, int maxResult) throws SQLException;
	
	public KBIterator iterator(Object template) {
		try {
			return iteratorSingle(template);
		}
		catch (SQLException sqle) {
			try {
				// Refresh the connection and retry.
				logger.log(Logger.WARNING, "Refreshing DB connection...");
				refreshDBConnection();
				logger.log(Logger.INFO, "DB connection correctly refreshed");
				return iteratorSingle(template);
			}
			catch (Exception e) {
				// Log the original error
				e.printStackTrace();
				logger.log(Logger.SEVERE,"DB error during iterated search operation.", sqle); 
			}
		}
		return new EmptyKBIterator();
	}

	protected abstract KBIterator iteratorSingle(Object template) throws SQLException;
	
	public void subscribe(Object template, SubscriptionResponder.Subscription s) throws NotUnderstoodException{
		try {
			subscribeSingle(template, s);
		}
		catch (SQLException sqle) {
			try {
				// Refresh the connection and retry.
				logger.log(Logger.WARNING, "Refreshing DB connection...");
				refreshDBConnection();
				logger.log(Logger.INFO, "DB connection correctly refreshed");
				subscribeSingle(template, s);
			}
			catch (Exception e) {
				// Log the original error
				e.printStackTrace();
				logger.log(Logger.SEVERE,"DB error during iterated search operation.", sqle); 
			}
		}
	}

	protected abstract void subscribeSingle(Object template, SubscriptionResponder.Subscription s) throws SQLException, NotUnderstoodException;
	
	// Note that getSubscriptions() is only called just after a registration/deregistration/modification -->
	// The connection refresh process is useless in this case.
	public abstract Enumeration getSubscriptions();
	
	public void unsubscribe(SubscriptionResponder.Subscription s) {
		try {
			unsubscribeSingle(s);
		}
		catch (SQLException sqle) {
			try {
				// Refresh the connection and retry.
				logger.log(Logger.WARNING, "Refreshing DB connection...");
				refreshDBConnection();
				logger.log(Logger.INFO, "DB connection correctly refreshed");
				unsubscribeSingle(s);
			}
			catch (Exception e) {
				// Log the original error
				e.printStackTrace();
				logger.log(Logger.SEVERE,"DB error during iterated search operation.", sqle); 
			}
		}
	}

	protected abstract void unsubscribeSingle(SubscriptionResponder.Subscription s) throws SQLException;
	
	
	/**
	 * Inner class EmptyKBIterator
	 */
	protected class EmptyKBIterator implements KBIterator {
		public boolean hasNext() {
			return false;
		}
		
		public Object next() {
			throw new NoSuchElementException("");
		}
		
		public void remove() {
		}
		
		public void close() {
		}		
	} // END of inner class EmptyKBIterator
}
