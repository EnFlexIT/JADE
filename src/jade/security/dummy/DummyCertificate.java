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

package jade.security.dummy;

import jade.security.*;

import jade.util.leap.List;

import java.util.Date;


public class DummyCertificate implements IdentityCertificate, DelegationCertificate {
	
	JADEPrincipal subject = new DummyPrincipal();
	
	public void setSubject(JADEPrincipal subject) { this.subject = subject; }
	public JADEPrincipal getSubject() { return subject; }
	
	public void setNotBefore(Date notBefore) { }
	public Date getNotBefore() { return null; }
	
	public void setNotAfter(Date notAfter) { }
	public Date getNotAfter() { return null; }
	
	public String encode() { return subject.getName(); }
	public void decode(String encoded) { subject = new DummyPrincipal(encoded); }
	
	public void addPermission(Object permission) { }
	public List getPermissions() { return new jade.util.leap.ArrayList(); }
	
}
