/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.domain.FIPAAgentManagement;

/**
 * Dummy class for J2ME
 * @version $Date$ $Revision$
 */
public class FIPAAgentManagementOntology {

    /**
     * A symbolic constant, containing the name of this ontology.
     */
    public static final String NAME = "FIPA-Agent-Management";

    // Concepts
    public static final String DFAGENTDESCRIPTION = "df-agent-description";
    public static final String SERVICEDESCRIPTION = "service-description";
    public static final String SEARCHCONSTRAINTS = "search-constraints";
    public static final String AMSAGENTDESCRIPTION = "ams-agent-description";
    public static final String APDESCRIPTION = "ap-description";
    public static final String APTRANSPORTDESCRIPTION = 
        "ap-transport-description";
    public static final String MTPDESCRIPTION = "mtp-description";
    public static final String PROPERTY = "property";

    // Actions
    public static final String REGISTER = "register";
    public static final String DEREGISTER = "deregister";
    public static final String MODIFY = "modify";
    public static final String SEARCH = "search";
    public static final String GETDESCRIPTION = "get-description";
    public static final String QUIT = "quit";

    // Not-understood Exception Propositions
    public static final String UNSUPPORTEDACT = "unsupported-act";
    public static final String UNEXPECTEDACT = "unexpected-act";
    public static final String UNSUPPORTEDVALUE = "unsupported-value";
    public static final String UNRECOGNISEDVALUE = "unrecognised-value";

    // Refusal Exception Propositions
    public static final String UNAUTHORISED = "unauthorised";
    public static final String UNSUPPORTEDFUNCTION = "unsupported-function";
    public static final String MISSINGPARAMETER = "missing-parameter";
    public static final String UNEXPECTEDPARAMETER = "unexpected-parameter";
    public static final String UNRECOGNISEDPARAMETERVALUE = 
        "unrecognised-parameter-value";

    // Failure Exception Propositions
    public static final String ALREADYREGISTERED = "already-registered";
    public static final String NOTREGISTERED = "not-registered";
    public static final String INTERNALERROR = "internal-error";
}

