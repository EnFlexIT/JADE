/*
 * (c) Copyright Hewlett-Packard Company 2001
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE and no warranty
 * that the program does not infringe the Intellectual Property rights of
 * a third party.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 */

package jade.wrapper;

/**
 * Defines those methods which are permitted to control a JADE agent.
 * @author David Bell, Dick Cowan: Hewlett-Packard
 */
public interface AgentController
{
    /**
     * Get the platforms name of the agent.
     * This name would be what the platform would use to uniquely reference this agent.
     * @return The agents name.
     */
    public String getName() throws AgentControllerException;
    
    /**
     * Start the agent.
     */
    public void start() throws AgentControllerException;

    /**
     * Suspend the agent.
     */
    public void suspend() throws AgentControllerException;
    
    /**
     * Activate a suspended agent.
     */
    public void activate() throws AgentControllerException;

    /**
     * Kill the agent.
     */
    public void kill() throws AgentControllerException;

    /**
     * Get the agent's state.
     * @return The agent's state.
     */
    public State getState() throws AgentControllerException;

}
