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

package jade.security;

import java.util.Date;

import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;


public class BasicCertificateImpl {
	
	BasicPrincipal subject;
	BasicPrincipal issuer;
	
	Date notBefore;
	Date notAfter;

	long serial;
	
  ArrayList permissions = new ArrayList();
  
	byte[] signature;
	
	public BasicCertificateImpl() {
  }

  //methods to (un)marshall

	public void setSubject(BasicPrincipal subject) { this.subject = subject; }
	public BasicPrincipal getSubject() { return subject; }

	public void setIssuer(BasicPrincipal issuer) { this.issuer = issuer; }
	public BasicPrincipal getIssuer() { return issuer; }

	public void setSerial(long serial) { this.serial = serial; }
	public long getSerial() { return serial; }

	public void setNotBefore(Date notBefore) { this.notBefore = notBefore; }
	public Date getNotBefore() { return notBefore; }

	public void setNotAfter(Date notAfter) { this.notAfter = notAfter; }
	public Date getNotAfter() { return notAfter; }
	
  public void addPermission(Object permission) { permissions.add(permission); }
  public Iterator getPermissions() { return permissions.iterator(); }
	
	public String encode() {
		StringBuffer str = new StringBuffer();
		str.append(subject.getName()).append('\n');
		str.append(issuer.getName()).append('\n');
		str.append(notBefore).append('\n');
		str.append(notAfter).append('\n');
		str.append(serial).append('\n');
		//str.append(getSignatureAsString()).append('\n');
		return str.toString();
	}
	
	public void decode(String encoded) {
	}
	
}
