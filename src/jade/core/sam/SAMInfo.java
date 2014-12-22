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

package jade.core.sam;

//#DOTNET_EXCLUDE_FILE

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * An instance of this class is passed to all configured <code>SAMInfoHandler<code>-s
 * at each polling time and groups together all information collected by the SAM Service at that 
 * polling time.
 */
public class SAMInfo implements Serializable {
	private static final long serialVersionUID = 84762938792387L;
	
	public static final String AGGREGATION_SEPARATOR = "#";

	private Map<String, AverageMeasure> entityMeasures;
	private Map<String, Long> counterValues;
	
	SAMInfo() {
		this(new HashMap<String, AverageMeasure>(), new HashMap<String, Long>());
	}
	
	SAMInfo(Map<String, AverageMeasure> entityMeasures, Map<String, Long> counterValues) {
		this.entityMeasures = entityMeasures;
		this.counterValues = counterValues;
	}
	
	/**
	 * Provides the measures of all monitored entities in form of a Map.
	 * @return A Map mapping monitored entity names to their measures
	 */
	public Map<String, AverageMeasure> getEntityMeasures() {
		return entityMeasures;
	}
	
	/**
	 * Provides the differential values of all monitored counters in form of a Map.
	 * @return A Map mapping monitored counter names to their differential values
	 */
	public Map<String, Long> getCounterValues() {
		return counterValues;
	}
	
	void update(SAMInfo info) {
		// Update entity measures
		Map<String, AverageMeasure> mm = info.getEntityMeasures();
		for (String entityName : mm.keySet()) {
			AverageMeasure newM = mm.get(entityName);
			// If this is a new entity --> add it. Otherwise update the measure we have internally
			AverageMeasure m = entityMeasures.get(entityName);
			if (m == null) {
				entityMeasures.put(entityName, newM);
			}
			else {
				m.update(newM);
			}
		}
		
		// Update counter values
		Map<String, Long> vv = info.getCounterValues();
		for (String counterName : vv.keySet()) {
			long newV = vv.get(counterName);
			// If this is a new counter --> add it. Otherwise sum to the value we have internally
			Long v = counterValues.get(counterName);
			if (v == null) {
				counterValues.put(counterName, newV);
			}
			else {
				counterValues.put(counterName, v.longValue()+newV);
			}
		}
	}
	
	/**
	 * If there are entities/counters of the form a#b, a#c... produce an aggregated entity a.
	 * Since a itself may have the form a1#a2, iterate until there are no more aggregations   
	 */
	void computeAggregatedValues() {
		// Aggregate measures
		Map<String, AverageMeasure> aggregatedMeasures = oneShotComputeAggregatedMeasures(entityMeasures);
		while (aggregatedMeasures.size() > 0) {
			addAllMeasures(aggregatedMeasures, entityMeasures);
			aggregatedMeasures = oneShotComputeAggregatedMeasures(aggregatedMeasures);
		}
		
		// Aggregate counters
		Map<String, Long> aggregatedCounters = oneShotComputeAggregatedCounters(counterValues);
		while (aggregatedCounters.size() > 0) {
			addAllCounters(aggregatedCounters, counterValues);
			aggregatedCounters = oneShotComputeAggregatedCounters(aggregatedCounters);
		}
	}
	
	private static Map<String, AverageMeasure> oneShotComputeAggregatedMeasures(Map<String, AverageMeasure> measures) {
		Map<String, AverageMeasure> aggregatedMeasures = new HashMap<String, AverageMeasure>();
		for (String entityName : measures.keySet()) {
			AverageMeasure am = measures.get(entityName);
			int k = entityName.lastIndexOf(AGGREGATION_SEPARATOR);
			if (k > 0) {
				// This is an "aggregated measure component" (aaa#bbb) --> accumulate component contribution
				String aggregatedEntityName = entityName.substring(0, k);
				AverageMeasure agM = aggregatedMeasures.get(aggregatedEntityName);
				if (agM == null) {
					agM = new AverageMeasure();
					aggregatedMeasures.put(aggregatedEntityName, agM);
				}
				agM.update(am);
			}
		}
		return aggregatedMeasures;
	}
	
	private static void addAllMeasures(Map<String, AverageMeasure> mm1, Map<String, AverageMeasure> mm2) {
		for (String entityName : mm1.keySet()) {
			AverageMeasure m = mm1.get(entityName);
			AverageMeasure old = mm2.get(entityName);
			if (old != null) {
				old.update(m);
			}
			else {
				mm2.put(entityName, m);
			}
		}
	}
	
	private static Map<String, Long> oneShotComputeAggregatedCounters(Map<String, Long> counters) {
		Map<String, Long> aggregatedCounters = new HashMap<String, Long>();
		for (String counterName : counters.keySet()) {
			Long c = counters.get(counterName);
			int k = counterName.lastIndexOf(AGGREGATION_SEPARATOR);
			if (k > 0) {
				// This is an "aggregated counter component" (aaa#bbb) --> accumulate component contribution
				String aggregatedCounterName = counterName.substring(0, k);
				Long agC = aggregatedCounters.get(aggregatedCounterName);
				if (agC == null) {
					agC = new Long(0);
				}
				aggregatedCounters.put(aggregatedCounterName, agC + c);
			}
		}
		return aggregatedCounters;
	}
	
	private static void addAllCounters(Map<String, Long> cc1, Map<String, Long> cc2) {
		for (String counterName : cc1.keySet()) {
			Long c = cc1.get(counterName);
			Long old = cc2.get(counterName);
			if (old == null) {
				old = new Long(0);
			}
			cc2.put(counterName, old + c);
		}
	}
}
