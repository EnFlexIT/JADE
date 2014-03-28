/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.android;

import jade.wrapper.AgentContainer;
import jade.wrapper.ContainerController;

/**
 * @author Federico Bergenti - Universita' di Parma
 */
public class AgentContainerHandler {
	private ContainerController containerController;

	private RuntimeService runtimeService;

	AgentContainerHandler(RuntimeService runtimeService, ContainerController containerController) {
		this.runtimeService = runtimeService;
		this.containerController = containerController;
	}

	/**
	 * @deprecated Use getContainerController() instead
	 */
	public AgentContainer getAgentContainer() {
		return (AgentContainer) containerController;
	}

	RuntimeService getRuntimeService() {
		return runtimeService;
	}

	public void kill(RuntimeCallback<Void> callback) {
		runtimeService.killAgentContainer(this, callback);
	}

	public void createNewAgent(String nickname, String className, Object[] args, RuntimeCallback<AgentHandler> callback) {
		runtimeService.createNewAgent(this, nickname, className, args, callback);
	}
}
