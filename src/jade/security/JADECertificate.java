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


/**
	The <code>JADECeritificate</code> interface has to be implemented
	by classes storing authenticated informations, as identities or
	delegated permissions.
   
	@author Michele Tomaiuolo - Universita` di Parma
	@version $Date$ $Revision$
*/
public interface JADECertificate {
	
	/**
		Sets the subject of this certificate, i.e. the principal which
		the carried informations refer to.
		@param The subject.
	*/
	public void setSubject(JADEPrincipal subject);

	/**
		Returns the subject of this certificate.
		@return The subject.
	*/
	public JADEPrincipal getSubject();
	
	/**
		Sets the validity period.
		@param The validity period.
	*/
	public void setNotBefore(Date notBefore);

	/**
		Returns the validity period.
		@return The validity period.
	*/
	public Date getNotBefore();
	
	/**
		Sets the validity period.
		@param The validity period.
	*/
	public void setNotAfter(Date notAfter);

	/**
		Returns the validity period.
		@return The validity period.
	*/
	public Date getNotAfter();
	
	/**
		Returns the canonical string representation of this certificate.
		This string can be used to evaluate a digest and to sign the
		certificate itself.
		@return The canonical string representation.
	*/
	public String encode();

	/**
		Builds back a certificate from its string encoding.
		@param encoded The string to restore the certificate from.
	*/
	public void decode(String encoded);
	
}
