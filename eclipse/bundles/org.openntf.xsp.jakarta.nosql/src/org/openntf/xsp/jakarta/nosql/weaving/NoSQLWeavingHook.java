/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.jakarta.nosql.weaving;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.eclipse.jnosql.communication.TypeReferenceReader;
import org.eclipse.jnosql.communication.ValueReader;
import org.eclipse.jnosql.communication.ValueWriter;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import jakarta.data.repository.DataRepository;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.LoaderClassPath;

/**
 * This {@link WeavingHook} implementation listens for attempts to load
 * {@code jakarta.nosql.ValueReaderDecorator} and, when found, replaces the
 * implementation methods with a version that dynamically looks up providers
 * at runtime instead of using a global singleton value.
 *
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class NoSQLWeavingHook implements WeavingHook {

	@Override
	public void weave(final WovenClass c) {
		switch(c.getClassName()) {
		case "org.eclipse.jnosql.mapping.metadata.ClassScanner" -> processClassScanner(c); //$NON-NLS-1$
		case "org.eclipse.jnosql.mapping.metadata.ClassConverter" -> processClassConverter(c); //$NON-NLS-1$
		case "org.eclipse.jnosql.communication.ValueReaderDecorator" -> processValueReaderDecorator(c); //$NON-NLS-1$
		case "org.eclipse.jnosql.communication.ValueWriter" -> processValueWriter(c); //$NON-NLS-1$
		case "org.eclipse.jnosql.communication.ValueWriterDecorator" -> processValueWriterDecorator(c); //$NON-NLS-1$
		case "org.eclipse.jnosql.communication.TypeReferenceReaderDecorator" -> processTypeReferenceReader(c); //$NON-NLS-1$
		case "org.eclipse.jnosql.mapping.metadata.ConstructorBuilder" -> processConstructorBuilder(c); //$NON-NLS-1$
		case "org.eclipse.jnosql.mapping.reflection.DefaultConstructorBuilder" -> processDefaultConstructorBuilder(c); //$NON-NLS-1$
		case "org.eclipse.jnosql.mapping.reflection.DefaultMapFieldMetadata" -> processDefaultMapFieldMetadata(c); //$NON-NLS-1$
		case "org.eclipse.jnosql.mapping.reflection.DefaultCollectionFieldMetadata" -> processDefaultCollectionFieldMetadata(c); //$NON-NLS-1$
		}
	}

	private void processClassScanner(final WovenClass c) {
		CtClass cc = defrost(c, DataRepository.class, org.eclipse.core.runtime.Platform.class, org.osgi.framework.Bundle.class);

		try {
			// Oddly, making a fragment bundle to use ServiceLoader fails when deployed in an NSF Update Site, so do it a horrible way
			String body = """
			{
				return org.eclipse.core.runtime.Platform.getBundle("org.openntf.xsp.jakarta.nosql").loadClass("org.openntf.xsp.jakarta.nosql.scanner.ComponentModuleClassScanner").getConstructor(new Class[0]).newInstance(new Object[0]);
			}"""; //$NON-NLS-1$
			CtMethod m = cc.getDeclaredMethod("load"); //$NON-NLS-1$
			m.setBody(body);

			c.setBytes(cc.toBytecode());
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	@SuppressWarnings("nls")
	private void processValueReaderDecorator(final WovenClass c) {
		CtClass cc = defrost(c, ValueReader.class);

		try {
			// boolean test(Class clazz)
			{
				String body = """
				{
					java.lang.Iterable instances = org.glassfish.hk2.osgiresourcelocator.ServiceLoader.lookupProviderInstances(org.eclipse.jnosql.communication.ValueReader.class);
					java.util.List readers = java.util.stream.StreamSupport.stream(instances.spliterator(), false).toList();
					for(int i = 0; i < readers.size(); i++) {
						org.eclipse.jnosql.communication.ValueReader r = (org.eclipse.jnosql.communication.ValueReader)readers.get(i);
						if(r.test($1)) {
							return true;
						}
					}
					return false;
			    }""";
				CtMethod m = cc.getDeclaredMethod("test"); //$NON-NLS-1$
				m.setBody(body);
			}

			// <T> T read(Class<T> clazz, Object value)
			{
				String body = """
				{
			        if ($1.isInstance($2)) {
			            return $1.cast($2);
			        }
					java.lang.Iterable instances = org.glassfish.hk2.osgiresourcelocator.ServiceLoader.lookupProviderInstances(org.eclipse.jnosql.communication.ValueReader.class);
					java.util.List readers = java.util.stream.StreamSupport.stream(instances.spliterator(), false).toList();
			        for(int i = 0; i < readers.size(); i++) {
						org.eclipse.jnosql.communication.ValueReader r = (org.eclipse.jnosql.communication.ValueReader)readers.get(i);
						if(r.test($1)) {
							return r.read($1, $2);
						}
					}
			        throw new UnsupportedOperationException("The type " + $1 + " is not supported yet");
			    }""";
				CtMethod m = cc.getDeclaredMethod("read"); //$NON-NLS-1$
				m.setBody(body);
			}

			c.setBytes(cc.toBytecode());
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	@SuppressWarnings("nls")
	private void processTypeReferenceReader(final WovenClass c) {
		CtClass cc = defrost(c, TypeReferenceReader.class);

		try {
			// boolean test(Class clazz)
			{
				String body = """
				{
					java.lang.Iterable instances = org.glassfish.hk2.osgiresourcelocator.ServiceLoader.lookupProviderInstances(org.eclipse.jnosql.communication.TypeReferenceReader.class);
					java.util.List readers = java.util.stream.StreamSupport.stream(instances.spliterator(), false).toList();
					for(int i = 0; i < readers.size(); i++) {
						org.eclipse.jnosql.communication.TypeReferenceReader r = (org.eclipse.jnosql.communication.TypeReferenceReader)readers.get(i);
						if(r.test($1)) {
							return true;
						}
					}
					return false;
			    }""";
				CtMethod m = cc.getDeclaredMethod("test"); //$NON-NLS-1$
				m.setBody(body);
			}

			// <T> T convert(TypeSupplier, Object value)
			{
				String body = """
				{
					java.lang.Iterable instances = org.glassfish.hk2.osgiresourcelocator.ServiceLoader.lookupProviderInstances(org.eclipse.jnosql.communication.TypeReferenceReader.class);
					java.util.List readers = java.util.stream.StreamSupport.stream(instances.spliterator(), false).toList();
			        for(int i = 0; i < readers.size(); i++) {
						org.eclipse.jnosql.communication.TypeReferenceReader r = (org.eclipse.jnosql.communication.TypeReferenceReader)readers.get(i);
						if(r.test($1)) {
							return r.convert($1, $2);
						}
					}
			        throw new UnsupportedOperationException("The type " + $1 + " is not supported yet");
			    }""";
				CtMethod m = cc.getDeclaredMethod("convert"); //$NON-NLS-1$
				m.setBody(body);
			}

			c.setBytes(cc.toBytecode());
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	private void processValueWriter(final WovenClass c) {
		CtClass cc = defrost(c);

		try {
			String body = """
			{
				java.lang.Iterable instances = org.glassfish.hk2.osgiresourcelocator.ServiceLoader.lookupProviderInstances(org.eclipse.jnosql.communication.ValueWriter.class);
				return java.util.stream.StreamSupport.stream(instances.spliterator(), false);
			}"""; //$NON-NLS-1$
			CtMethod m = cc.getDeclaredMethod("getWriters"); //$NON-NLS-1$
			m.setBody(body);

			c.setBytes(cc.toBytecode());
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	@SuppressWarnings("nls")
	private void processValueWriterDecorator(final WovenClass c) {
		CtClass cc = defrost(c, ValueWriter.class);

		try {
			// boolean test(Class clazz)
			{
				String body = """
				{
					java.lang.Iterable instances = org.glassfish.hk2.osgiresourcelocator.ServiceLoader.lookupProviderInstances(org.eclipse.jnosql.communication.ValueWriter.class);
					java.util.List writers = java.util.stream.StreamSupport.stream(instances.spliterator(), false).toList();
					for(int i = 0; i < writers.size(); i++) {
						org.eclipse.jnosql.communication.ValueWriter w = (org.eclipse.jnosql.communication.ValueWriter)writers.get(i);
						if(w.test($1)) {
							return true;
						}
					}
					return false;
			    }""";
				CtMethod m = cc.getDeclaredMethod("test"); //$NON-NLS-1$
				m.setBody(body);
			}

			// Object write(Object value)
			{
				String body = """
				{
			        Class clazz = $1.getClass();
					java.lang.Iterable instances = org.glassfish.hk2.osgiresourcelocator.ServiceLoader.lookupProviderInstances(org.eclipse.jnosql.communication.ValueWriter.class);
					java.util.List writers = java.util.stream.StreamSupport.stream(instances.spliterator(), false).toList();
			        for(int i = 0; i < writers.size(); i++) {
						org.eclipse.jnosql.communication.ValueWriter w = (org.eclipse.jnosql.communication.ValueWriter)writers.get(i);
						if(w.test($1)) {
							return w.write($1);
						}
					}
			        throw new UnsupportedOperationException("The type " + clazz + " is not supported yet");
			    }""";
				CtMethod m = cc.getDeclaredMethod("read"); //$NON-NLS-1$
				m.setBody(body);
			}

			c.setBytes(cc.toBytecode());
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	private void processClassConverter(final WovenClass c) {
		CtClass cc = defrost(c, DataRepository.class);

		try {
			String body = """
			{
				java.lang.Iterable instances = org.glassfish.hk2.osgiresourcelocator.ServiceLoader.lookupProviderInstances(org.eclipse.jnosql.mapping.metadata.ClassConverter.class);
				return java.util.Objects.requireNonNull((org.eclipse.jnosql.mapping.metadata.ClassConverter)instances.iterator().next());
			}"""; //$NON-NLS-1$
			CtMethod m = cc.getDeclaredMethod("load"); //$NON-NLS-1$
			m.setBody(body);

			c.setBytes(cc.toBytecode());
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	private CtClass defrost(final WovenClass c, final Class<?>... contextClass) {
		ClassPool pool = new ClassPool();
		pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
		pool.appendClassPath(new ClassClassPath(org.glassfish.hk2.osgiresourcelocator.ServiceLoader.class));
		for(Class<?> ctx : contextClass) {
			pool.appendClassPath(new ClassClassPath(ctx));
		}
		CtClass cc;
		try(InputStream is = new ByteArrayInputStream(c.getBytes())) {
			cc = pool.makeClass(is);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		cc.defrost();
		return cc;
	}

	@SuppressWarnings("nls")
	private void processConstructorBuilder(final WovenClass c) {
		ClassPool pool = new ClassPool();
		pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
		pool.appendClassPath(new LoaderClassPath(c.getBundleWiring().getClassLoader()));
		CtClass cc;
		try(InputStream is = new ByteArrayInputStream(c.getBytes())) {
			cc = pool.makeClass(is);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		cc.defrost();

		try {
			// ConstructorBuilder of(ConstructorMetadata constructor)
			{
				String body = """
			    {
					java.lang.Iterable instances = org.glassfish.hk2.osgiresourcelocator.ServiceLoader.lookupProviderInstances(org.eclipse.jnosql.mapping.metadata.ConstructorBuilderSupplier.class);
					org.eclipse.jnosql.mapping.metadata.ConstructorBuilderSupplier supplier = (org.eclipse.jnosql.mapping.metadata.ConstructorBuilderSupplier)instances.iterator().next();
					return supplier.apply($1);
		        }""";
				CtMethod m = cc.getDeclaredMethod("of"); //$NON-NLS-1$
				m.setBody(body);
			}

			c.setBytes(cc.toBytecode());
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * As of JNoSQL 1.1.1, DefaultConstructorBuilder's addEmptyParameter method only accounts for
	 * object types, not primitives. This patch works around that by adding {@code false} for
	 * empty boolean parameters and {@code 0} for other primitives.
	 */
	@SuppressWarnings("nls")
	private void processDefaultConstructorBuilder(final WovenClass c) {
		ClassPool pool = new ClassPool();
		pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
		pool.appendClassPath(new LoaderClassPath(c.getBundleWiring().getClassLoader()));
		CtClass cc;
		try(InputStream is = new ByteArrayInputStream(c.getBytes())) {
			cc = pool.makeClass(is);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		cc.defrost();

		try {
			// void addEmptyParameter()
			{
				String body = """
				{
					java.lang.reflect.Constructor constructor = ((org.eclipse.jnosql.mapping.reflection.DefaultConstructorMetadata) this.metadata).constructor();
			        Class type = constructor.getParameterTypes()[this.values.size()];
			        if(boolean.class.equals(type)) {
				       this.values.add(Boolean.TRUE);
			        } else if(type.isPrimitive()) {
						this.values.add(Integer.valueOf(0));
			        } else {
			        	this.values.add(null);
			        }
			    }""";
				CtMethod m = cc.getDeclaredMethod("addEmptyParameter"); //$NON-NLS-1$
				m.setBody(body);
			}

			c.setBytes(cc.toBytecode());
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * As of JNoSQL 1.1.3, DefaultMapFieldMetadata throws an exception in its constructor when
	 * a field type is a Map but isn't itself parameterized (e.g. JsonObject). This patch
	 * works around that by also checking implemented interfaces of the type. This will still
	 * fail if the type doesn't directly implement or extend Map, but it should help in practical
	 * cases. 
	 */
	@SuppressWarnings("nls")
	private void processDefaultMapFieldMetadata(final WovenClass c) {
		ClassPool pool = new ClassPool();
		pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
		pool.appendClassPath(new LoaderClassPath(c.getBundleWiring().getClassLoader()));
		CtClass cc;
		try(InputStream is = new ByteArrayInputStream(c.getBytes())) {
			cc = pool.makeClass(is);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		cc.defrost();

		try {
			{
				String body = """
				{
					super($1, $2, $3, $5, $6, $7, $8);
			        this.typeSupplier = $4;
			        java.lang.reflect.Type mapType = this.field.getGenericType();
			        if(!(mapType instanceof java.lang.reflect.ParameterizedType)) {
						java.lang.reflect.Type[] interfaces = ((java.lang.Class)mapType).getGenericInterfaces();
						for(int i = 0; i < interfaces.length; i++) {
							if(interfaces[i].getTypeName().startsWith("java.util.Map")) {
								mapType = interfaces[i];
								break;
							}
						}
			        }
			        this.keyType = (java.lang.Class) ((java.lang.reflect.ParameterizedType) mapType)
			                .getActualTypeArguments()[0];
			        this.valueType = (java.lang.Class) ((java.lang.reflect.ParameterizedType) mapType)
			                .getActualTypeArguments()[1];
			    }""";
				CtConstructor m = cc.getDeclaredConstructors()[0];
				m.setBody(body);
			}

			c.setBytes(cc.toBytecode());
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * Same as {@link #processDefaultMapFieldMetadata(WovenClass)}, but for the
	 * {@code Collection} type. Unlike in that class, this happens in two methods.
	 */
	@SuppressWarnings("nls")
	private void processDefaultCollectionFieldMetadata(final WovenClass c) {
		ClassPool pool = new ClassPool();
		pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
		pool.appendClassPath(new LoaderClassPath(c.getBundleWiring().getClassLoader()));
		CtClass cc;
		try(InputStream is = new ByteArrayInputStream(c.getBytes())) {
			cc = pool.makeClass(is);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		cc.defrost();

		try {
			// hasFieldAnnotation(Class)
			{
				String body = """
				{
					java.lang.reflect.Type mapType = this.field.getGenericType();
			        if(!(mapType instanceof java.lang.reflect.ParameterizedType)) {
						java.lang.reflect.Type[] interfaces = ((java.lang.Class)mapType).getGenericInterfaces();
						for(int i = 0; i < interfaces.length; i++) {
							if(interfaces[i].getTypeName().startsWith("java.util.Collection")) {
								mapType = interfaces[i];
								break;
							} else if(interfaces[i].getTypeName().startsWith("java.util.Set")) {
								mapType = interfaces[i];
								break;
							} else if(interfaces[i].getTypeName().startsWith("java.util.List")) {
								mapType = interfaces[i];
								break;
							}
						}
			        }
			        return ((java.lang.Class) ((java.lang.reflect.ParameterizedType) mapType)
			                .getActualTypeArguments()[0]).getAnnotation($1) != null;
			    }""";
				CtMethod m = cc.getDeclaredMethod("hasFieldAnnotation");
				m.setBody(body);
			}
			
			// elementType
			{
				String body = """
				{
					java.lang.reflect.Type mapType = this.field.getGenericType();
			        if(!(mapType instanceof java.lang.reflect.ParameterizedType)) {
						java.lang.reflect.Type[] interfaces = ((java.lang.Class)mapType).getGenericInterfaces();
						for(int i = 0; i < interfaces.length; i++) {
							if(interfaces[i].getTypeName().startsWith("java.util.Collection")) {
								mapType = interfaces[i];
								break;
							} else if(interfaces[i].getTypeName().startsWith("java.util.Set")) {
								mapType = interfaces[i];
								break;
							} else if(interfaces[i].getTypeName().startsWith("java.util.List")) {
								mapType = interfaces[i];
								break;
							}
						}
			        }
			        return (java.lang.Class) ((java.lang.reflect.ParameterizedType) mapType)
			                .getActualTypeArguments()[0];
			    }""";
				CtMethod m = cc.getDeclaredMethod("elementType");
				m.setBody(body);
			}

			c.setBytes(cc.toBytecode());
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
}
