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

package jade.domain.FIPAAgentManagement;

/** 
* This class models an envelope.
* @see jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology
* @author Fabio Bellifemine - CSELT S.p.A.
* @version $Date$ $Revision$
*/

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import java.util.Date;
import jade.util.leap.Properties;

import jade.core.AID;

public class Envelope implements jade.util.leap.Serializable {

	/**
  @serial
  */
  private ArrayList to = new ArrayList();
  /**
  @serial
  */
  private AID from;
  /**
  @serial
  */
  private String comments;
  /**
  @serial
  */
  private String aclRepresentation;
  /**
  @serial
  */
  private Long payloadLength;
  /**
  @serial
  */
  private String payloadEncoding;
  /**
  @serial
  */
  private Date date;
  /**
  @serial
  */
  private ArrayList encrypted = new ArrayList();
  /**
  @serial
  */
  private ArrayList intendedReceiver = new ArrayList();
  /**
  @serial
  */
  private Properties transportBehaviour;

  /**
  serial
  */
  private ArrayList stamps = new ArrayList();

    /**
     * Constructor. Initializes the payloadLength to -1.
     **/
    public Envelope () {
	payloadLength = new Long(-1);
    }
  public void addTo(AID id) {
    to.add(id);
  }

  public boolean removeTo(AID id) {
    return to.remove(id);
  }

  public void clearAllTo() {
    to.clear();
  }

  public Iterator getAllTo() {
    return to.iterator();
  }

  public void setFrom(AID id) {
    from = id;
  }

  public AID getFrom() {
    return from;
  }

  public void setComments(String c) {
    comments = c;
  }

  public String getComments() {
    return comments;
  }

  public void setAclRepresentation(String r) {
    aclRepresentation = r;
  }

  public String getAclRepresentation() {
    return aclRepresentation;
  }

  public void setPayloadLength(Long l) {
    payloadLength = l;
  }

  public Long getPayloadLength() {
    return payloadLength;
  }

  public void setPayloadEncoding(String e) {
    payloadEncoding = e;
  }

  public String getPayloadEncoding() {
    return payloadEncoding;
  }

  public void setDate(Date d) {
    date = d;
  }

  public Date getDate() {
    return date;
  }

  public void addEncrypted(String s) {
    encrypted.add(s);
  }

  public boolean removeEncrypted(String s) {
    return encrypted.remove(s);
  }

  public void clearAllEncrypted() {
    encrypted.clear();
  }

  public Iterator getAllEncrypted() {
    return encrypted.iterator();
  }

  public void addIntendedReceiver(AID id) {
    intendedReceiver.add(id);
  }

  public boolean removeIntendedReceiver(AID id) {
    return intendedReceiver.remove(id);
  }

  public void clearAllIntendedReceiver() {
    intendedReceiver.clear();
  }

  public Iterator getAllIntendedReceiver() {
    return intendedReceiver.iterator();
  }

  public void setReceived(ReceivedObject ro) {
    addStamp(ro);
  }

  public ReceivedObject getReceived() {
    if(stamps.isEmpty())
      return null;
    else
      return (ReceivedObject)stamps.get(stamps.size() - 1);
  }

  /**
     Add a <code>received-object</code> stamp to this message
     envelope. This method is used by the ACC to add a new stamp to
     the envelope at every routing hop.
     @param ro The <code>received-object</code> to add.
  */
  public void addStamp(ReceivedObject ro) {
    stamps.add(ro);
  }

  /**
     Access the list of all the stamps. The
     <code>received-object</code> stamps are sorted according to the
     routing path, from the oldest to the newest.
  */
  public ReceivedObject[] getStamps() {
      ReceivedObject[] ret = new ReceivedObject[stamps.size()];
      int counter = 0;

      for(Iterator it = stamps.iterator(); it.hasNext(); )
      ret[counter++] = (ReceivedObject)it.next();

    return ret;
  }

  // FIXME: Handle Properties

    public String toString() {
	String s = new String("(Envelope ");
	Iterator i = getAllTo();
	if (i.hasNext()) {
	    s = s + " :to (sequence ";
	    for (Iterator ii=i; ii.hasNext(); ) 
		s = s+" "+ii.next().toString();
	    s = s + ") ";
	}
	if (getFrom() != null)
	    s = s + " :from " + getFrom().toString();
	if (getComments() != null) 
	    s = s + " :comments " + getComments(); 
	if (getAclRepresentation() != null) 
	    s = s + " :acl-representation " + getAclRepresentation(); 
	if (getPayloadLength() != null) 
	    s = s + " :payload-length " + getPayloadLength().toString(); 
	if (getPayloadEncoding() != null) 
	    s = s + " :payload-encoding " + getPayloadEncoding();
	if (getDate() != null)
	    s = s + " :date " + getDate().toString();
	i = getAllEncrypted();
	if (i.hasNext()) {
	    s = s + " :encrypted ";
	    for (Iterator ii=i; ii.hasNext(); )
		s = s + " "+ii.next().toString();
	}
	i = getAllIntendedReceiver();
	if (i.hasNext()) {
	    s = s + " :intended-receiver (sequence ";
	    for (Iterator ii=i; ii.hasNext(); ) 
		s = s+" "+ ii.next().toString();
	    s = s + ") ";
	}
	ReceivedObject[] ro = getStamps();
	if (ro.length > 0 ) {
	    s = s + " :received-object (sequence ";
	    for (int j=0; j<ro.length; j++) 
		s = s + " "+ ro[j].toString(); 
	    s = s + ") ";
	}
	return s+")";
    }

    public Object clone(){
	Envelope env = new Envelope();
	env.to = (ArrayList)to.clone();
	env.encrypted = (ArrayList)encrypted.clone();
	env.intendedReceiver= (ArrayList)intendedReceiver.clone();
	env.stamps = (ArrayList)stamps.clone();
	env.from = from;
	env.comments = comments;
	env.aclRepresentation = aclRepresentation;
	env.payloadLength = payloadLength;
	env.payloadEncoding = payloadEncoding;
	env.date = date;
	env.transportBehaviour = transportBehaviour;
	return env;
    }
}
