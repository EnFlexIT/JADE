/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2002 TILAB S.p.A.

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


/**
	The <code>SDSIName</code> interface represents
	the SDSI name including a public key used in
        asymmetric cryptographic algorithms.

	It is also used as unique identifier for JADE principals
	in secure operations

	@author Giosue Vitaglione - Telecom Italia LAB

	@version $Date$ $Revision$
*/
public interface SDSIName
//#ALL_EXCLUDE_BEGIN
		extends java.security.PublicKey
//#ALL_EXCLUDE_END
{
	/**
		Returns the standard algorithm name for this key.
		@return the name of the algorithm associated with this key.
		@see java.security.PublicKey#getAlgorithm()

	*/
	public String getAlgorithm();


	/**
		Returns the key in its primary encoding format, or null if this key does not support encoding.
		@return The subject.
		@see java.security.PublicKey#getEncoded()
	*/
	public byte[] getEncoded();


	/**
		Returns the name of the primary encoding format of this key, or null if this key does not support encoding.
		@return the primary encoding format of the key.
		@see java.security.PublicKey#getFormat()
	*/
	public String getFormat();



	/**
		Returns the sequence of the names local to the Public Key
        in a SDSI fashion. Last item is always ".".

        Example:  "."           => Simply the Public Key, no local names here involved.
        Example:  "bob", "."    => The principal defined as bob' by the owner of the given public key
        Example:  "bob", "alice", "."  => The principal defined as 'alice' by the principal defined by 'bob' by the owner of the given public key

		@return the primary encoding format of the key.
		@see java.security.PublicKey#getFormat()
	*/
	public String[] getLocalNames();

        /**
         *
         * @return the right most local name
         */
         public String getLastLocalName();




}