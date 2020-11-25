/*
* Copyright (c) Joan-Manuel Marques 2013. All rights reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
* This file is part of the practical assignment of Distributed Systems course.
*
* This code is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This code is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this code.  If not, see <http://www.gnu.org/licenses/>.
*/

package recipes_service.tsae.data_structures;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import recipes_service.data.Operation;
//LSim logging system imports sgeag@2017
//import lsim.coordinator.LSimCoordinator;
import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;
import lsim.library.api.LSimLogger;

/**
 * @author Joan-Manuel Marques, Daniel Lázaro Iglesias
 * December 2012
 *
 */
public class Log implements Serializable{
	// Only for the zip file with the correct solution of phase1.Needed for the logging system for the phase1. sgeag_2018p 
//	private transient LSimCoordinator lsim = LSimFactory.getCoordinatorInstance();
	// Needed for the logging system sgeag@2017
//	private transient LSimWorker lsim = LSimFactory.getWorkerInstance();

	private static final long serialVersionUID = -4864990265268259700L;
	/**
	 * This class implements a log, that stores the operations
	 * received  by a client.
	 * They are stored in a ConcurrentHashMap (a hash table),
	 * that stores a list of operations for each member of 
	 * the group.
	 */
	private ConcurrentHashMap<String, List<Operation>> log= new ConcurrentHashMap<String, List<Operation>>();  

	public Log(List<String> participants){
		// create an empty log
		for (Iterator<String> it = participants.iterator(); it.hasNext(); ){
			log.put(it.next(), new Vector<Operation>());
		}
	}

	/**
	 * inserts an operation into the log. Operations are 
	 * inserted in order. If the last operation for 
	 * the user is not the previous operation than the one 
	 * being inserted, the insertion will fail.
	 * 
	 * @param op
	 * @return true if op is inserted, false otherwise.
	 */
	public synchronized boolean add(Operation op){
		// ....
		
		String hostId = op.getTimestamp().getHostid();
		List<Operation> messages = log.get(hostId);
		
		if(messages.isEmpty() || messages.get(messages.size()-1).getTimestamp().compare(op.getTimestamp())<0)
		{
				
				messages.add(op);
				log.put(hostId,messages);
				
				return true;
		}
		
		// return generated automatically. Remove it when implementing your solution 
		return false;
	}
	
	/**
	 * removes an operation from the log.  
	 * @param op
	 * @return true if op is removed, false otherwise.
	 */
	public boolean remove(Operation op)
	{
		return false;
	}
	/**
	 * Checks the received summary (sum) and determines the operations
	 * contained in the log that have not been seen by
	 * the proprietary of the summary.
	 * Returns them in an ordered list.
	 * @param sum
	 * @return list of operations
	 */
	public synchronized List<Operation> listNewer(TimestampVector sum){
		List<Operation> newerList = new Vector<Operation>();
		
		for(Enumeration<String> en=log.keys(); en.hasMoreElements(); )
		{
			String hostId = en.nextElement();
			List<Operation> messages = log.get(hostId); 
			if (!messages.isEmpty())
			{
				Timestamp last = messages.get(messages.size()-1).getTimestamp();
				if(last.compare(sum.getLast(hostId))>0)
				{
					if(sum.getLast(hostId).isNullTimestamp())
					{
						newerList.addAll(messages);
					}else
					{
					int from = searchIndexOfOperation(sum.getLast(hostId))+1;
					newerList.addAll(messages.subList(from, messages.size()));
					}
				}
			}	
		}
		return newerList;
	}
	
	
	/**
	 * Removes from the log the operations that have
	 * been acknowledged by all the members
	 * of the group, according to the provided
	 * ackSummary. 
	 * @param ack: ackSummary.
	 */
	public synchronized void purgeLog(TimestampMatrix ack){
		
		List<Operation> messages;
		TimestampVector minAck;
	
		minAck = ack.minTimestampVector();
		
		for(Enumeration<String> en = log.keys(); en.hasMoreElements(); )
		{
				String hostId = en.nextElement();
				messages = log.get(hostId);
	
				boolean found = false;
				int i = 0;
				Timestamp last = minAck.getLast(hostId);
				
				while(i < messages.size() && !found)
				{
					found = last.equals(messages.get(i).getTimestamp());
					if (!found) messages.remove(messages.get(i));
					i++;
				}	
		}
		
	}

	/**
	 * equals
	 */
	@Override
	public  synchronized boolean equals(Object obj) {
		//MIO 17/10/2020
		if(obj != null && obj instanceof Log)
		{
			Log other = (Log) obj;
			return log.equals(other.log);
		}
		// return generated automatically. Remove it when implementing your solution 
		return false;
	}

	/**
	 * toString
	 */
	@Override
	public synchronized String toString() {
		String name="";
		for(Enumeration<List<Operation>> en=log.elements();
		en.hasMoreElements(); ){
		List<Operation> sublog=en.nextElement();
		for(ListIterator<Operation> en2=sublog.listIterator(); en2.hasNext();){
			name+=en2.next().toString()+"\n";
		}
	}
		
		return name;
	}
	
	//returns the index of an Operation in the principal's list of operations 
	private synchronized int searchIndexOfOperation(Timestamp timestamp)
	{
		String hostId;
		List<Operation> messages;
		Operation op;
		boolean found;
		int index;
		
		hostId = timestamp.getHostid();
		messages = log.get(hostId);
		Iterator<Operation> it = messages.iterator();
		op = it.next();
		found = false;
		
		while(it.hasNext() && !found)
		{
			found = op.getTimestamp().equals(timestamp);
			if(!found) op = it.next();
		}
		
		index = messages.indexOf(op);
		return index;
	}
}
