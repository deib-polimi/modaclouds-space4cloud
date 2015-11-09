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
package it.polimi.modaclouds.space4cloud.utils;

import java.lang.reflect.Field;

public final class ReflectionUtility {

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

	/**
	 * this method uses reflection to access and to modify the value of a
	 * certain object
	 * 
	 * @param object
	 *            the object
	 * @param fieldName
	 *            the field name
	 * @param fieldValue
	 *            the field value
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

	/**
	 * reinforcement for a static class
	 */
	private ReflectionUtility() {
		throw new AssertionError();
	}

}
