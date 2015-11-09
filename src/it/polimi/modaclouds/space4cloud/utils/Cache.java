/*******************************************************************************
 * Copyright 2014 Giovanni Paolo Gibilisco
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * 
 */
package it.polimi.modaclouds.space4cloud.utils;

import java.util.LinkedHashMap;

/**
 * The Class Cache.
 * 
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @author MODAClouds
 */
public class Cache<K, V> extends LinkedHashMap<K, V> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5603292271106543306L;
	/** The max size. */
	private int maxSize = 1;

	/**
	 * Instantiates a new cache.
	 * 
	 * @param maxSize
	 *            the max size
	 */
	public Cache(int maxSize) {
		this.maxSize = maxSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
	 */
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		return size() > this.maxSize;
	}

}
