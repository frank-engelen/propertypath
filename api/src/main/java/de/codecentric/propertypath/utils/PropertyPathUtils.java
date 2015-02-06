package de.codecentric.propertypath.utils;

import java.util.Collection;

import de.codecentric.propertypath.api.PropertyPath;

public class PropertyPathUtils {

	public static <T> void copy(T source, T destination, PropertyPath<T, ?>... properties) {
		for (PropertyPath<T, ?> property : properties) {
			Object value = property.get(source);
			property.setTypeUnsafe(destination, value);
		}
	}

	public static <T> void copy(T source, T destination, Collection<PropertyPath<T, ?>> properties) {
		for (PropertyPath<T, ?> property : properties) {
			Object value = property.get(source);
			property.setTypeUnsafe(destination, value);
		}
	}
}
