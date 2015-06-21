package com.globalLibrary.util;

import java.lang.reflect.*;
import java.util.*;

/**
 * 反射工具类
 */
public final class Reflect {
    private final Object obj;
    private final boolean isClass;

    /**
     * 对指定类型反射
     *
     * @param clazz 待反射类型
     * @return 反射工具类
     */
    @SuppressWarnings("UnusedDeclaration")
    public static Reflect on(Class clazz) {
        return new Reflect(clazz);
    }

    /**
     * 反射获取泛型字段对应的具体类型
     *
     * @param field 字段
     * @param index 泛型序号
     * @return 泛型的具体类型
     */

    public static Class getGenericType(Field field, int index) {
        Class result = Object.class;
        Type genType = field.getGenericType();
        if (genType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) genType;
            Type[] types = type.getActualTypeArguments();
            if (index >= 0 && index < types.length && types[index] instanceof Class) {
                result = (Class) types[index];
            }
        }
        return result;
    }

    private Reflect(Class clazz) {
        this.obj = clazz;
        this.isClass = true;
    }

    private Reflect(Object obj) {
        this.obj = obj;
        isClass = false;
    }

    /**
     * 获取所有字段
     *
     * @return 所有字段集合
     */
    public FieldReflect fields() {
        return FieldReflect.on(type());
    }

    private Class<?> type() {
        if (isClass) {
            return (Class<?>) obj;
        }
        return obj.getClass();
    }

    /**
     * 调用方法
     *
     * @param methodName 方法名
     * @return 方法返回结果值的包装，如果方法无返回值，则是 Object 类的包装
     * @throws ReflectException 无指定方法或调用失败
     */
    public Reflect call(String methodName) throws ReflectException {
        return call(methodName, new Object[0]);
    }

    /**
     * 调用方法
     *
     * @param methodName 方法名
     * @param values     方法参数
     * @return 方法返回结果值的包装，如果方法无返回值，则是 Object 类的包装
     * @throws ReflectException 无指定方法或调用失败
     */
    public Reflect call(String methodName, Object... values) throws ReflectException {
        Class<?>[] types = types(values);
        try {
            Method method = exactMethod(methodName, types);
            return on(method, obj, values);
        } catch (NoSuchMethodException e) {
            try {
                return on(similarMethod(methodName, types), obj, values);
            } catch (NoSuchMethodException e1) {
                throw new ReflectException(e);
            }
        }
    }

    private Class[] types(Object... values) {
        if (values == null) {
            return new Class[0];
        }
        Class[] types = new Class[values.length];
        for (int i = 0, count = values.length; i < count; i++) {
            types[i] = values[i] == null ? Object.class : values[i].getClass();
        }
        return types;
    }

    private Method exactMethod(String methodName, Class... types) throws NoSuchMethodException {
        Class<?> _clazz = type();
        Method method = null;
        try {
            method = _clazz.getMethod(methodName, types);
        } catch (NoSuchMethodException e) {
            while (_clazz != null && _clazz != Object.class) {
                try {
                    method = _clazz.getDeclaredMethod(methodName, types);
                    break;
                } catch (NoSuchMethodException e1) {
                    _clazz = _clazz.getSuperclass();
                }
            }
        }
        if (method == null) {
            throw new NoSuchMethodException("No exact method " + methodName + " with params " + Arrays.toString(types) + " could be found on type " + type());
        } else {
            method.setAccessible(true);
            return method;
        }
    }

    private Reflect on(Method method, Object obj, Object[] values) throws ReflectException {
        try {
            accessible(method);
            if (method.getReturnType() == void.class) {
                method.invoke(obj, values);
                return on(Object.class);
            } else {
                return on(method.invoke(obj, values));
            }
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    /**
     * 设置 Field、Method 或 Constructor 为可访问
     *
     * @param obj 待设置对象
     * @param <T>
     * @return 设置后对象
     */
    public static <T extends AccessibleObject> T accessible(T obj) {
        if (obj == null) {
            return null;
        }
        if (!obj.isAccessible()) {
            obj.setAccessible(true);
        }
        return obj;
    }

    /**
     * 获取满足指定条件的方法
     *
     * @param name  方法名
     * @param types 方法的参数类型
     * @return 满足条件的方法
     * @throws NoSuchMethodException
     */
    private Method similarMethod(String name, Class<?>[] types) throws NoSuchMethodException {
        Class _clazz = type();
        for (Method method : _clazz.getMethods()) {
            if (method.getName().equals(name) && match(method.getParameterTypes(), types)) {
                return method;
            }
        }
        while (_clazz != null && _clazz != Object.class) {
            for (Method method : _clazz.getDeclaredMethods()) {
                if (method.getName().equals(name) && match(method.getParameterTypes(), types)) {
                    method.setAccessible(true);
                    return method;
                }
            }
            _clazz = _clazz.getSuperclass();
        }
        throw new NoSuchMethodException("No similar method " + name + " with params " + Arrays.toString(types) + " could be found on type " + type());
    }

    private boolean match(Class<?>[] declaredTypes, Class<?>[] actualTypes) {
        if (declaredTypes.length == actualTypes.length) {
            for (int i = 0, count = declaredTypes.length; i < count; i++) {
                if (!wrapper(declaredTypes[i]).isAssignableFrom(wrapper(actualTypes[i]))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static Class<?> wrapper(Class<?> type) {
        if (type == null) {
            return null;
        } else if (type.isPrimitive()) {
            if (boolean.class == type) {
                return Boolean.class;
            } else if (int.class == type) {
                return Integer.class;
            } else if (long.class == type) {
                return Long.class;
            } else if (short.class == type) {
                return Short.class;
            } else if (byte.class == type) {
                return Byte.class;
            } else if (double.class == type) {
                return Double.class;
            } else if (float.class == type) {
                return Float.class;
            } else if (char.class == type) {
                return Character.class;
            } else if (void.class == type) {
                return Void.class;
            }
        }

        return type;
    }

    /**
     * 创建新的对象
     *
     * @return 新对象的包装对象
     * @throws ReflectException 构造器未找到或调用构造器时失败
     */
    public Reflect create() throws ReflectException {
        return create(new Object[0]);
    }

    /**
     * 创建新的对象
     *
     * @param values 构造函数参数
     * @return 新对象的包装对象
     * @throws ReflectException 构造器未找到或调用构造器时失败
     */
    public Reflect create(Object... values) throws ReflectException {
        Class<?> _clazz = type();
        Class[] types = types(values);
        Constructor<?> constructor = null;
        try {
            constructor = _clazz.getDeclaredConstructor(types);
        } catch (NoSuchMethodException e) {
            for (Constructor<?> constructorTmp : _clazz.getDeclaredConstructors()) {
                if (match(constructorTmp.getParameterTypes(), types)) {
                    break;
                }
            }
        }
        if (constructor == null) {
            throw new ReflectException(_clazz + " can not be create, could not be found constructor with params " + Arrays.toString(values));
        } else {
            try {
                return on(constructor.newInstance(values));
            } catch (Exception e) {
                throw new ReflectException(e);
            }
        }
    }

    /**
     * 对指定对象反射
     *
     * @param obj 待反射对象
     * @return 反射工具类
     */
    public static Reflect on(Object obj) {
        return new Reflect(obj);
    }

    /**
     * 获得字段值的包装对象
     *
     * @param fieldName 字段名
     * @return 字段值的包装对象
     * @throws ReflectException 字段未找到或获取字段值失败
     */
    public Reflect field(String fieldName) throws ReflectException {
        try {
            Field field = findField(fieldName);
            return on(field.get(obj));
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private Field findField(String fieldName) throws ReflectException {
        Field field = null;
        Class _clazz = type();
        try {
            field = _clazz.getField(fieldName);
        } catch (NoSuchFieldException e) {
            while (_clazz != null && _clazz != Object.class) {
                try {
                    field = _clazz.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException e1) {
                    _clazz = _clazz.getSuperclass();
                }
            }
        }
        if (field == null) {
            throw new ReflectException("field " + fieldName + " could not be found in " + type());
        } else {
            field.setAccessible(true);
            return field;
        }
    }

    /**
     * 设置字段值
     *
     * @param fieldName 字段名
     * @param value     字段值
     * @return 当前对象的包装对象
     * @throws ReflectException 字段未找到或字段值设置失败
     */
    public Reflect set(String fieldName, Object value) throws ReflectException {
        try {
            Field field = findField(fieldName);
            accessible(field).set(obj, unwrap(value));
            return this;
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private Object unwrap(Object obj) {
        if (obj instanceof Reflect) {
            return ((Reflect) obj).get();
        }
        return obj;
    }

    /**
     * 获得包装的对象
     *
     * @param <T> 返回值类型
     * @return 被包装的对象
     */
    public <T> T get() throws ReflectException {
        try {
            return (T) obj;
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    public static class FieldReflect {
        private ArrayList<Field> fields = new ArrayList<Field>();

        public static FieldReflect on(Class clazz) {
            return new FieldReflect(clazz);
        }

        private FieldReflect(Class clazz) {
            while (clazz != null && clazz != Object.class) {
                Collections.addAll(fields, clazz.getDeclaredFields());
                clazz = clazz.getSuperclass();
            }
        }

        public FieldReflect withAnnotation(Class clazz) {
            for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext(); ) {
                Field field = iterator.next();
                if (field.getAnnotation(clazz) == null) {
                    iterator.remove();
                }
            }
            return this;
        }

        public FieldReflect withModifier(int modifier) {
            for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext(); ) {
                Field field = iterator.next();
                if ((field.getModifiers() & modifier) == 0) {
                    iterator.remove();
                }
            }
            return this;
        }

        public FieldReflect withName(String fieldName) {
            for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext(); ) {
                Field field = iterator.next();
                if (!field.getName().equals(fieldName)) {
                    iterator.remove();
                }
            }
            return this;
        }

        public FieldReflect withType(Class fieldType) {
            for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext(); ) {
                Field field = iterator.next();
                if (!field.getType().equals(fieldType)) {
                    iterator.remove();
                }
            }
            return this;
        }

        public List<Field> get() {
            return Collections.unmodifiableList(fields);
        }
    }

    public static class ReflectException extends RuntimeException {
        public ReflectException() {
            super();
        }

        public ReflectException(String detailMessage) {
            super(detailMessage);
        }

        public ReflectException(Throwable throwable) {
            super(throwable);
        }

        public ReflectException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }
    }
}
