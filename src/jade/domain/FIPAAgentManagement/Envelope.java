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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.util.Properties;

import jade.core.AID;

public class Envelope implements java.io.Serializable {

  private List to = new ArrayList();
  private AID from;
  private String comments;
  private String aclRepresentation;
  private String payloadLength;
  private String payloadEncoding;
  private Date date;
  private List encrypted = new ArrayList();
  private List intendedReceiver = new ArrayList();
  private ReceivedObject received;
  private Properties transportBehaviour;

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

  public void setPayloadLength(String l) {
    payloadLength = l;
  }

  public String getPayloadLength() {
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
    received = ro;
  }

  public ReceivedObject getReceived() {
    return received;
  }

  // FIXME: Handle Properties

}
