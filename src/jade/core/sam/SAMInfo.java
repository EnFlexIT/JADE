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
	
	public static final String DEFAULT_AGGREGATION_SEPARATOR = "#";
	public static final char DEFAULT_AGGREGATION_SEPARATOR_CHAR = '#';
	public static final String SUM_AGGREGATION_SEPARATOR = "+";
	public static final char SUM_AGGREGATION_SEPARATOR_CHAR = '+';
	public static final String AVG_AGGREGATION_SEPARATOR = "@";
	public static final char AVG_AGGREGATION_SEPARATOR_CHAR = '@';
	
	public static final int AVG_AGGREGATION = 0;
	public static final int SUM_AGGREGATION = 1;

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
			AggregationInfo ai = getAggregationInfo(entityName, AVG_AGGREGATION);
			if (ai != null) {
				// This is a contribution to an "aggregated measure" (aaa#bbb) --> accumulate component contribution
				String aggregatedEntityName = ai.aggregatedName;
				AverageMeasure agM = aggregatedMeasures.get(aggregatedEntityName);
				if (agM == null) {
					agM = new AverageMeasure();
					aggregatedMeasures.put(aggregatedEntityName, agM);
				}
				agM.update(am, ai.aggregation);
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
		Map<String, CounterAggregator> aggregatedCounters = new HashMap<String, CounterAggregator>();
		for (String counterName : counters.keySet()) {
			Long c = counters.get(counterName);
			AggregationInfo ai = getAggregationInfo(counterName, SUM_AGGREGATION);
			if (ai != null) {
				// This is a contribution to an "aggregated counter" (aaa#bbb) --> accumulate component contribution
				String aggregatedCounterName = ai.aggregatedName;
				CounterAggregator agC = aggregatedCounters.get(aggregatedCounterName);
				if (agC == null) {
					agC = new CounterAggregator();
					aggregatedCounters.put(aggregatedCounterName, agC);
				}
				agC.update(c, ai.aggregation);
			}
		}
		
		Map<String, Long> result = new HashMap<String, Long>(aggregatedCounters.size());
		for (Map.Entry<String, CounterAggregator> entry : aggregatedCounters.entrySet()) {
			result.put(entry.getKey(), entry.getValue().getAggregatedValue());
		}
		return result;
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
	
	
	public static final AggregationInfo getAggregationInfo(String name, int defaultAggregation) {
		for (int i = name.length() - 1; i >= 0; --i) {
			AggregationInfo ai = null;
			char c = name.charAt(i);
			if (c == DEFAULT_AGGREGATION_SEPARATOR_CHAR) {
				ai = new AggregationInfo(defaultAggregation);
			}
			else if (c == SUM_AGGREGATION_SEPARATOR_CHAR) {
				ai = new AggregationInfo(SUM_AGGREGATION);
			}
			else if (c == AVG_AGGREGATION_SEPARATOR_CHAR) {
				ai = new AggregationInfo(AVG_AGGREGATION);
			}
			
			if (ai != null) {
				ai.aggregatedName = name.substring(0, i);
				return ai;
			}
		}
		
		return null;
	}
	
	
	/**
	 * Inner class AggregationInfo
	 */
	public static class AggregationInfo {
		private int aggregation;
		private String aggregatedName;
		
		AggregationInfo(int aggregation) {
			this.aggregation = aggregation;
		}
	} // END of inner class AggregationInfo
	
	
	/**
	 * Inner class CounterAggregator
	 */
	private static class CounterAggregator {
		private double aggregatedVal = 0;
		private int contributionsCnt = 0;
		
		public void update(long contribution, int aggregation) {
			if (aggregation == AVG_AGGREGATION) {
				aggregatedVal = (aggregatedVal * contributionsCnt + contribution) / (contributionsCnt+1);
				contributionsCnt++;
			}
			else {
				// Default aggregation: SUM
				aggregatedVal += contribution;
			}
		}
		
		public long getAggregatedValue() {
			return (long) aggregatedVal;
		}
	} // END of inner class CounterAggregator
	
}
