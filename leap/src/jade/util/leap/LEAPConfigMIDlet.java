/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.util.leap;

//#J2SE_EXCLUDE_FILE
//#PJAVA_EXCLUDE_FILE

import java.util.Enumeration;
import java.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

/**
 * This MIDlet allows to create/modify a record-store containing
 * leap-properties.
 * 
 * @author and copyright c2001, marc schlichte, ICM-SIEMENS
 * @author steffen rusitschka, Siemens AG
 * @author Makram Bouzid, Motorola
 * @author Jerome Picault, Motorola
 * @version $Date$ $Release: 1.3 $
 */
public class LEAPConfigMIDlet extends MIDlet implements CommandListener {
    private Display                       display;
    private String                        recordStoreName;
    private boolean                       saveProp = 
        false;    // Added by M.B.(Motorola Paris), 08/20/01
    private Form                          form;
    private TextField                     nameField;
    private javax.microedition.lcdui.List list;
    private Form                          RSform;
    private ChoiceGroup                   LRStoresChoice;
    private Form                          ManuForm;
    private TextField                     mainField;
    private TextField                     routerField;
    private TextField                     imtpField;
    private TextField                     icpField;
    private Form                          FinalForm;
    private Properties                    currentProps;
    private Form                          ShPform;
    private Form                          addForm;
    private TextField                     keyField;

    /**
     * Constructor declaration
     * 
     */
    public LEAPConfigMIDlet() {
        display = Display.getDisplay(this);
    }

    /**
     * Method declaration
     * 
     * @see
     */
    public void startApp() {
        chooseTargetRecordStore();
    } 

    /**
     * Method declaration
     * 
     * @see
     */
    public void pauseApp() {

        // nothing
    } 

    /**
     * Method declaration
     * 
     * @param unconditional
     * 
     * @see
     */
    public void destroyApp(boolean unconditional) {

        // nothing
    } 

    /**
     * Method declaration
     *
     * @param c
     * @param d
     *
     * @see
     */
    public void commandAction(Command c, Displayable d) {
        if (d == form) {
            if (c == proceedCommand) {
                recordStoreName = nameField.getString();

                ThreadMenuListner thr0 = new ThreadMenuListner(0);

                thr0.start();
            } 
            else {
                shutdown("EXIT");
            } 
        } 
        else if (d == list) {
            if (c == list.SELECT_COMMAND || c == selectCommand) {
                String selected = list.getString(list.getSelectedIndex());

		// if (selected == HTTP_PROP_SOURCE) {

                    // chooseHTTPAddress();
                //} 
                //else 
                if (selected == RS_PROP_SOURCE) {
                    chooseSourceRecordStore();
                } 
                else if (selected == MANUAL_PROP_SOURCE) {
                    chooseMainAndLocal();
                } 
            } 
            else {
                shutdown("EXIT");
            } 
        } 
        else if (d == RSform) {
            if (c == proceedCommand) {
                int               selectedRS = 
                    LRStoresChoice.getSelectedIndex();
                Properties        props = 
                    loadProperties(LRStoresChoice.getString(selectedRS));
                ThreadMenuListner thr1 = new ThreadMenuListner(1, props);

                thr1.start();
            } 
            else {
                shutdown("EXIT");
            } 
        } 
        else if (d == FinalForm) {
            if (c == otherRSCommand) {
                chooseTargetRecordStore();
            } 
            else {
                exit();
            } 
        } 
        else if (d == ShPform) {
            if (c == addCommand) {
                addForm = new Form("Add Property");
                keyField = new TextField("Name of Prop", "", 20, 
                                         TextField.ANY);

                addForm.append(keyField);
                addForm.addCommand(proceedCommand);
                addForm.addCommand(exitCommand);
                addForm.setCommandListener(LEAPConfigMIDlet.this);
                display.setCurrent(addForm);
            } 
            else if (c == saveCommand) {
                displayWait("Saving " + recordStoreName + " RecordStore");
                updateProps(currentProps);
                finish(true);
            } 
            else if (c == deleteCommand) {
                displayWait("Deleting " + recordStoreName + " RecordStore!");

                ThreadMenuListner thr2 = new ThreadMenuListner(2);

                thr2.start();
            } 
            else if (c == exitCommand) {
                finish(false);
            } 
        } 
        else if (d == addForm) {
            if (c == proceedCommand) {
                ShPform.append(new TextField(keyField.getString(), "", 100, 
                                             TextField.ANY));
            } 

            display.setCurrent(ShPform);
        } 
    } 

    private static final String  HTTP_PROP_SOURCE = "Download via HTTP";
    private static final String  RS_PROP_SOURCE = "Copy from existing RS";
    private static final String  MANUAL_PROP_SOURCE = "Enter manually";
    private static final Command exitCommand = new Command("Exit", 
            Command.EXIT, 1);
    private static final Command saveCommand = new Command("Save RecordStore", 
            Command.OK, 3);
    private static final Command deleteCommand = 
        new Command("Delete RecordStore", Command.SCREEN, 3);
    private static final Command addCommand = new Command("Add Property", 
            Command.SCREEN, 3);
    private static final Command selectCommand = new Command("Select", 
            Command.OK, 1);
    private static final Command proceedCommand = new Command("Proceed", 
            Command.OK, 1);
    private static final Command otherRSCommand = new Command("Home", 
            Command.OK, 1);

    /**
     * Method declaration
     * 
     * @see
     */
    private void chooseTargetRecordStore() {
        form = new Form("LEAPConfigMIDlet");
        nameField = new TextField("Choose Target RecordStore:", "LEAP", 200, 
                                  TextField.ANY);

        form.append(nameField);
        form.addCommand(proceedCommand);
        form.addCommand(exitCommand);
        form.setCommandListener(LEAPConfigMIDlet.this);
        display.setCurrent(form);
    } 

    /**
     * Method declaration
     * 
     * @see
     */
    private void choosePropSource() {
        list = new javax.microedition.lcdui.List("Choose Source", 
                                                 Choice.IMPLICIT);

        list.addCommand(exitCommand);
        list.addCommand(selectCommand);
        list.setCommandListener(LEAPConfigMIDlet.this);
        //list.append(HTTP_PROP_SOURCE, null);

        // The following option is proposed only if the record store is not
        // empty.
        if ((RecordStore.listRecordStores()) != null) {
            list.append(RS_PROP_SOURCE, null);
        } 

        list.append(MANUAL_PROP_SOURCE, null);
        display.setCurrent(list);
    } 

    /**
     * Method declaration
     * @see
     */
    private void chooseSourceRecordStore() {
        RSform = new Form("Choose RecordStore");

        String[] stores = RecordStore.listRecordStores();

        LRStoresChoice = new ChoiceGroup("RecordStore list:", 
                                         Choice.EXCLUSIVE, stores, null);

        RSform.append(LRStoresChoice);
        RSform.addCommand(proceedCommand);
        RSform.addCommand(exitCommand);
        RSform.setCommandListener(LEAPConfigMIDlet.this);
        display.setCurrent(RSform);
    } 

    /**
     * Method declaration
     * 
     * @see
     */
    private void chooseMainAndLocal() {
        Properties props = defaultProperties();

        ManuForm = new Form("Enter Properties");

        ThreadMenuListner thr1 = new ThreadMenuListner(1, props);

        thr1.start();
        display.setCurrent(ManuForm);
    } 

    /**
     * this one must run in a separate thread!
     */
    private void modifyProps(Properties props) {
        displayFinalInformation("Result:", 
                                displayAndStoreProperties(props) 
                                ? "Props stored" : "Props not stored");
    } 

    /**
     * Method declaration
     * 
     * @param txt
     * 
     * @see
     */
    private void displayWait(String txt) {
        final Form form = new Form("LEAPConfigMIDlet");

        form.append(txt);
        form.append("\nPLEASE WAIT...");
        display.setCurrent(form);
    } 

    /**
     * Method declaration
     * 
     * @param caption
     * @param body
     * 
     * @see
     */
    private void displayFinalInformation(String caption, String body) {
        FinalForm = new Form(caption);

        FinalForm.append(body);
        FinalForm.addCommand(exitCommand);
        FinalForm.addCommand(otherRSCommand);
        FinalForm.setCommandListener(LEAPConfigMIDlet.this);
        display.setCurrent(FinalForm);
    } 

    /**
     * this one must run in a separate thread!
     */
    private synchronized boolean displayPropDialog(Properties props_) {
        currentProps = props_;
        ShPform = new Form("Show-Prop-Source");

        ShPform.addCommand(saveCommand);
        ShPform.addCommand(addCommand);
        ShPform.addCommand(deleteCommand);
        ShPform.addCommand(exitCommand);
        ShPform.setCommandListener(LEAPConfigMIDlet.this);

        Enumeration keys = currentProps.keys();

        while (keys != null && keys.hasMoreElements()) {
            String key = (String) keys.nextElement();

            ShPform.append(new TextField(key, currentProps.getProperty(key), 
                                         100, TextField.ANY));
        } 

        display.setCurrent(ShPform);

        // wait for call of finish
        try {
            wait();
        } 
        catch (InterruptedException ex) {}

        return saveProp;    // Modified by M.B. (Motorola Paris), 08/20/01
    } 

    /**
     * Method declaration
     * 
     * @see
     */
    private void updateProps(Properties props) {

        // inspect each text-field of form and set-prop on props
        for (int i = 0; i < ShPform.size(); ++i) {
            TextField field = (TextField) ShPform.get(i);

            if ((field.getString()).length() != 0) {
                props.setProperty(field.getLabel(), field.getString());
            } 
            else {
                props.remove(field.getLabel());
            } 
        } 
    } 

    /**
     * Method declaration
     * 
     * @param val
     * 
     * @see
     */
    private void finish(boolean val) {
        saveProp = val;   
        synchronized (LEAPConfigMIDlet.this) {
            LEAPConfigMIDlet.this.notify();
        } 
    } 

    /**
     * Method declaration
     * 
     * @return
     * 
     * @see
     */
    private Properties defaultProperties() {
        Properties props = new Properties();
        // set defaults (adapt this when props in leap change!)
        // ok! Modified by M.B. (Motorola Paris), 08/17/01
	props.setProperty("main-host", "140.101.173.124");
	props.setProperty("main-port", "6837");        
        props.setProperty("rungc", "true");
	// the following are default, not set.
	//props.setProperty("gui", "false");
	//props.setProperty("resource", "jade.core.LightResourceManager");
	//props.setProperty("notification", "jade.core.DummyNotificationManager");
	//props.setProperty("acc", "jade.core.LightAcc");
	//props.setProperty("mobility", "jade.core.DummyMobilityManager");
        //props.setProperty("imtp", "jade.imtp.leap.LEAPIMTPManager");
        //props.setProperty("debuggc", "false");
	//props.setProperty("routerURL", "jicp://140.101.173.52:3000");
	//props.setProperty("icp", "jade.imtp.leap.JICP.JICPKMPeer(3000,140.101.173.52)");
	//props.setProperty("main", "false");
	//props.setProperty("mainURL", "jicp://140.101.173.52:3000");
        return props;
    } 


    /**
     * Method declaration
     * 
     * @param name
     * 
     * @return
     * 
     * @see
     */
    private Properties loadProperties(String name) {
        Properties props = new Properties();
        try {
            props.load(name);
            return props;
        } 
        catch (IOException ex) {
            return null;
        } 
    } 

    /**
     * this one must run in a separate thread!
     */
    private boolean displayAndStoreProperties(Properties props) {
        if (displayPropDialog(props)) {

            // transfer props to propsRecordStore
            Properties targetProps = new Properties();

            for (Enumeration enum = props.keys(); 
                    enum != null && enum.hasMoreElements(); ) {
                String key = (String) enum.nextElement();

                targetProps.setProperty(key, props.getProperty(key));
            } 

            // store
            try {
                targetProps.store(recordStoreName);

                return true;
            } 
            catch (IOException ex) {
                return false;
            } 
        } 
        else {
            return false;
        } 
    } 

    /**
     * Method declaration
     * 
     * @param msg
     * 
     * @see
     */
    private void shutdown(String msg) {
        displayFinalInformation("Information:", msg);
    } 

    /**
     * Method declaration
     * 
     * @see
     */
    private void exit() {
        destroyApp(true);
        notifyDestroyed();
    } 

    /*
     * This class is added for optimisation purposes (to reduce the number of
     * generated classes). Otherwise, it has no really significance.
     */

    /**
     * Class declaration
     *
     * @author LEAP
     */
    public class ThreadMenuListner extends Thread {
        int        choosedOption;    // The task of this thread, specified during

        // its creation
        Properties props;

        /**
         * Constructor declaration
         *
         * @param choice
         *
         */
        public ThreadMenuListner(int choice) {
            choosedOption = choice;
        }

        /**
         * Constructor declaration
         *
         * @param choice
         * @param props
         *
         */
        public ThreadMenuListner(int choice, Properties props) {
            choosedOption = choice;
            this.props = props;
        }

        /**
         * Method declaration
         *
         * @see
         */
        public void run() {
            switch (choosedOption) {

                case 0:    // testing wether the name of the record store specified

                    // by the user already exists in the store or not, in
                    // order to show the right menu
                    props = loadProperties(recordStoreName);

                    if (props != null) {
                        modifyProps(props);
                    } 
                    else {
                        choosePropSource();
                    } 

                    break;

                case 1:    // Instructions to do when creating a record store from

                    // an already existing one or manually
                    if (props == null) {
                        props = defaultProperties();
                    } 

                    modifyProps(props);

                    break;

                case 2:    // Deleting a record store
                    try {
                        RecordStore.deleteRecordStore(recordStoreName);
                    } 
                    catch (RecordStoreException ex) {}

                    finish(false);

                    break;
            }
        } 

    }

}

