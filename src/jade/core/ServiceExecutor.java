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

package jade.core;

//#APIDOC_EXCLUDE_FILE


/**
   This interface is implemented by all the
   service executor classes, i.e. SecurityServiceExecutor, etc...
   Agents get their service executor by the method: Agent.getServiceExecutor(String)

   @see jade.core.Agent

*/
public interface ServiceExecutor {

    public void init( Agent a );

    // Known ServiceExecutor default classes
    public final static String SECURITY = "jade.core.security.SecurityServiceExecutor";
    //public final static String CONTENT  = "jade.content.ContentServiceExecutor";  // it does not exist yet
}

