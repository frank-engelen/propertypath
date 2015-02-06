package de.codecentric.propertypath.api;

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
	private Class<?>[] declaredInChain;
	private final Class<?> typeInParent;
	private final int length;
	private transient Method[] methods;
	private transient Method setter;
	private transient boolean initDone;

	public PropertyPath(Class<ORIGIN> originClazz, PropertyPath<ORIGIN, ?> parent, String nameInParent, Class<?> typeInParent, Class<?> declaredIn) {
		this.originClazz = originClazz;
		this.parent = parent;
		this.nameInParent = nameInParent;
		this.typeInParent = typeInParent;

		if (declaredIn == null) {
			declaredInChain = new Class<?>[] {};
		} else if (parent == null) {
			declaredInChain = new Class<?>[] { declaredIn };
		} else {
			declaredInChain = new Class<?>[parent.declaredInChain.length + 1];
			System.arraycopy(parent.declaredInChain, 0, declaredInChain, 0, parent.declaredInChain.length);
			declaredInChain[parent.declaredInChain.length] = declaredIn;
		}
		if (parent == null && nameInParent == null) {
			this.fullPath = "";
			length = 0;
		} else if (parent == null && nameInParent != null) {
			this.fullPath = nameInParent;
			length = 1;
		} else if (parent != null && parent.fullPath.isEmpty() && nameInParent != null) {
			this.fullPath = nameInParent;
			length = 1;
		} else if (parent != null) {
			this.fullPath = parent.fullPath + "." + nameInParent;
			length = parent.length() + 1;
		} else {
			this.fullPath = nameInParent;
			length = 1;
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

	public boolean isWritable() {
		init();
		return setter != null;
	}

	public boolean isReadable() {
		init();
		return methods != null && methods.length > 0;
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

	public <NEW_TARGET, M extends PropertyPath<ORIGIN, NEW_TARGET>> M downcast(Class<M> downclazz) {

		try {
			Constructor<M> constructor = downclazz.getConstructor(Class.class, PropertyPath.class, String.class, Class.class, Class.class);
			M instance = constructor.newInstance(originClazz, parent, nameInParent, null, null);
			instance.declaredInChain = this.declaredInChain;
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
			setter = findSetter(parentTargetClass, typeInParent, setterName);
			if (setter == null) {
				Class<?> typeInParentPrimitive = getPrimitiveType4ObjectType(typeInParent);
				if (typeInParentPrimitive != null) {
					setter = findSetter(parentTargetClass, typeInParentPrimitive, setterName);
				}
			}
			targetClassAndNameInParent2Setter.put(key, setter);
		}
		return setter;
	}

	private static Class<?> getPrimitiveType4ObjectType(Class<?> type) {
		if (type.equals(Byte.class)) {
			return byte.class;
		} else if (type.equals(Short.class)) {
			return short.class;
		} else if (type.equals(Integer.class)) {
			return int.class;
		} else if (type.equals(Long.class)) {
			return long.class;
		} else if (type.equals(Double.class)) {
			return double.class;
		} else if (type.equals(Float.class)) {
			return float.class;
		} else if (type.equals(Character.class)) {
			return char.class;
		} else if (type.equals(Boolean.class)) {
			return boolean.class;
		}
		return null;
	}

	private static Method findSetter(Class<?> parentTargetClass, Class<?> typeInParent, final String setterName) {
		Method setter;
		try {
			setter = parentTargetClass.getMethod(setterName, new Class[] { typeInParent });
		} catch (SecurityException e) {
			setter = null;
		} catch (NoSuchMethodException e) {
			setter = null;
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

		if (instance == null || !isReadable()) {
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

	public TARGET getNullsafe(ORIGIN instance) {
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
					if (current == null) {
						return null;
					}
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
		setTypeUnsafe(instance, value);
	}

	public void setTypeUnsafe(ORIGIN instance, Object value) {
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
		if (obj == this) {
			return true;
		}

		if (obj instanceof PropertyPath<?, ?>) {
			PropertyPath<?, ?> prop = (PropertyPath<?, ?>) obj;
			return fullPath.equals(prop.fullPath) && arraysEquals(declaredInChain, prop.declaredInChain, -1);
		}
		return false;
	}

	private boolean arraysEquals(Object[] arr1, Object[] arr2, int lengthMaxIn) {
		if (arr1 == arr2) {
			return true;
		}

		if (arr1 == null || arr2 == null) {
			return false;
		}

		int lengthMax = lengthMaxIn;
		if (lengthMaxIn == -1) {
			if (arr1.length != arr2.length) {
				return false;
			}

			lengthMax = arr1.length;
		} else if (lengthMaxIn > arr1.length || lengthMaxIn > arr2.length) {
			return false;
		}

		for (int i = 0; i < lengthMax; i++) {
			final Object o1 = arr1[i];
			final Object o2 = arr2[i];

			if (o1 != o2 && (o1 == null || o2 == null || !o1.equals(o2))) {
				return false;
			}
		}

		return true;
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

	public boolean startsWith(PropertyPath<?, ?> subPath) {

		if (!this.fullPath.startsWith(subPath.fullPath)) {
			return false;
		}

		return arraysEquals(this.declaredInChain, subPath.declaredInChain, subPath.declaredInChain.length);
	}

	public int length() {
		return length;
	}
}
