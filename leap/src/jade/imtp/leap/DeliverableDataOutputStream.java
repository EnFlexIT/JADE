/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
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
 * Copyright (C) 2001 Siemens AG.
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

package jade.imtp.leap;

import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.ReceivedObject;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import jade.mtp.MTPDescriptor;
import jade.security.*;
import jade.security.dummy.*;
import jade.imtp.leap.JICP.JICPAddress;

/**
 * This class implements a data output stream serializing
 * Deliverables, primitive types and J2SE class types that are considered as
 * primitive type to a given byte array according to the LEAP surrogate
 * serialization mechanism.
 * 
 * @author Michael Watzke
 * @author Giovanni Caire
 * @author Nicolas Lhuillier
 * @version 1.0, 13/05/2002
 */
class DeliverableDataOutputStream extends DataOutputStream {

    private StubHelper myStubHelper;

    /**
     * Constructs a data output stream that is serializing Deliverables to a
     * given byte array according to the LEAP surrogate serialization
     * mechanism.
     */
    public DeliverableDataOutputStream(StubHelper sh) {
        super(new ByteArrayOutputStream());
        myStubHelper = sh;
    }

    /**
     * Get the byte array that contains the frozen, serialized Deliverables
     * written to this data output stream.
     * @return the byte array containing the serialized Deliverables written
     * to this data output stream
     */
    public byte[] getSerializedByteArray() {
        return ((ByteArrayOutputStream) out).toByteArray();
    } 

    /**
     * Writes an object whose class is not known from the context to
     * this data output stream.
     * @param o the object to be written.
     * @exception LEAPSerializationException if an error occurs during
     * serialization or the object is an instance of a class that cannot be
     * serialized.
     */
    public void writeObject(Object o) throws LEAPSerializationException {
        try {
            if (o != null) {

                // Presence flag true
                writeBoolean(true);

                // Directly handle serialization of classes that must be
                // serialized more frequently
                if (o instanceof ACLMessage) {                   // ACLMessage
                    writeByte(Serializer.ACL_ID);
                    serializeACL((ACLMessage) o);
                } 
                else if (o instanceof AID) {                     // AID
                    writeByte(Serializer.AID_ID);
                    serializeAID((AID) o);
                } 
                else if (o instanceof String) {                  // String
                    writeByte(Serializer.STRING_ID);
                    writeUTF((String) o);
                } 
                else if (o instanceof RemoteContainerProxy) {    // RemoteContainerProxy
                    writeByte(Serializer.REMOTEPROXY_ID);
                    serializeProxy((RemoteContainerProxy) o);
                } 
                else if (o instanceof ContainerID) {             // ContainerID
                    writeByte(Serializer.CONTAINERID_ID);
                    serializeContainerID((ContainerID) o);
                } 
                else if (o instanceof Boolean) {                 // Boolean
                    writeByte(Serializer.BOOLEAN_ID);
                    writeBoolean(((Boolean) o).booleanValue());
                } 
                else if (o instanceof Integer) {                 // Integer
                    writeByte(Serializer.INTEGER_ID);
                    writeInt(((Integer) o).intValue());
                } 
                else if (o instanceof Date) {                    // Date
                    writeByte(Serializer.DATE_ID);
                    serializeDate((Date) o);
                } 
                else if (o instanceof String[]) {                // Array of Strings
                    writeByte(Serializer.STRINGARRAY_ID);
                    serializeStringArray((String[]) o);
                } 
                else if (o instanceof Vector) {                  // Vector
                    writeByte(Serializer.VECTOR_ID);
                    serializeVector((Vector) o);
                } 
                else if (o instanceof MTPDescriptor) {           // MTPDescriptor
                    writeByte(Serializer.MTPDESCRIPTOR_ID);
                    serializeMTPDescriptor((MTPDescriptor) o);
                } 
                else if (o instanceof MainContainer) {           // MainContainerStub
                    if (!(o instanceof MainContainerStub)) {
                        // A Stub must be serialized
                        o = myStubHelper.buildLocalStub(o);
                    } 
                    writeByte(Serializer.MAINSTUB_ID);
                    serializeMainStub((MainContainerStub) o);
                } 
                else if (o instanceof AgentContainer) {          // AgentContainerStub
                    if (!(o instanceof AgentContainerStub)) {
                        // A Stub must be serialized
                        o = myStubHelper.buildLocalStub(o);
                    }
                    writeByte(Serializer.CONTAINERSTUB_ID);
                    serializeContainerStub((AgentContainerStub) o);
                }
                else if (o instanceof ArrayList) {               // ArrayList
                    writeByte(Serializer.ARRAYLIST_ID);
                    serializeArrayList((ArrayList) o);
                }
                else if (o instanceof byte[]) {                  // Byte Array
                    writeByte(Serializer.BYTEARRAY_ID);
                    serializeByteArray((byte[]) o);
                }
                else if (o instanceof Envelope) {                // Envelope 
                    writeByte(Serializer.ENVELOPE_ID);
                    serializeEnvelope((Envelope) o);
                }
                else if (o instanceof JICPAddress) {             // JICPAddress 
                    writeByte(Serializer.JICPADDRESS_ID);
                    serializeJICPAddress((JICPAddress) o);
                }
                else if (o instanceof Properties) {              // Properties 
                    writeByte(Serializer.PROPERTIES_ID);
                    serializeProperties((Properties) o);
                }
                else if (o instanceof ReceivedObject) {          // ReceivedObject
                    writeByte(Serializer.RECEIVEDOBJECT_ID);
                    serializeReceivedObject((ReceivedObject) o);
                }
                else if (o instanceof DummyCertificate) {        // DummyCertificate
                    writeByte(Serializer.DUMMYCERTIFICATE_ID);
                    serializeDummyCertificate((DummyCertificate) o);
                }
                else if (o instanceof DummyPrincipal) {          // DummyPrincipal
                    writeByte(Serializer.DUMMYPRINCIPAL_ID);
                    serializeDummyPrincipal((DummyPrincipal) o);
                }
                else if (o instanceof CertificateFolder) {          // CertificateFolder
                    writeByte(Serializer.CERTIFICATEFOLDER_ID);
                    serializeCertificateFolder((CertificateFolder) o);
                }
                // Delegate serialization of other classes 
                // to a proper Serializer object
                else {
                    Serializer s = getSerializer(o);

                    writeByte(Serializer.DEFAULT_ID);
                    writeUTF(s.getClass().getName());
                    s.serialize(o, this);
                } 
            } // END of if (o != null)
            else {

                // Presence flag false
                writeBoolean(false);
            } 
        }  // END of try
        catch (IOException ioe) {
            throw new LEAPSerializationException("Error Serializing object "+o);
        } 
        catch (IMTPException imtpe) {
            throw new LEAPSerializationException("Can't get a Stub for object "+o);
        } 
    } 

    /**
     * Writes an AID object to this data output stream.
     * @param id the AID to be written.
     * @exception LEAPSerializationException if an error occurs during
     * serialization
     */
    public void writeAID(AID id) throws LEAPSerializationException {
        try {
            if (id != null) {
                writeBoolean(true);     // Presence flag true
                serializeAID(id);
            } 
            else {
                writeBoolean(false);    // Presence flag false
            } 
        } 
        catch (IOException ioe) {
            throw new LEAPSerializationException("Error serializing AID");
        } 
    } 

    /**
     * Writes a String object to this data output stream.
     * @param s the String to be written.
     * @exception LEAPSerializationException if an error occurs during
     * serialization
     */
    public void writeString(String s) throws LEAPSerializationException {
        try {
            if (s != null) {
                writeBoolean(true);     // Presence flag true
                writeUTF(s);
            } 
            else {
                writeBoolean(false);    // Presence flag false
            } 
        } 
        catch (IOException ioe) {
            throw new LEAPSerializationException("Error serializing String");
        } 
    } 

    /**
     * Writes a Date object to this data output stream.
     * @param d the Date to be written.
     * @exception LEAPSerializationException if an error occurs during
     * serialization
     */
    public void writeDate(Date d) throws LEAPSerializationException {
        try {
            if (d != null) {
                writeBoolean(true);     // Presence flag true
                serializeDate(d);
            } 
            else {
                writeBoolean(false);    // Presence flag false
            } 
        } 
        catch (IOException ioe) {
            throw new LEAPSerializationException("Error serializing Date");
        } 
    } 

    /**
     * Writes a StringBuffer object to this data output stream.
     * @param s the StringBuffer to be written.
     * @exception LEAPSerializationException if an error occurs during
     * serialization
     */
    public void writeStringBuffer(StringBuffer s) throws LEAPSerializationException {
        try {
            if (s != null) {
                writeBoolean(true);     // Presence flag true
                serializeStringBuffer(s);
            } 
            else {
                writeBoolean(false);    // Presence flag false
            } 
        } 
        catch (IOException ioe) {
            throw new LEAPSerializationException("Error serializing String");
        } 
    } 

    /**
     * Writes a Vector object to this data output stream.
     * @param v the Vector to be written.
     * @exception LEAPSerializationException if an error occurs during
     * serialization
     */
    public void writeVector(Vector v) throws LEAPSerializationException {
        try {
            if (v != null) {
                writeBoolean(true);     // Presence flag true
                serializeVector(v);
            } 
            else {
                writeBoolean(false);    // Presence flag false
            } 
        } 
        catch (IOException ioe) {
            throw new LEAPSerializationException("Error serializing Vector");
        } 
    } 

    /**
     * Writes an array of String to this data output stream.
     * @param sa the array of String to be written.
     * @exception LEAPSerializationException if an error occurs during
     * serialization
     */
    public void writeStringArray(String[] sa) throws LEAPSerializationException {
        try {
            if (sa != null) {
                writeBoolean(true);     // Presence flag true
                serializeStringArray(sa);
            } 
            else {
                writeBoolean(false);    // Presence flag false
            } 
        } 
        catch (IOException ioe) {
            throw new LEAPSerializationException("Error serializing String[]");
        } 
    } 

    /**
     * Writes a Long object to this data output stream.
     * @param l The Long to be written.
     * @exception LEAPSerializationException if an error occurs during
     * serialization
     */
    /*
     * public void writeLongObject(Long l) throws LEAPSerializationException {
     * try {
     * if (l != null) {
     * writeBoolean(true);     // Presence flag true
     * writeLong(l.longValue());
     * }
     * else {
     * writeBoolean(false);    // Presence flag false
     * }
     * }
     * catch (IOException ioe) {
     * throw new LEAPSerializationException("Error serializing Long");
     * }
     * }
     */

    // PRIVATE METHODS
    // All the following methods are used to actually serialize instances of
    // Java classes to this output stream. They are only used internally when
    // the context ensures that the Java object to be serialized is not null!

    /**
     */
    private void serializeDate(Date d) throws IOException {
        writeLong(d.getTime());
    } 

    /**
     */
    private void serializeStringBuffer(StringBuffer sb) throws IOException {
        writeUTF(sb.toString());
    } 

    /**
     */
    private void serializeVector(Vector v) throws IOException, LEAPSerializationException {
        writeInt(v.size());

        for (int i = 0; i < v.size(); i++) {
            writeObject(v.elementAt(i));
        } 
    } 

    /*
      private void serializeProperties(Properties props) 
      throws IOException, LEAPSerializationException {
      int size = props.size();
      
      writeInt(size);

      Enumeration e = props.keys();
      
      while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      
      writeString(key);
      writeString(props.getProperty(key));
      } 
      } 
    */

    /**
     */
    private void serializeStringArray(String[] sa) throws IOException, LEAPSerializationException {
        writeInt(sa.length);

        for (int i = 0; i < sa.length; i++) {
            writeString(sa[i]);
        } 
    } 

    /**
     */
    private void serializeProxy(RemoteContainerProxy proxy) 
        throws IOException, LEAPSerializationException {
        writeAID(proxy.getReceiver());
        writeObject(proxy.getRemoteContainer());
    } 

    /**
     */
    private void serializeACL(ACLMessage msg) throws IOException, LEAPSerializationException {
        writeInt(msg.getPerformative());
        writeAID(msg.getSender());

        Iterator it = msg.getAllReceiver();

        while (it.hasNext()) {
            writeBoolean(true);
            serializeAID((AID) it.next());
        } 

        writeBoolean(false);

        it = msg.getAllReplyTo();

        while (it.hasNext()) {
            writeBoolean(true);
            serializeAID((AID) it.next());
        } 

        writeBoolean(false);
        writeString(msg.getLanguage());
        writeString(msg.getOntology());

        // Content
        if (msg.hasByteSequenceContent()) {
            writeInt(1);
            writeObject(msg.getByteSequenceContent());
        } 
        else {
            writeInt(0);
            writeString(msg.getContent());
        } 

        writeString(msg.getEncoding());
        writeString(msg.getProtocol());
        writeString(msg.getConversationId());
        writeDate(msg.getReplyByDate());
        writeString(msg.getInReplyTo());
        writeString(msg.getReplyWith());

        // User def properties can't be null!
        serializeProperties(msg.getAllUserDefinedParameters());
        writeObject(msg.getEnvelope());
    } 

    /**
     * Package scoped as it is called by the EnvelopSerializer
     */
    void serializeAID(AID id) throws IOException, LEAPSerializationException {
        writeString(id.getName());

        Iterator it = id.getAllAddresses();

        while (it.hasNext()) {
            writeBoolean(true);
            writeUTF((String) it.next());
        } 

        writeBoolean(false);

        it = id.getAllResolvers();

        while (it.hasNext()) {
            writeBoolean(true);
            serializeAID((AID) it.next());
        } 

        writeBoolean(false);

        // User def slots can't be null!
        serializeProperties(id.getAllUserDefinedSlot());
    } 

    /**
     * Package scoped as it is called by the CommandDispatcher
     */
    void serializeCommand(Command cmd) throws LEAPSerializationException {
        try {
            writeInt(cmd.getCode());    // the code of the command has to
            // be at index 0 and it has to be
            // a 4 byte integer.
            writeInt(cmd.getObjectID());

            int paramCnt = cmd.getParamCnt();

            writeInt(paramCnt);

            for (int i = 0; i < paramCnt; ++i) {
                writeObject(cmd.getParamAt(i));
            } 
        } 
        catch (IOException ioe) {
            throw new LEAPSerializationException("Error serializing Command");
        } 
    } 

    /**
     */
    public void serializeContainerID(ContainerID cid) throws LEAPSerializationException {
        try {
            writeUTF(cid.getName());
            writeUTF(cid.getAddress());
        } 
        catch (IOException ioe) {
            throw new LEAPSerializationException("Error serializing ContainerID");
        } 
    } 

    /**
     */
    public void serializeMTPDescriptor(MTPDescriptor dsc) throws LEAPSerializationException {
        try {
            writeUTF(dsc.getName());
            writeStringArray(dsc.getAddresses());
            writeStringArray(dsc.getSupportedProtocols());
        } 
        catch (IOException ioe) {
            throw new LEAPSerializationException("Error serializing MTPDescriptor");
        } 
    } 

    /**
     */
    private void serializeMainStub(MainContainerStub mcs) 
        throws LEAPSerializationException {
        try {
            writeInt(mcs.remoteID);
            int cnt = mcs.remoteTAs.size();
            writeInt(cnt);
 
            for (int i = 0; i < cnt; ++i) {
                writeObject(mcs.remoteTAs.get(i));
            } 
        } 
        catch (IOException ioe) {
            throw new LEAPSerializationException("IO error serializing object "+mcs);
        } 
    }

    /**
     */
    private void serializeContainerStub(AgentContainerStub acs) 
        throws LEAPSerializationException {
        try {
            writeInt(acs.remoteID);
            int cnt = acs.remoteTAs.size();
            writeInt(cnt);
            
            for (int i = 0; i < cnt; ++i) {
                writeObject(acs.remoteTAs.get(i));
            } 
        } 
        catch (IOException ioe) {
            throw new LEAPSerializationException("IO error serializing Stub "+acs);
        } 
    }

    /**
     */
    private void serializeArrayList(ArrayList l)
        throws LEAPSerializationException {
        try {
            int       size = l.size();
            writeInt(size);

            for (int i = 0; i < size; i++) {
                writeObject(l.get(i));
            } 
        } 
        catch (IOException ioe) {
            throw new LEAPSerializationException("I/O error serializing ArrayList "+l);
        } 
    }

    /**
     */  
    private void serializeByteArray(byte[] ba)
        throws LEAPSerializationException {
        try {
            writeInt(ba.length);
            write(ba, 0, ba.length);
        } 
        catch (IOException ioe) {
            throw new LEAPSerializationException("IO error serializing byte[] "+ba);
        } 
    }

    /**
     */  
    private void serializeEnvelope(Envelope e)
        throws LEAPSerializationException {
        try {
            
            // to
            Iterator it = e.getAllTo();
            
            while (it.hasNext()) {
                writeBoolean(true);
                serializeAID((AID) it.next());
            } 
            
            writeBoolean(false);
            writeAID(e.getFrom());
            writeString(e.getComments());
            writeString(e.getAclRepresentation());
            writeLong(e.getPayloadLength().longValue());
            writeString(e.getPayloadEncoding());
            writeDate(e.getDate());
            
            // encrypted
            it = e.getAllEncrypted();
            
            while (it.hasNext()) {
                writeBoolean(true);
                writeUTF((String) it.next());
            } 
            
            writeBoolean(false);

            // intended receivers
            it = e.getAllIntendedReceiver();
            
            while (it.hasNext()) {
                writeBoolean(true);
                serializeAID((AID) it.next());
            } 
            
            writeBoolean(false);
            writeObject(e.getReceived());
            
            // writeObject(e.getTransportBehaviour());
        } 
        catch (IOException ioe) {
            throw new LEAPSerializationException("IO error serializing Envelope "+e);
        } 
    }
    
    /**
     */  
    private void serializeJICPAddress(JICPAddress addr)
        throws LEAPSerializationException {
        writeString(addr.getProto());
        writeString(addr.getHost());
        writeString(addr.getPort());
        writeString(addr.getFile());
        writeString(addr.getAnchor());
    }

    /**
     */  
    private void serializeProperties(Properties p)
        throws LEAPSerializationException {
        try {     
            int        size = p.size();
            writeInt(size);  

            Enumeration e = p.propertyNames();
            while (e.hasMoreElements()) {
                Object key = e.nextElement();      
                writeObject(key);
                writeObject(p.getProperty((String) key));
            } 
        } 
        catch (IOException ioe) {
            throw new LEAPSerializationException("I/O error serializing Properties "+p);
        }    
    }

    /**
     */  
    private void serializeReceivedObject(ReceivedObject r)
        throws LEAPSerializationException {
        writeString(r.getBy());
        writeString(r.getFrom());
        writeDate(r.getDate());
        writeString(r.getId());
        writeString(r.getVia());
    }

    /**
     */   
    private void serializeDummyCertificate(DummyCertificate dc)
        throws LEAPSerializationException {
        writeObject(dc.getSubject());
        writeDate(dc.getNotBefore());
        writeDate(dc.getNotAfter());
    }

    /**
     */   
    private void serializeDummyPrincipal(DummyPrincipal dp)
        throws LEAPSerializationException {
        writeString(dp.getName());
    }

    /**
     */   
    private void serializeCertificateFolder(CertificateFolder cf)
        throws LEAPSerializationException {
        
        writeObject(cf.getIdentityCertificate());
        // Convert the List of delegation certificates into a vector
    	List l = cf.getDelegationCertificates();
    	Iterator it = l.iterator();
        Vector v = new Vector();
    	while (it.hasNext()) {
            v.addElement(it.next());
    	}
    	writeVector(v);
    } 
    
    
    /**
     */
    private Serializer getSerializer(Object o) 
        throws LEAPSerializationException {
        String fullName = o.getClass().getName();
        int    index = fullName.lastIndexOf('.');
        String name = fullName.substring(index+1);
        String serName = new String("jade.imtp.leap."+name+"Serializer");
      
        // DEBUG
        // System.out.println(serName);
        try {
            Serializer s = (Serializer) Class.forName(serName).newInstance();

            return s;
        } 
        catch (Exception e) {
            throw new LEAPSerializationException("Error creating Serializer for object "+o);
        } 
    } 

}

