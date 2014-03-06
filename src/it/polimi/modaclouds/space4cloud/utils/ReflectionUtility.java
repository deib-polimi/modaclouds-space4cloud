package it.polimi.modaclouds.space4cloud.utils;

import java.lang.reflect.Field;

public final class ReflectionUtility {

	
	/**
	 * reinforcement for a static class 
	 */
	private ReflectionUtility()
	{	
		throw new AssertionError();
	}
	
	/**
	 * this method uses reflection to access and to modify the value of a certain object
	 *
	 * @param object the object
	 * @param fieldName the field name
	 * @param fieldValue the field value
	 * @return true, if successful
	 */
	public static boolean set(Object object, String fieldName, Object fieldValue) {
	    Class<?> clazz = object.getClass();
	    while (clazz != null) {
	        try {
	            Field field = clazz.getDeclaredField(fieldName);
	            field.setAccessible(true);
	            field.set(object, fieldValue);
	            return true;
	        } catch (NoSuchFieldException e) {
	            clazz = clazz.getSuperclass();
	        } catch (Exception e) {
	            return false;
	        }
	    }
	    return false;
	}
	
	@SuppressWarnings("unchecked")
	public static <E> E get(Object object, String fieldName) {
	    Class<?> clazz = object.getClass();
	    while (clazz != null) {
	        try {
	            Field field = clazz.getDeclaredField(fieldName);
	            field.setAccessible(true);
	            return (E) field.get(object);
	        } catch (NoSuchFieldException e) {
	            clazz = clazz.getSuperclass();
	        } catch (Exception e) {
	            return null;
	        }
	    }
	    return null;
	}
	

}
