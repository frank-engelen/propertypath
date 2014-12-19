package de.codecentric.propertypath.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PropertyPath {

    private final PropertyPath parent;
    private final String nameInParent;
    private final String fullPath;
    private final Class<?> originClazz;
    private final Method[] methods;
    private final Method setter;
    private final Class<?> targetClass;

    public PropertyPath(Class<?> originClazz, PropertyPath parent, String nameInParent, Class<?> typeInParent) {
	this.originClazz = originClazz;
	this.parent = parent;
	this.nameInParent = nameInParent;

	if (parent == null && nameInParent == null) {
	    this.fullPath = "";
	} else if (parent == null && nameInParent != null) {
	    this.fullPath = nameInParent;
	} else if (parent != null && parent.fullPath.isEmpty() && nameInParent != null) {
	    this.fullPath = nameInParent;
	} else if (parent != null) {
	    this.fullPath = parent.fullPath + "." + nameInParent;
	} else {
	    this.fullPath = nameInParent;
	}

	if (parent == null) {
	    methods = null;
	    setter = null;
	    targetClass = originClazz;
	} else {
	    methods = getGetters(originClazz, fullPath, parent, nameInParent);
	    setter = getSetter(parent.targetClass, nameInParent, typeInParent);
	    targetClass = typeInParent;
	}
    }

    public PropertyPath getParent() {
	return parent;
    }

    public String getFullPath() {
	return fullPath;
    }

    public String getNameInParent() {
	return nameInParent;
    }

    private static final java.util.Map<String, Method> targetClassAndNameInParent2Setter = new java.util.HashMap<String, Method>();

    private static Method getSetter(Class<?> parentTargetClass, String nameInParent, Class<?> typeInParent) {
	final String key = parentTargetClass.getName() + "#" + nameInParent;
	Method setter = targetClassAndNameInParent2Setter.get(key);
	if (setter == null && !targetClassAndNameInParent2Setter.containsKey(key)) {
	    final String setterName = "set" + getCaseRightName(nameInParent);
	    try {
		setter = parentTargetClass.getMethod(setterName, new Class[] { typeInParent });
	    } catch (SecurityException e) {
		setter = null;
	    } catch (NoSuchMethodException e) {
		setter = null;
	    }
	    targetClassAndNameInParent2Setter.put(key, setter);
	}
	return setter;
    }

    private static Method[] EMPTY_METHOD_ARRAY = new Method[0];
    private static final Map<String, Method[]> originClassAndFullPath2GetterArray = new HashMap<String, Method[]>();

    private static Method[] getGetters(Class<?> originClass, String fullPath, PropertyPath parent, String nameInParent) {
	final String key = originClass.getName() + "#" + fullPath;

	Method[] getters = originClassAndFullPath2GetterArray.get(key);
	if (getters == null) {
	    final Method getter = getGetter(parent.targetClass, nameInParent);
	    if (getter == null) {
		getters = EMPTY_METHOD_ARRAY;
	    } else {
		if (parent.methods == null) {
		    getters = new Method[] { getter };
		} else {
		    getters = new Method[parent.methods.length + 1];
		    System.arraycopy(parent.methods, 0, getters, 0, parent.methods.length);
		    getters[parent.methods.length] = getter;
		}
	    }
	    originClassAndFullPath2GetterArray.put(key, getters);
	}

	return getters;
    }

    private static Method getGetter(Class<?> targetClass, String nameInParent) {
	final String[] tries = { "get", "is", "are" };
	for (final String stry : tries) {
	    final String getterName = stry + getCaseRightName(nameInParent);
	    try {
		final Method getter = targetClass.getMethod(getterName, new Class[] {});
		return getter;
	    } catch (SecurityException e) {
		// ignore;
	    } catch (NoSuchMethodException e) {
		// ignore
	    }
	}
	return null;
    }

    private static String getCaseRightName(String nameInParent) {
	return nameInParent.substring(0, 1).toUpperCase() + nameInParent.substring(1);
    }

    //
    // private final Method getSetter() {
    // final Method last = methods[methods.length - 1];
    // final String nameOfLast = last.getName();
    //
    // final String key = this.targetClass.getName() + "#" + nameOfLast;
    // Method setter = classAndGetter2Setter.get(key);
    // if (setter != null) {
    // return setter;
    // }
    //
    // String nameOfSetter = getNameOfSetter(nameOfLast);
    //
    // if (nameOfSetter != null) {
    // try {
    // setter = targetClass.getMethod(nameOfSetter, last.getReturnType());
    // } catch (SecurityException e) {
    // // implies no setter
    // } catch (NoSuchMethodException e) {
    // // implies no setter
    // }
    // }
    //
    // classAndGetter2Setter.put(key, setter);
    //
    // return setter;
    // }
    //
    // protected static String getNameOfSetter(final String nameOfLast) {
    // String nameOfSetter = null;
    // if (nameOfLast.startsWith("get") || nameOfLast.startsWith("are")) {
    // nameOfSetter = "set" + nameOfLast.substring(3);
    // } else if (nameOfLast.startsWith("is")) {
    // nameOfSetter = "set" + nameOfLast.substring(2);
    // } else {
    // nameOfSetter = nameOfLast;
    // }
    // return nameOfSetter;
    // }
    //
    // private static class Info {
    // private final Class<?> clazz;
    // private List<Method> methods = new java.util.ArrayList<Method>();
    //
    // public Info(Class<?> clazz) {
    // this.clazz = clazz;
    // }
    //
    // public List<Method> getMethods() {
    // return methods;
    // }
    //
    // public Class<?> getClazz() {
    // return clazz;
    // }
    //
    // }

    @Override
    public String toString() {
	return originClazz.getName() + "::" + fullPath + "/" + (setter != null ? setter.getName() : "n/a") + " (" + targetClass.getName() + ")";
    }

    public Object get(Object instance) {
	if (instance == null) {
	    return null;
	}
	Object current = instance;
	for (int i = 0; i < methods.length && current != null; i++) {
	    Method m = methods[i];
	    try {
		current = m.invoke(current);
	    } catch (IllegalArgumentException e) {
		throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
		throw new RuntimeException(e);
	    } catch (InvocationTargetException e) {
		throw new RuntimeException(e);
	    }
	}
	return current;
    }

    public void set(Object instance, Object value) {
	if (instance == null || setter == null) {
	    return;
	}

	Object current = instance;
	for (int i = 0; i < methods.length - 1 && current != null; i++) {
	    Method m = methods[i];
	    try {
		current = m.invoke(current);
	    } catch (IllegalArgumentException e) {
		throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
		throw new RuntimeException(e);
	    } catch (InvocationTargetException e) {
		throw new RuntimeException(e);
	    }
	}

	if (current != null) {
	    try {
		setter.invoke(current, value);
	    } catch (IllegalArgumentException e) {
		throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
		throw new RuntimeException(e);
	    } catch (InvocationTargetException e) {
		throw new RuntimeException(e);
	    }
	}
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof PropertyPath) {
	    PropertyPath prop = (PropertyPath) obj;
	    return originClazz.equals(prop.originClazz) && fullPath.equals(prop.fullPath);
	}
	return false;
    }

    @Override
    public int hashCode() {
	return originClazz.hashCode() * 37 + fullPath.hashCode();
    }

    public boolean sameOriginClass(PropertyPath other) {
	return other != null && this.originClazz.equals(other.originClazz);
    }

    public boolean sameTargetClass(PropertyPath other) {
	return other != null && this.targetClass.equals(other.targetClass);
    }
}
