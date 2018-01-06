/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.flexdock.util;

import java.lang.reflect.Method;

/**
 * @author Christopher Butler
 */
public class Utilities {
    /**
     * A constant representing the Java version. This constant is {@code true}
     * if the version is 1.5.
     */
    public static final boolean JAVA_1_5 = System.getProperty("java.version").startsWith("1.5");

    /**
     * A String representing the flexdock version. This constant is a string.
     */
    public static final String VERSION = "1.2.4";

    private Utilities() {
    }

    /**
     * Returns a {@code float} value for the specified {@code String}. This
     * method calls {@code Float.parseFloat(String s)} and returns the resulting
     * {@code float} value. If any {@code Exception} is thrown by
     * {@code parseFloat}, this method returns the value supplied by the
     * {@code defaultValue} parameter.
     *
     * @param data         a {@code String} containing the {@code float} representation
     *                     to be parsed
     * @param defaultValue the value to return if an {@code Exception} is encountered on
     *                     the underlying parse mechanism.
     * @return the floating-point value represented by the argument in decimal
     * @see Float#parseFloat(java.lang.String)
     */
    public static float getFloat(String data, float defaultValue) {
        if (data == null) {
            return defaultValue;
        }

        try {
            return Float.parseFloat(data);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Returns an instance of the specified class name. If {@code className} is
     * {@code null}, then this method returns a {@code null} reference.
     * <p>
     * This method will try two different means of obtaining an instance of
     * {@code className}. First, it will attempt to resolve the {@code Class}
     * of {@code className} via {@code Class.forName(String className)}. It
     * will then use reflection to search for a method on the class named
     * {@code "getInstance()"}. If the method is found, then it is invoked and
     * the object instance is returned.
     * <p>
     * If there are any problems encountered while attempting to invoke
     * {@code getInstance()} on the specified class, the {@code Throwable} is
     * caught and this method dispatches to
     * {@code createInstance(String className, boolean failSilent)} with an
     * argument of {@code false} for {@code failSilent}.
     * {@code createInstance(String className, boolean failSilent)} will attempt
     * to invoke {@code newInstance()} on the {@code Class} for the specified
     * class name. If any {@code Throwable} is encountered during this process,
     * the value of {@code false} for {@code failSilent} will cause the stack
     * trace to be printed to the {@code System.err} and a {@code null}
     * reference will be returned.
     *
     * @param className the fully qualified name of the desired class.
     * @return an instance of the specified class
     * @see #getInstance(String, boolean)
     * @see #createInstance(String, boolean)
     * @see Class#forName(java.lang.String)
     * @see Class#getMethod(java.lang.String, java.lang.Class[])
     * @see Method#invoke(java.lang.Object, java.lang.Object[])
     * @see Class#newInstance()
     */
    public static Object getInstance(String className) {
        return getInstance(className, false);
    }

    /**
     * Returns an instance of the specified class name. If {@code className} is
     * {@code null}, then this method returns a {@code null} reference.
     * <p>
     * This method will try two different means of obtaining an instance of
     * {@code className}. First, it will attempt to resolve the {@code Class}
     * of {@code className} via {@code Class.forName(String className)}. It
     * will then use reflection to search for a method on the class named
     * {@code "getInstance()"}. If the method is found, then it is invoked and
     * the object instance is returned.
     * <p>
     * If there are any problems encountered while attempting to invoke
     * {@code getInstance()} on the specified class, the {@code Throwable} is
     * caught and this method dispatches to
     * {@code createInstance(String className, boolean failSilent)}, passing
     * the specified value for {@code failSilent}.
     * {@code createInstance(String className, boolean failSilent)} will attempt
     * to invoke {@code newInstance()} on the {@code Class} for the specified
     * class name. If any {@code Throwable} is encountered during this process,
     * the value of {@code failSilent} is checked to determine whether the stack
     * stack trace should be printed to the {@code System.err}. A {@code null}
     * reference will be returned if any problems are encountered.
     *
     * @param className  the fully qualified name of the desired class.
     * @param failSilent {@code true} if the stack trace should <b>not</b> be printed
     *                   to the {@code System.err} when a {@code Throwable} is caught,
     *                   {@code false} otherwise.
     * @return an instance of the specified class
     * @see #createInstance(String, boolean)
     * @see Class#forName(java.lang.String)
     * @see Class#getMethod(java.lang.String, java.lang.Class[])
     * @see Method#invoke(java.lang.Object, java.lang.Object[])
     * @see Class#newInstance()
     */
    public static Object getInstance(String className, boolean failSilent) {
        if (className == null) {
            return null;
        }

        try {
            Class<?> c = Class.forName(className);
            Method m = c.getMethod("getInstance");
            return m.invoke(null);
        } catch (Throwable e) {
            return createInstance(className, failSilent);
        }
    }

    /**
     * Creates and returns an instance of the specified class name using
     * {@code Class.newInstance()}. If {@code className} is {@code null}, then
     * this method returns a {@code null} reference. This dispatches to
     * {@code createInstance(String className, Class superType, boolean failSilent)}
     * with an argument of {@code null} for {@code superType} and {@code false}
     * for {@code failSilent}.
     * <p>
     * This method will attempt to resolve the {@code Class} of
     * {@code className} via {@code Class.forName(String className)}. No class
     * assignability checkes are performed because this method uses a
     * {@code null} {@code superType}.
     * <p>
     * Once the desired class has been resolved, a new instance of it is created
     * and returned by invoking its {@code newInstance()} method. If there are
     * any problems encountered during this process, the value of {@code false}
     * for {@code failSilent} will ensure the stack stack trace is be printed to
     * the {@code System.err}. A {@code null} reference will be returned if any
     * problems are encountered.
     *
     * @param className the fully qualified name of the desired class.
     * @return an instance of the specified class
     * @see #createInstance(String, Class, boolean)
     * @see Class#forName(java.lang.String)
     * @see Class#newInstance()
     */
    public static Object createInstance(String className) {
        return createInstance(className, null);
    }

    /**
     * Creates and returns an instance of the specified class name using
     * {@code Class.newInstance()}. If {@code className} is {@code null}, then
     * this method returns a {@code null} reference. The {@code failSilent}
     * parameter will determine whether error stack traces should be reported to
     * the {@code System.err} before this method returns {@code null}. This
     * method dispatches to
     * {@code createInstance(String className, Class superType, boolean failSilent)}
     * with an argument of {@code null} for {@code superType}.
     * <p>
     * This method will attempt to resolve the {@code Class} of
     * {@code className} via {@code Class.forName(String className)}. No class
     * assignability checkes are performed because this method uses a
     * {@code null} {@code superType}.
     * <p>
     * Once the desired class has been resolved, a new instance of it is created
     * and returned by invoking its {@code newInstance()} method. If there are
     * any problems encountered during this process, the value of
     * {@code failSilent} is checked to determine whether the stack stack trace
     * should be printed to the {@code System.err}. A {@code null} reference
     * will be returned if any problems are encountered.
     *
     * @param className  the fully qualified name of the desired class.
     * @param failSilent {@code true} if the stack trace should <b>not</b> be printed
     *                   to the {@code System.err} when a {@code Throwable} is caught,
     *                   {@code false} otherwise.
     * @return an instance of the specified class
     * @see #createInstance(String, Class, boolean)
     * @see Class#forName(java.lang.String)
     * @see Class#newInstance()
     */
    private static Object createInstance(String className, boolean failSilent) {
        return createInstance(className, null, failSilent);
    }

    /**
     * Creates and returns an instance of the specified class name using
     * {@code Class.newInstance()}. If {@code className} is {@code null}, then
     * this method returns a {@code null} reference. If {@code superType} is
     * non-{@code null}, then this method will enforce polymorphic identity
     * via {@code Class.isAssignableFrom(Class cls)}. This method dispatches to
     * {@code createInstance(String className, Class superType, boolean failSilent)}
     * with an argument of {@code false} for {@code failSilent}.
     * <p>
     * This method will attempt to resolve the {@code Class} of
     * {@code className} via {@code Class.forName(String className)}. If
     * {@code superType} is non-{@code null}, then class identity is checked
     * by calling {@code superType.isAssignableFrom(c)} to ensure the resolved
     * class is an valid equivalent, descendent, or implementation of the
     * specified {@code className}. If this check fails, then a
     * {@code ClassCastException} is thrown and caught internally and this
     * method returns {@code null}. If {@code superType} is {@code null}, then
     * no assignability checks are performed on the resolved class.
     * <p>
     * Once the desired class has been resolved, a new instance of it is created
     * and returned by invoking its {@code newInstance()} method. If there are
     * any problems encountered during this process, the value of {@code false}
     * for {@code failSilent} will ensure the stack stack trace is be printed to
     * the {@code System.err}. A {@code null} reference will be returned if any
     * problems are encountered.
     *
     * @param className the fully qualified name of the desired class.
     * @param superType optional paramter used as a means of enforcing the inheritance
     *                  hierarchy
     * @return an instance of the specified class
     * @see #createInstance(String, Class, boolean)
     * @see Class#forName(java.lang.String)
     * @see Class#isAssignableFrom(java.lang.Class)
     * @see Class#newInstance()
     */
    public static Object createInstance(String className, Class<org.flexdock.docking.drag.effects.DragPreview> superType) {
        return createInstance(className, superType, false);
    }

    /**
     * Creates and returns an instance of the specified class name using
     * {@code Class.newInstance()}. If {@code className} is {@code null}, then
     * this method returns a {@code null} reference. If {@code superType} is
     * non-{@code null}, then this method will enforce polymorphic identity
     * via {@code Class.isAssignableFrom(Class cls)}. The {@code failSilent}
     * parameter will determine whether error stack traces should be reported to
     * the {@code System.err} before this method returns {@code null}.
     * <p>
     * This method will attempt to resolve the {@code Class} of
     * {@code className} via {@code Class.forName(String className)}. If
     * {@code superType} is non-{@code null}, then class identity is checked
     * by calling {@code superType.isAssignableFrom(c)} to ensure the resolved
     * class is an valid equivalent, descendent, or implementation of the
     * specified {@code className}. If this check fails, then a
     * {@code ClassCastException} is thrown and caught internally and this
     * method returns {@code null}. If {@code superType} is {@code null}, then
     * no assignability checks are performed on the resolved class.
     * <p>
     * Once the desired class has been resolved, a new instance of it is created
     * and returned by invoking its {@code newInstance()} method. If there are
     * any problems encountered during this process, the value of
     * {@code failSilent} is checked to determine whether the stack stack trace
     * should be printed to the {@code System.err}. A {@code null} reference
     * will be returned if any problems are encountered.
     *
     * @param className  the fully qualified name of the desired class.
     * @param superType  optional paramter used as a means of enforcing the inheritance
     *                   hierarchy
     * @param failSilent {@code true} if the stack trace should <b>not</b> be printed
     *                   to the {@code System.err} when a {@code Throwable} is caught,
     *                   {@code false} otherwise.
     * @return an instance of the specified class
     * @see Class#forName(java.lang.String)
     * @see Class#isAssignableFrom(java.lang.Class)
     * @see Class#newInstance()
     */
    public static Object createInstance(String className, Class<?> superType,
                                        boolean failSilent) {
        if (className == null) {
            return null;
        }

        try {
            Class<?> c = Class.forName(className);
            if (superType != null && !superType.isAssignableFrom(c)) {
                throw new ClassCastException("'" + c.getName()
                        + "' is not a type of " + superType + ".");
            }
            return c.newInstance();
        } catch (Throwable e) {
            if (!failSilent) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Checks for equality between the two specified {@code Objects}. If both
     * arguments are the same {@code Object} reference using an {@code ==}
     * relationship, then this method returns {@code true}. Failing that check,
     * if either of the arguments is {@code null}, then the other must not be
     * and this method returns {@code false}. Finally, if both arguments are
     * non-{@code null} with different {@code Object} references, then this
     * method returns the value of {@code obj1.equals(obj2)}.
     * <p>
     * This method is the exact opposite of
     * {@code isChanged(Object oldObj, Object newObj)}.
     *
     * @param obj1 the first {@code Object} to be checked for equality
     * @param obj2 the second {@code Object} to be checked for equality
     * @return {@code true} if the {@code Objects} are equal, {@code false}
     * otherwise.
     * @see #isChanged(Object, Object)
     * @see Object#equals(java.lang.Object)
     */
    public static boolean isEqual(Object obj1, Object obj2) {
        return !isChanged(obj1, obj2);
    }

    /**
     * Checks for inequality between the two specified {@code Objects}. If both
     * arguments are the same {@code Object} reference using an {@code ==}
     * relationship, then this method returns {@code false}. Failing that
     * check, if either of the arguments is {@code null}, then the other must
     * not be and this method returns {@code true}. Finally, if both arguments
     * are non-{@code null} with different {@code Object} references, then this
     * method returns the opposite value of {@code obj1.equals(obj2)}.
     * <p>
     * This method is the exact opposite of
     * {@code isEqual(Object obj1, Object obj2)}.
     *
     * @param oldObj the first {@code Object} to be checked for inequality
     * @param newObj the second {@code Object} to be checked for inequality
     * @return {@code false} if the {@code Objects} are equal, {@code true}
     * otherwise.
     * @see #isEqual(Object, Object)
     * @see Object#equals(java.lang.Object)
     */
    public static boolean isChanged(Object oldObj, Object newObj) {
        return oldObj != newObj && (oldObj == null || newObj == null || !oldObj.equals(newObj));

    }

    /**
     * Returns {@code true} if there is currently a {@code System} property with
     * the specified {@code key} whose value is "true". If the {@code System}
     * property does not exist, or the value is inequal to "true", this method
     * returns {@code false}. This method returns {@code false} if the
     * specified {@code key} parameter is {@code null}.
     *
     * @param key the {@code System} property to test.
     * @return {@code true} if there is currently a {@code System} property with
     * the specified {@code key} whose value is "true".
     * @see System#getProperty(java.lang.String)
     * @see String#equals(java.lang.Object)
     * @deprecated Use {@link Boolean#getBoolean(String)}.
     */
    public static boolean sysTrue(String key) {
        String value = key == null ? null : System.getProperty(key);
        return value != null && "true".equals(value);
    }

}
