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

package examples.ontology;

/**
* @author Angelo Difino - CSELT S.p.A
* @version $Date$ $Revision$
*/
public class EngageAction {
	
	private Company	_company;							//Company engager
	private Person	_person;							//Person engaged
	private String	_actor;
	
	//These methods are used by the JADE-framework
	public void set_0(Company company) {
		_company=company;
	}
	public Company get_0() {
		return _company;
	}

	public void set_1(Person person) {
		_person=person;
	}
	public Person get_1() {
		return _person;
	}
	
	public void setActor(String actor) {
		_actor=actor;
	}
	public String getActor() {
		return _actor;
	}
	
	//These methods are application-dependent and are used by RequesterAgent
	//and ExecutorAgent
	public void setPerson(Person personToEngage) {
		_person=personToEngage;
	}
	public void setCompany(Company companyEngager) {
		_company=companyEngager;
	}
	public Person getPerson() {			
		return _person;
	}
	public Company getCompany() {		
		return _company;
	}
	public void execute(){
		System.out.println("Engaging to company "+_company.getName()+" "+" the person "+ _person.getName());
	}
}