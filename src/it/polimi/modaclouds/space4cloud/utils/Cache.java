/**
 * 
 */
package it.polimi.modaclouds.space4cloud.utils;

import java.util.LinkedHashMap;

/**
 * The Class Cache.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author MODAClouds
 */
public class Cache<K,V> extends LinkedHashMap<K, V> {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5603292271106543306L;
	/** The max size. */
	private int maxSize = 1;

	/**
	 * Instantiates a new cache.
	 *
	 * @param maxSize the max size
	 */
	public Cache(int maxSize) {
	this.maxSize = maxSize;
	}

	/* (non-Javadoc)
	 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
	 */
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
	return size() > this.maxSize;
	}

	}