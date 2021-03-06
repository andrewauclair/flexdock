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

import java.util.WeakHashMap;

/**
 * This class manages associations between classes and object instances. It
 * allows for mappings between a class and its subclasses and another associated
 * class, or an associated instance of a class.
 * <p>
 * This class is useful for "handler" type logic in which a handler class must
 * be mapped to the classes it is designed to handle. Consider the class
 * hierarchy of {@code Foo}, {@code Bar}, and {@code Baz}, where {@code Bar}
 * extends {@code Foo} and {@code Baz} extends {@code Bar}.
 * <p>
 * <pre>
 * Foo.class
 *   |-Bar.class
 *       |-Baz.class
 * </pre>
 * <p>
 * Each of these classes is ultimately a type of {@code Foo}. Some operation is
 * performed on instances of {@code Foo} and a set of handler classes are used
 * to handle different types of {@code Foo}. Adding a mapping between
 * {@code Foo.class} and {@code Handler1.class} will create an association
 * between {@code Foo} and all <i>strict, non-specific</i> subclasses of
 * {@code Foo} and {@code Handler1.class}.
 * <p>
 * This means that given any instance of {@code Foo}, calling
 * {@code getClassMapping(Object obj)} will return {@code Handler1.class} as the
 * class responsible for handling the {@code Foo} instance. This includes
 * {@code Bar} and {@code Baz}. All types of {@code Foo} now have a implicit
 * association with {@code Handler1.class}
 * <p>
 * However, if this method is subsequently called with arguments of
 * {@code Baz.class} and {@code Handler2.class}, then a <i>specific</i>
 * subclass mapping has been introduced for {@code Baz}. Associations apply to
 * the given class and <i>non-specific</i> subclasses. Thus, the
 * {@code Handler1.class} association remains for {@code Foo} and {@code Bar},
 * but no longer for {@code Baz}. Calling {@code getClassMapping(Object obj)}
 * with an instance of {@code Baz} will now return {@code Handler2.class}.
 * <p>
 * <pre>
 *  Foo.class ---------------&gt; (maps to Handler1.class)
 *    |-Bar.class -----------&gt; (maps to Handler1.class)
 *        |-Baz.class -------&gt; (maps to Handler2.class)
 * </pre>
 * <p>
 * Polymorphic identity within the class association uses <i>strict</i>
 * subclasses. This means that the {@code Handler1.class} mapping for
 * {@code Foo}, {@code Bar}, and all non-specific subclasses will hold true.
 * However, if {@code Foo} happens to implement the interface {@code Qwerty},
 * the class mapping relationship will not hold true for all implementations of
 * {@code Qwerty}. Only subclasses of {@code Foo}.
 * <p>
 * <pre>
 *  Foo.class (implements Qwerty) ----------------&gt; (maps to Handler1.class)
 *    |-Bar.class (implements Qwerty) ------------&gt; (maps to Handler1.class)
 *        |-Baz.class (implements Qwerty) --------&gt; (maps to Handler2.class)
 *  Asdf.class (implements Qwerty) ---------------&gt; (maps to nothing)
 * </pre>
 *
 * @author Christopher Butler
 */
public class ClassMapping {
    private final WeakHashMap<Class<?>, Class<?>> classes = new WeakHashMap<>(4);

    private final WeakHashMap<Class<?>, Object> instances = new WeakHashMap<>(4);

	private Class<?> defaultClass;

    private Object defaultInstance;

    /**
     * Creates a new {@code ClassMapping} instance with the specified default
     * values. All calls to {@code getClassMapping(Class key)} for this
     * {@code ClassMapping} in which a specific mapping cannot be found will
     * return the specified {@code defaultClass}. All calls to
     * {@code getClassInstance(Class key)} in which a specific mapping cannot be
     * found will return the specified {@code defaultInstance}.
     *
     * @param defaultClass    the default class used by this {@code ClassMapping}
     * @param defaultInstance the default object instance used by this {@code ClassMapping}
     */
	public ClassMapping(Class<?> defaultClass, Object defaultInstance) {
        this.defaultClass = defaultClass;
        this.defaultInstance = defaultInstance;
    }

    /**
     * Adds a mapping between the key {@code Class} and the specified
     * {@code value}. If either {@code key} or {@code value} are {@code null},
     * then this method returns with no action taken. This method creates an
     * association between the specified {@code key} {@code Class} and all
     * strict, non-specific subclasses and the specified {@code value}
     * {@code Class}. The {@code value} class may later be retrieved by calling
     * getClassMapping(Class key) using the specified {@code key} class or any
     * subclass thereof for which a specific class mapping does not already
     * exist.
     *
     * @param key   the {@code Class} to be mapped to the specified {@code value}.
     * @param value the {@code Class} to be associated with the specified <b>key</b>
     * @see #addClassMapping(Class, Class, Object)
     * @see #getClassMapping(Class)
     * @see #removeClassMapping(Class)
     */
	public void addClassMapping(Class<?> key, Class<?> value) {
        addClassMapping(key, value, null);
    }

    /**
     * Adds a mapping between the key {@code Class} and both the specified
     * {@code value} and specified object instance.. If either {@code key} or
     * {@code value} are {@code null}, then this method returns with no action
     * taken. This method creates an association between the specified
     * {@code key} {@code Class} and all strict, non-specific subclasses and the
     * specified {@code value} {@code Class}. The {@code value} class may later
     * be retrieved by calling {@code getClassMapping(Class key)} using the
     * specified {@code key} class or any subclass thereof for which a specific
     * class mapping does not already exist.
     * <p>
     * This method also creates an optional mapping between the {@code key} and
     * a particular object instance, defined by the {@code instance} parameter.
     * If {@code instance} is non-{@code null}, then a mapping is defined
     * between {@code key} and all strict, non-specific subclasses and the
     * object instance itself. The {@code instance} object may later be
     * retrieved by calling {@code getClassInstance(Class key)} using the
     * specified {@code key} class or any subclass thereof for which a specific
     * instance mapping does not already exist. If {@code instance} is
     * {@code null}, then no instance mapping is created.
     *
     * @param key      the {@code Class} to be mapped to the specified {@code value}.
     * @param value    the {@code Class} to be associated with the specified <b>key</b>
     * @param instance the object instance to be associated with the specified <b>key</b>
     * @see #getClassMapping(Class)
     * @see #getClassInstance(Class)
     * @see #removeClassMapping(Class)
     */
    public void addClassMapping(Class<?> key, Class<?> value, Object instance) {
        if (key == null || value == null) {
            return;
        }

        synchronized (classes) {
            classes.put(key, value);
        }

        if (instance != null) {
            synchronized (instances) {
                instances.put(key, instance);
            }
        }
    }

    /**
     * Removes any existing class mappings for the {@code Class} type of the
     * specified {@code Object}. This method calls {@code getClass()} on the
     * specified {@code Object} and dispatches to
     * {@code removeClassMapping(Class key)}. If {@code obj} is {@code null},
     * then this method returns {@code null}.
     * <p>
     * Removing the mapping for the specified {@code Class} will also remove it
     * for all non-specific subclasses. This means that subclasses of the
     * specified {@code Class} will require specific mappings if the it is
     * desired for the existing mapping behavior for these classes to remain the
     * same.
     * <p>
     * If any instance mappings exist for the specified {@code Class}, they are
     * also removed. This means non-specific subclass instance mappings will
     * also be removed.
     *
     * @param key the {@code Class} whose internal mapping will be removed
     * @return the {@code Class} whose mapping has been removed
     * @see #addClassMapping(Class, Class)
     * @see #addClassMapping(Class, Class, Object)
     * @see #getClassInstance(Class)
     */
	public Class<?> removeClassMapping(Class<?> key) {
        if (key == null) {
            return null;
        }

		Class<?> clazz;
        synchronized (classes) {
            clazz = classes.remove(key);
        }

        synchronized (instances) {
            instances.remove(key);
        }

        return clazz;
    }

    /**
     * Returns the {@code Class} associated with the specified {@code Class}.
     * If {@code key} is {@code null}, this method will return the value
     * retrieved from {@code getDefaultMapping()}. If no mapping has been
     * defined for the specified {@code Class}, then it's superclass is
     * checked, and then that classes' superclass, and so on until
     * {@code java.lang.Object} is reached. If a mapping is found anywhere
     * within the superclass hierarchy, then the mapped {@code Class} is
     * returned. Otherwise, the value returned by {@code getDefaultMapping()} is
     * returned.
     *
     * @param key the {@code Class} whose internal mapping will be returned
     * @return the {@code Class} that is mapped internally to the specified
     * {@code key}
     * @see #addClassMapping(Class, Class)
     * @see #removeClassMapping(Class)
     */
	public Class<?> getClassMapping(Class<?> key) {
        if (key == null) {
            return defaultClass;
        }

		Class<?> value = null;

        synchronized (classes) {
			for (Class<?> clazz = key; clazz != null && value == null; clazz = clazz.getSuperclass()) {
                value = classes.get(clazz);
            }
        }

        return value == null ? defaultClass : value;
    }

    /**
     * Returns the {@code Object} instance associated with the specified
     * {@code Class}. If {@code key} is {@code null}, this method will return
     * the value retrieved from {@code getDefaultInstance()}. If no mapping has
     * been defined for the specified {@code Class}, then it's superclass is
     * checked, and then that classes' superclass, and so on until
     * {@code java.lang.Object} is reached. If an instance mapping is found
     * anywhere within the superclass hierarchy, then the mapped {@code Object}
     * is returned. Otherwise, the value returned by
     * {@code getDefaultInstance()} is returned.
     *
     * @param key the {@code Class} whose internal mapping will be returned
     * @return the {@code Object} instance that is mapped internally to the
     * specified {@code key}
     * @see #addClassMapping(Class, Class, Object)
     * @see #removeClassMapping(Class)
     */
	public Object getClassInstance(Class<?> key) {
        if (key == null) {
            return defaultInstance;
        }

        Object value = null;

        synchronized (instances) {
			for (Class<?> clazz = key; clazz != null && value == null; clazz = clazz
                    .getSuperclass()) {
                value = instances.get(clazz);
            }
        }

        return value == null ? defaultInstance : value;
    }

}
