/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

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

package jade.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.*;

import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Container;
	
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Properties;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.net.InetAddress;
import java.net.UnknownHostException;

import jade.PropertyType;
import jade.Boot;
import jade.BootException;

/**
   
   This class create the gui for the jade configuration
   
   @author Tiziana Trucco - CSELT S.p.A.
   @version $Date$ $Revision$

 */
 
 
 
 public class BootGUI extends JDialog
 {
  static String extension = "conf";
  static String title = "--JADE Properties--";
  
  /**
  @serial
  */
  List propertiesVector; 
  /**
  @serial
  */
  boolean modified;
  /**
  @serial
  */
  File currentDir = null;
  /**
  @serial
  */
  JTextField statusField= new JTextField();
	/**
  @serial
  */
  JPanel topPanel = new JPanel();
	/**
  @serial
  */
  JPanel propertyPanel = new JPanel();
	/**
  @serial
  */
  JPanel buttonPanel = new JPanel();
	/**
  @serial
  */
  String fileOpened = null;
	/**
  @serial
  */
  Properties outProp = null;
 
  BootGUI thisBootGui;
  
	//This class create a JPanel for a single property
 	class singlePanel extends JPanel
 	{
 	
 	  singlePanel()
 	  {
 	  	super();
 	  }
 	 
 		JPanel newSinglePanel(PropertyType property)
 		{
 		 	JPanel mainP = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		  Border etched = BorderFactory.createEtchedBorder(Color.white, Color.gray);      
 			
 		  String name = property.getName();
 			JLabel nameLabel = new JLabel(name.toUpperCase());
 		  nameLabel.setPreferredSize(new Dimension(80,26));
 			nameLabel.setMaximumSize(new Dimension(80,26));
      nameLabel.setMinimumSize(new Dimension(80,26));

 			mainP.add(nameLabel);
 			
 			String type = property.getType();
 			JCheckBox valueBox;
 			JTextField valueText;
 			
 			// if the property has a command line value than it is used
 			// otherwise is used the default value
 			String commandValue = property.getCommandLineValue();
 		
 			String defaultValue = property.getDefaultValue();
 			String value = defaultValue;
 			
 			if(commandValue != null)
 			  { 
 			  	value = commandValue;
 			  	modified = true;
 			  }
 		
 			if(type.equalsIgnoreCase(PropertyType.BOOLEAN_TYPE))
 			{
 				 valueBox = new JCheckBox();
 				 valueBox.setSelected((new Boolean(value)).booleanValue());
 				 valueBox.setToolTipText(property.getToolTip());
 				 
 				 mainP.add(valueBox);
 			}
 			else
 			{
 				valueText = new JTextField();
 				valueText.setBorder(etched);
 				valueText.setPreferredSize(new Dimension(180,26));
 				valueText.setMaximumSize(new Dimension(180,26));
        valueText.setMinimumSize(new Dimension(180,26));

 	      valueText.setText(value);
 		 		valueText.setToolTipText(property.getToolTip());
 				
 				mainP.add(valueText);
 			}
 			
 			return mainP;
 				
 		}
 	}
 	
 	
  //This class extends FileFilter in order to show only files with extension ".conf".
 	private class myFileFilter extends FileFilter 
 	{
 		public boolean accept(File f)
 		{
 			if(f.isDirectory())
 			{
 				return true;
 			}
 			
 			String ext = getExtension(f);
 			if(ext != null)
 			{
 				if(ext.equals (extension))
 					return true;
 					else
 					return false;
 			}
 			return false;
 		}
 		
 	  public String getDescription()
 		{
 			return"Configuration file (*."+ extension + ")";
 		}
 		
 		private String getExtension(File f)
 		{
 			String ext = null;
 			String s = f.getName();
 			int i = s.lastIndexOf('.');
 			if(i>0 && i < s.length()-1 )
 				ext = s.substring(i+1).toLowerCase();
 			return ext;	
 		}
 	}
 	
 	
 	public BootGUI()
 	{
 		super();
 		thisBootGui = this;
 	}
 	
 	 	
 	public Properties ShowBootGUI(List properties)
 	 {
 	 	
 	 	this.propertiesVector = properties;
   
 	 	setTitle("JADE Configuration");
 	 	
 	  Border raisedbevel = BorderFactory.createRaisedBevelBorder();
 	                        
 	 	JPanel mainPanel = new JPanel();
 	 	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
 	 	mainPanel.setBorder(raisedbevel);
 	 	
 	 
 	 	topPanel.setLayout(new BoxLayout(topPanel,BoxLayout.Y_AXIS));
 	 	
 	  propertyPanel.setLayout(new BoxLayout(propertyPanel, BoxLayout.Y_AXIS));
 	  
 	  Iterator prop = properties.iterator();
 	  
 	  while(prop.hasNext())
 	  {
 	  	singlePanel propPanel = new singlePanel();
 	  	PropertyType p = (PropertyType) prop.next();
 	  	
 	  	JPanel panel = propPanel.newSinglePanel(p);
 	  	propertyPanel.add(panel);
 	  }
 	  
 	 
 	  ////////////////////////
		// Status message
		////////////////////////
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout());
		
		statusField.setEditable(false);
		statusField.setPreferredSize(new Dimension(200,50));
		statusField.setMaximumSize(new Dimension(200,50));
		statusField.setMinimumSize(new Dimension(200,50));
		
		if(modified)
			statusField.setText("Warning: default parameter overriden by \ncommand line ones");
		statusPanel.add(statusField, BorderLayout.CENTER);

 	  
 	  buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
 	  
 	  JButton openB = new JButton("Open File");
 	  openB.setToolTipText("Read configuration from file");
 	  openB.addActionListener(new ActionListener(){
 	  
 	  	public void actionPerformed(ActionEvent e){
 	  	
 	  		String param = (String)e.getActionCommand();
 	  		if(param.equals("Open File"))
 	  		{
 	  		  JFileChooser chooser = new JFileChooser();
 	  		  chooser.setFileFilter(new myFileFilter());
 	  		  
 	  		  if(currentDir != null)
 	  		  	chooser.setCurrentDirectory(currentDir);
 	  		  	
 	  		  int returnVal = chooser.showOpenDialog(null);
 	  		  if(returnVal == JFileChooser.APPROVE_OPTION)
 	  		  {
 	  		    currentDir = chooser.getCurrentDirectory();
 	  		    String fileName = chooser.getSelectedFile().getAbsolutePath();
 	  		    try{
 	  		    	 loadPropertiesFromFile(fileName);
 	  		    	 fileOpened = fileName;
 	  		       updateProperties();
 	  		       
 	  		    }catch(FileNotFoundException fe){
 	  		      System.out.println("File not found Exception");
 	  		    }catch(IOException ioe){
 	  		      System.out.println("IO Exception");
 	  		    }
 	  		  }
 	  		  
 	  			
 	  		}
 	  	}
 	  });
 	  
 	  addWindowListener(new WindowAdapter(){
 	  
 	  	public void windowClosing(WindowEvent e)
 	  	{
 	  		System.exit(0);
 	  	}
 	  });
 	  
 	  buttonPanel.add(openB);
 	  
 	  
 	  JButton saveB = new JButton("Save File");
 	  saveB.setToolTipText("Save configuration into a file");
 	  saveB.addActionListener(new ActionListener(){
 	   public void actionPerformed(ActionEvent e)
 	   {
 	   	String param = (String)e.getActionCommand();
 	   	if(param.equals("Save File"))
 	   	  {
 	   	  	Properties propToSave = extractPropertiesFromGui();
 	   	  	//propToSave.list(System.out);
 	   	  	
 	   	  	try
 	   	  	{
 	   	  		Boot.checkProperties(propToSave);
 	   	  	 	JFileChooser chooser = new JFileChooser();
 	   	  	  chooser.setFileFilter(new myFileFilter());
 	   	  	 	
 	   	  	  if(currentDir !=null)
 	   	  	 		chooser.setCurrentDirectory(currentDir);
 	   	  	 	
 	   	  	 	int returnVal = chooser.showSaveDialog(null);
 	   	  	 	
 	   	  	 	if(returnVal == JFileChooser.APPROVE_OPTION)
 	   	  	 	{ 
 	   	  	 		currentDir = chooser.getCurrentDirectory();
 	   	  	 		String fileName = chooser.getSelectedFile().getAbsolutePath();
 	   	  	 		boolean ext = hasExtension(fileName);
 	   	  	 		
 	   	  	 		
 	   	  	 		if(ext==false)
 	   	  	 		   fileName = fileName.concat(".conf");
 	   	  	 		fileOpened = fileName;  
 	   	        try
 	   	        {
 	   	        	FileOutputStream out = new FileOutputStream(fileName);
 	   	        	propToSave.store(out,title);
 	   	        	out.close();
 	   	          outProp = propToSave;
 	   	        	//dispose();
 	   	        	
 	   	        }catch(FileNotFoundException e1){
 	   	        	System.out.println("File not found Exception");
 	   	        }catch(IOException e2){
 	   	        	System.out.println("IO exception");
 	   	        }
 	   	  	 	}
 	   	  	}catch (BootException be)
 	   	  	{
 	   	  		statusField.setText(be.getMessage());
 	   	  	}

 	   	  }
 	   }
 	  });
 	  buttonPanel.add(saveB);

 	  
 	  JButton runB = new JButton("Run");
 	  runB.setToolTipText("Launch the system");
 	  runB.addActionListener(new ActionListener(){
 	   public void actionPerformed(ActionEvent e)
 	   {
 	   	String param = (String)e.getActionCommand();
 	   	
 	   	if(param.equals("Run"))
 	   	{
 	   		Properties propToSave = extractPropertiesFromGui();
 	   		
 	   	  try
 	   	  {
 	   	  		Boot.checkProperties(propToSave);
 	   	  		boolean different = false;
 	   	  		 	   	  		
 	   	  		if(fileOpened !=null)
 	   	  		{
 	   	  			// compares the properties from gui with those in the file
 	   	  			Properties p = readPropertiesFromFile(fileOpened);
 	   	  			//p.list(System.out);
 	   	  			different = compareProperties(propToSave,p);
 	   	  		}
 	   	  		
 	   	  		if(different || fileOpened == null)
 	   	  		{
 	   	  		  Object[] options = {"Yes", "No"};
      				int val = JOptionPane.showOptionDialog(thisBootGui,"Would you save this configuration ?", "MESSAGE", JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE, null, options,options[0]);
      				
      			
      				if(val == JOptionPane.YES_OPTION)
      					
      					{
 	   	  			   //Save file
 	   	  			   JFileChooser chooser = new JFileChooser();
 	   	  			   chooser.setFileFilter(new myFileFilter());
 	   	  			   
 	   	  	 	     if(currentDir !=null)
 	   	  	 		    chooser.setCurrentDirectory(currentDir);
 	   	  	 	     int returnVal = chooser.showSaveDialog(null);
 	   	  	 	     if(returnVal == JFileChooser.APPROVE_OPTION)
 	   	  	 	     { 
 	   	  	 	  	   currentDir = chooser.getCurrentDirectory();
 	   	  	 	  	   String fileName = chooser.getSelectedFile().getAbsolutePath();
 	   	  	 	  	   boolean ext = hasExtension(fileName);
 	   	  	 	  	   
 	   	  	 	  	   if(ext == false)
 	   	  	 	  	   	fileName = fileName.concat(".conf");
 	   	  	 	  	   	
 	   	             try
 	   	             {
 	   	          	   FileOutputStream out = new FileOutputStream(fileName);
 	   	        	     propToSave.store(out,title);
 	   	          	   out.close();
 	   	        	
 	   	              }catch(FileNotFoundException e1){
 	   	        	      System.out.println("File not found exception");
 	   	              }catch(IOException e2){
 	   	        	      System.out.println("IO exception");}
 	   	  	 	      }
 	   	  		    }
 	   	  		}
 	   	  	
 	   	  		outProp = propToSave;
 	   	  		dispose();
 	   	  }catch(BootException be){
 	   	  	statusField.setText(be.getMessage());
 	   	  }catch (FileNotFoundException e1){
 	   	  	System.out.println("File not found");
 	   	  }catch(IOException e2){
 	   	  	System.out.println("Io Exception");
 	   	  }

 	   	}
 	   }
 	  });
 	  buttonPanel.add(runB);

 	  JButton exitB = new JButton("Exit");
 	  exitB.setToolTipText("Exit without executing");
 	  exitB.addActionListener(new ActionListener(){
 	   public void actionPerformed(ActionEvent e)
 	   {
 	   	String param =(String)e.getActionCommand();
 	   	if(param.equals("Exit"))
 	   		System.exit(0);
 	   }
 	  });
 	  
 	  buttonPanel.add(exitB);
 	  
 	  JButton helpB = new JButton("Help");
 	  helpB.addActionListener(new ActionListener(){
 	  	public void actionPerformed(ActionEvent e)
 	  	{
 	  		String param = (String)e.getActionCommand();
 	  		if(param.equals("Help"))
 	  			{
 	  				TreeHelp help = new TreeHelp(thisBootGui,"Boot Help", "help/BOOTGUI.html");
	         // must insert the listener for the close action
	        
	         help.setVisible(true);
	         help.requestFocus();
 	  			}

 	  	}
 	  });

 	  buttonPanel.add(helpB);
 	  topPanel.add(buttonPanel);
 	  topPanel.add(propertyPanel);

 	  mainPanel.add(topPanel);
 	   		
		mainPanel.add(statusPanel);
   
 	  getContentPane().add(mainPanel,BorderLayout.CENTER);
 	  setResizable(false);
 	  setModal(true);
 	  
 	  ShowCorrect();
 	  
 	  return outProp;
 	 }
 	 
 	
 	 //Extract the values of the configuration properties from the GUI.
 	 private Properties extractPropertiesFromGui()
 	 {
 	 	Properties out = new Properties();
  	
 	 	int size = propertyPanel.getComponentCount();
 	 	
 	 	Iterator it = propertiesVector.iterator();
 	 	
 	 	while(it.hasNext())
 	 	{
		  PropertyType prop = (PropertyType)it.next();
		  String name = prop.getName();
		  String type = prop.getType();
 	 	  String defaultVal = prop.getDefaultValue();
 	 	  boolean found = false;
 	 	  for(int i = 0; i<size && !found; i++)
 	 		{
 	 			JPanel singlePanel = (JPanel)propertyPanel.getComponent(i);
 	 			JLabel label = (JLabel)singlePanel.getComponent(0);
 	 			
 	 			if(name.equalsIgnoreCase(label.getText()))
 	 				{
 	 					found = true;
 	 					
 	 					if(type.equalsIgnoreCase(PropertyType.BOOLEAN_TYPE))
 	 					{
 	 						//JCheckBox
 	 						JCheckBox box = (JCheckBox)singlePanel.getComponent(1);
 	 						boolean value = false;
 	 						
 	 						if(box.isSelected())
 	 								value = true;
 	 						String stringValue = (new Boolean(value)).toString();
 	 						out.put(name.toLowerCase(),stringValue);
  	          
 	 					}
 	 					else
 	 					{
 	 						//JTextField
 	 						JTextField textField = (JTextField)singlePanel.getComponent(1);
 	 						String text = textField.getText();
 	 						//if the user not specificy a value the default one is saved.
 	 						if (text.length() == 0)
 	 							text = defaultVal;
 	 						out.put(name.toLowerCase(),text);
 	 						
 	 					}
 	 				}
 	 		}
 	 	}
 	 	return out;
 	 	
 	 }
 	 
 	 // Compares two properties 
 	 private boolean compareProperties(Properties p1, Properties p2)
 	 {
 	 
 	 	Enumeration keys = p1.propertyNames();
 	 	boolean modified = false;
 	 	
 	 	while(keys.hasMoreElements() && !modified)
 	 	{
 	 		String k1 = (String)keys.nextElement();
 	 		String v1 = p1.getProperty(k1);
 	 		String v2 = p2.getProperty(k1);
 	 	
 	 		if(v2 != null)
 	 	  	modified = !(v1.equalsIgnoreCase(v2));
 	 	}
 	 
 	 	return modified;
 	 	
 	 }
 	 
 	 
 	 // This method update the gui when a new file is opened.
 	 // For every property it sets in the vector of property the value read in the file or if it is absent the default value. 
 	 private void updateProperties()
 	 {
 	 	int size = propertyPanel.getComponentCount();
 	 	
 	 	Iterator it = propertiesVector.iterator();
 	 	
 	 	while(it.hasNext())
 	 	{
 	 		PropertyType prop = (PropertyType)it.next();
 	 		String name = prop.getName();
 	 		String type = prop.getType();
 	 	  String fileValue = prop.getFileValue();
 	 	  
 	 	  //if no corresponding fileValue then using default value
 	 	  fileValue = (fileValue != null ? fileValue : prop.getDefaultValue());
 	 	  boolean found = false;
 	 	  
 	 	
 	 	  for(int i = 0; i<size && !found; i++)
 	 		{
 	 			JPanel singlePanel = (JPanel)propertyPanel.getComponent(i);
 	 			JLabel label = (JLabel)singlePanel.getComponent(0);
 	 		  
 	 			if(name.equalsIgnoreCase(label.getText()))
 	 				{
 	 					found = true;
 	 					
 	 					if(type.equalsIgnoreCase(PropertyType.BOOLEAN_TYPE))
 	 					{
 	 						//JCheckBox
 	 						JCheckBox box = (JCheckBox)singlePanel.getComponent(1);
 	 						
 	 						if (fileValue.equalsIgnoreCase("true"))
 	 							box.setSelected(true);
 	 						else 
 	 						  box.setSelected(false);
  	          
 	 					}
 	 					else
 	 					{
 	 						//JTextField
 	 						JTextField textField = (JTextField)singlePanel.getComponent(1);
 	 					  
 	 						textField.setText(fileValue);
 	 					}
 	 				}
 	 		}
 	 	}
 	 }
 	 
 	 //To show the gui in the center of the screen
 	 
 	 private void ShowCorrect() 
 	 {
    pack();
    //setSize(300, 300);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int centerX = (int)screenSize.getWidth() / 2;
    int centerY = (int)screenSize.getHeight() / 2;
    Dimension sizePanel = getSize();
    int x = (new Double(sizePanel.getWidth())).intValue() / 2;
    int y = (new Double(sizePanel.getHeight())).intValue() / 2;
    setLocation(centerX - x, centerY - y);
    
    setVisible(true);
    toFront();
 	 }
 	 
   //This method read the properties from file and update the vector of properties.
  	 private void loadPropertiesFromFile(String fileName) throws FileNotFoundException, IOException
   {
   
  	Properties p = readPropertiesFromFile(fileName);
  	
  	// update the properties in the vector of properties
  	// for every property set the value read in the file and set the command line value to null.
  	
  	Enumeration e = p.propertyNames();
  
  	Iterator it = propertiesVector.iterator();
  	while(e.hasMoreElements())
  	{	
  		
  	  boolean found = false;
	    String name = (String)e.nextElement();    
  	 
  	  while(it.hasNext() && !found)
  	  {
  	  	PropertyType pt = (PropertyType)it.next();
  	  	if(pt.getName().equalsIgnoreCase(name))
  	  		{
			    found = true;
			    pt.setCommandLineValue(null);
			    pt.setFileValue(p.getProperty(name));
  	  		}
  	  }
      it = propertiesVector.iterator(); 
  	}
  	
  
  	
  }
  
  //This method read the properties from a specific file.
  private Properties readPropertiesFromFile(String fileName) throws FileNotFoundException, IOException
  {
  	Properties p = new Properties();
  	FileInputStream in = new FileInputStream(fileName);
  	p.load(in);
  	in.close();
  
  	return p;

  }

 	//This method verify if the file written by the user has the right extension. (Used in the save action)
  private boolean hasExtension(String fileName)
 	{
 	  String ext = null;
 	  boolean out = false;
 	  
 	  int i = fileName.lastIndexOf('.');
 	  
 	  if(i>0 && i<fileName.length()-1)
 	     ext = fileName.substring(i+1);
 	     
 	  if (ext != null)
 	  	if(ext.equalsIgnoreCase("conf"))
 	  		out= true;
 	  		
 	  return out;		
 	} 
 }
