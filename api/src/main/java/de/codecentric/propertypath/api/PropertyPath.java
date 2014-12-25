package de.codecentric.propertypath.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PropertyPath<ORIGIN, TARGET> implements Serializable {

    private static final long serialVersionUID = 1L;
    private final PropertyPath<ORIGIN, ?> parent;
    private final String nameInParent;
    private final String fullPath;
    private final Class<?> originClazz;
    private final Class<?> typeInParent;
    private transient Method[] methods;
    private transient Method setter;
    private transient boolean initDone;

    public PropertyPath(Class<ORIGIN> originClazz, PropertyPath<ORIGIN, ?> parent, String nameInParent, Class<?> typeInParent) {
	this.originClazz = originClazz;
	this.parent = parent;
	this.nameInParent = nameInParent;
	this.typeInParent = typeInParent;

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
    }

    private final void init() {
	if (parent != null) {
	    parent.init();
	}
	if (!initDone) {
	    if (parent == null) {
		methods = null;
		setter = null;
	    } else {
		methods = getGetters(originClazz, fullPath, parent, nameInParent);
		setter = getSetter(parent.typeInParent, nameInParent, typeInParent);
	    }
	    initDone = true;
	}
    }

    public PropertyPath<ORIGIN, ?> getParent() {
	return parent;
    }

    public String getFullPath() {
	return fullPath;
    }

    public String getNameInParent() {
	return nameInParent;
    }

    public <NEW_TARGET, M extends PropertyPath<ORIGIN, NEW_TARGET>> M _downcast(Class<M> downclazz) {

	try {
	    Constructor<M> constructor = downclazz.getConstructor(Class.class, PropertyPath.class, String.class, Class.class);
	    M instance = constructor.newInstance(originClazz, parent, nameInParent, null);
	    return instance;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
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

    private static final Method[] EMPTY_METHOD_ARRAY = new Method[0];
    private static final Map<String, Method[]> originClassAndFullPath2GetterArray = new HashMap<String, Method[]>();

    private static Method[] getGetters(Class<?> originClass, String fullPath, PropertyPath<?, ?> parent, String nameInParent) {
	final String key = originClass.getName() + "#" + fullPath;

	Method[] getters = originClassAndFullPath2GetterArray.get(key);
	if (getters == null) {
	    final Method getter = getGetter(parent.typeInParent, nameInParent);
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

    @Override
    public String toString() {
	return originClazz.getName() + "::" + fullPath + "/" + (setter != null ? setter.getName() : "n/a") + " (" + typeInParent.getName() + ")";
    }

    public TARGET get(ORIGIN instance) {
	init();

	if (instance == null) {
	    return null;
	}

	Object current = instance;
	if (methods != null) {
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
	}

	@SuppressWarnings("unchecked")
	final TARGET result = (TARGET) current;

	return result;
    }

    public void set(ORIGIN instance, TARGET value) {
	init();

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
	if (obj instanceof PropertyPath<?, ?>) {
	    PropertyPath<?, ?> prop = (PropertyPath<?, ?>) obj;
	    return originClazz.equals(prop.originClazz) && fullPath.equals(prop.fullPath);
	}
	return false;
    }

    @Override
    public int hashCode() {
	return originClazz.hashCode() * 37 + fullPath.hashCode();
    }

    public boolean sameOriginClass(PropertyPath<?, ?> other) {
	return other != null && this.originClazz.equals(other.originClazz);
    }

    public boolean sameTargetClass(PropertyPath<?, ?> other) {
	return other != null && this.typeInParent.equals(other.typeInParent);
    }

    protected static <T extends Serializable> T deepCopy(T source) {
	if (source == null) {
	    return null;
	}

	try {
	    final ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);
	    final ObjectOutputStream oos = new ObjectOutputStream(bos);
	    oos.writeObject(source);
	    oos.flush();
	    oos.close();
	    bos.flush();
	    bos.close();

	    final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
	    final ObjectInputStream ois = new ObjectInputStream(bis);

	    @SuppressWarnings("unchecked")
	    T destination = (T) ois.readObject();

	    return destination;
	} catch (IOException e) {
	    throw new RuntimeException(e);
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException(e);
	}
    }

    public boolean startsWith(PropertyPath<ORIGIN, ?> subPath) {
	return this.fullPath.startsWith(subPath.fullPath) && sameOriginClass(subPath);
    }
}
