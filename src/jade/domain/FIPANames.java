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

package jade.domain;

/**
 * This class provides a single access point for the
 *  set of constants
 * already defined by FIPA.
 * The constants have been grouped by category (i.e. ACLCodecs, 
 * Content Languages, MTPs, ...), with one inner class implementing each
 * category.
 * @author Fabio Bellifemine - TILab
 * @version $Date$ $Revision$
 **/

public class FIPANames {
    /**
     * Set of constants that identifies the Codec of ACL Messages and
     * that can be assigned via 
     * <code> ACLMessage.getEnvelope().setAclRepresentation(FIPANames.ACLCodec.BITEFFICIENT); </code>
     **/
    public static class ACLCodec {
	/** Syntactic representation of ACL in string form 
	 * @see <a href=http://www.fipa.org/specs/fipa00070/XC00070f.html>FIPA Spec</a>
	 **/
	public static final String STRING = "fipa.acl.rep.string.std";
	/** Syntactic representation of ACL in XML form
	 * @see <a href=http://www.fipa.org/specs/fipa00071/XC00071b.html>FIPA Spec</a>
	 **/
	public static final String XML = "fipa.acl.rep.xml.std";
	/** Syntactic representation of ACL in XML form
	 * @see <a href=http://www.fipa.org/specs/fipa00069/XC00069e.html>FIPA Spec</a>
	 **/
	public static final String BITEFFICIENT = "fipa.acl.rep.bitefficient.std"; 
    }

    /**
     * Set of constants that identifies the Interaction Protocols and that
     * can be assigned via
     * <code>ACLMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST)
     * </code>
     **/
    public static class InteractionProtocol implements jade.proto.FIPAProtocolNames {
    }

    /**
     * Set of constants that identifies the content languages and that 
     * can be assigned via
     * <code>ACLMessage.setLanguage(FIPANames.ContentLanguage.SL0)
     * </code>
     **/
    public static class ContentLanguage {
	public static final String FIPA_SL0 = "FIPA-SL0";
	public static final String FIPA_SL1 = "FIPA-SL1";
	public static final String FIPA_SL2 = "FIPA-SL2";
	public static final String FIPA_SL  = "FIPA-SL";
    }

    /**
     * Set of constants that identifies the Message Transport Protocols. 
     **/
    public static class MTP {
	/**
	 * IIOP-based MTP
	 * @see <a href=http://www.fipa.org/specs/fipa00075/XC00075e.html>FIPA Spec</a>
	 **/
	public static final String IIOP = "fipa.mts.mtp.iiop.std";
	/**
	 * WAP-based MTP
	 * @see <a href=http://www.fipa.org/specs/fipa00076/XC00076c.html>FIPA Spec</a>
	 **/
	public static final String WAP = "fipa.mts.mtp.wap.std";
	/**
	 * HTTP-based MTP
	 * @see <a href=http://www.fipa.org/specs/fipa00084/XC00084d.html>FIPA Spec</a>
	 **/
	public static final String HTTP = "fipa.mts.mtp.http.std";
    }
}
