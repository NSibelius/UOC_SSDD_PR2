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
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import edu.uoc.dpcs.lsim.logger.LoggerManager.Level;

/**
 * @author Joan-Manuel Marques, Daniel Lázaro Iglesias
 * December 2012
 *
 */
public class TimestampMatrix implements Serializable{
	
	private static final long serialVersionUID = 3331148113387926667L;
	ConcurrentHashMap<String, TimestampVector> timestampMatrix = new ConcurrentHashMap<String, TimestampVector>();
	
	public TimestampMatrix(List<String> participants){
		// create and empty TimestampMatrix
		for (Iterator<String> it = participants.iterator(); it.hasNext(); ){
			timestampMatrix.put(it.next(), new TimestampVector(participants));
		}
	}
	
	/**
	 * @param node
	 * @return the timestamp vector of node in this timestamp matrix
	 */
	public synchronized TimestampVector getTimestampVector(String node){
		//MIO Ok
		return timestampMatrix.get(node);
		// return generated automatically. Remove it when implementing your solution 
		//return null;
	}
	
	/**
	 * Merges two timestamp matrix taking the elementwise maximum
	 * @param tsMatrix
	 */
	public synchronized void updateMax(TimestampMatrix tsMatrix){
		//MIO - depende de updateMax() del TimestampVector
		for(Enumeration<String> en=timestampMatrix.keys(); en.hasMoreElements();)
		{
			String name=en.nextElement();
			timestampMatrix.get(name).updateMax(tsMatrix.getTimestampVector(name));
		}
	}
	
	/**
	 * substitutes current timestamp vector of node for tsVector
	 * @param node
	 * @param tsVector
	 */
	public synchronized void update(String node, TimestampVector tsVector){
		//MIO - OK
		timestampMatrix.replace(node, timestampMatrix.get(node), tsVector);
	}
	
	/**
	 * 
	 * @return a timestamp vector containing, for each node, 
	 * the timestamp known by all participants
	 */
	public synchronized TimestampVector minTimestampVector(){
		//MIO depende de mergeMin() de TimestampVector
		TimestampVector min = null;
		
		Enumeration <TimestampVector> en = timestampMatrix.elements();
		min = en.nextElement();
				
		while(en.hasMoreElements())
		{
			TimestampVector tsv=en.nextElement();
			min.mergeMin(tsv);
		}
		
		return min;
		 
		
		//GITHUB
//		TimestampVector result = null;     
//        for (TimestampVector matrixValues : this.timestampMatrix.values()) {
//            if (result == null)
//                result = matrixValues.clone();
//            else
//                result.mergeMin(matrixValues);
//        }       
//        return result;
	}
	
	/**
	 * clone
	 */
	public synchronized TimestampMatrix clone(){
		List<String> participants = Arrays.asList();
		TimestampMatrix copy = new TimestampMatrix(participants);
		copy.timestampMatrix.putAll(timestampMatrix);
		return copy;
		// return generated automatically. Remove it when implementing your solution 
		//return null;
	}
	
	/**
	 * equals
	 */
	@Override
	public synchronized boolean equals(Object obj) {
		
		if(obj != null && obj instanceof TimestampMatrix)
		{
			TimestampMatrix other = (TimestampMatrix) obj;
			return timestampMatrix.equals(other.timestampMatrix);
		}
		// return generated automatically. Remove it when implementing your solution 
		return false;
	}

	
	/**
	 * toString
	 */
	@Override
	public synchronized String toString() {
		String all="";
		if(timestampMatrix==null){
			return all;
		}
		for(Enumeration<String> en=timestampMatrix.keys(); en.hasMoreElements();){
			String name=en.nextElement();
			if(timestampMatrix.get(name)!=null)
				all+=name+":   "+timestampMatrix.get(name)+"\n";
		}
		return all;
	}
}
