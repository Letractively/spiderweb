/*
 * This file is part of the Spider Web Framework.
 * 
 * The Spider Web Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Spider Web Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Spider Web Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.medallia.spider.api;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import com.medallia.spider.IOHelpers;
import com.medallia.spider.api.StRenderable.DynamicInput;
import com.medallia.spider.api.StRenderable.Input;
import com.medallia.spider.api.StRenderer.InputArgHandler;
import com.medallia.spider.api.StRenderer.InputArgParser;
import com.medallia.tiny.Empty;
import com.medallia.tiny.Implement;
import com.medallia.tiny.Strings;

/** Implementation of {@link DynamicInput} that is also used to parse the
 * values for the static input variables.
 */
public class DynamicInputImpl implements DynamicInput, InputArgHandler {

	private final Map<String, String[]> inputParams;
	private final Map<String, byte[]> fileUploads;
	
	private final Map<Class<?>, InputArgParser<?>> inputArgParsers = Empty.hashMap();

	/**
	 * @param the request from which to read the request parameters
	 */
	public DynamicInputImpl(HttpServletRequest request) {
		if (ServletFileUpload.isMultipartContent(request)) {
			this.inputParams = Empty.hashMap();
			this.fileUploads = Empty.hashMap();

			ServletFileUpload upload = new ServletFileUpload();
			try {
				FileItemIterator iter = upload.getItemIterator(request);
				while (iter.hasNext()) {
					FileItemStream item = iter.next();
					String fieldName = item.getFieldName();
					InputStream stream = item.openStream();
					if (item.isFormField()) {
						inputParams.put(fieldName, new String[] { Streams.asString(stream) });
					} else {
						fileUploads.put(fieldName, IOHelpers.toByteArray(stream));
					}
				}
			} catch (IOException e) {
				throw new IllegalArgumentException("Failed to parse multipart", e);
			} catch (FileUploadException e) {
				throw new IllegalArgumentException("Failed to parse multipart", e);
			}
		} else {
			@SuppressWarnings("unchecked")
			Map<String, String[]> reqParams = request.getParameterMap();
			this.inputParams = reqParams;
			this.fileUploads = Collections.emptyMap();
		}
	}
	
	@Implement public <X> void registerArgParser(Class<X> type, InputArgParser<X> parser) {
		inputArgParsers.put(type, parser);
	}

	@Implement public <X> X getInput(String name, Class<X> type) {
		return getInput(name, type, emptyAnnotatedElement);
	}
	
	/**
	 * Method used for parse values for the methods declared in {@link Input}.
	 */
	public <X> X getInput(String name, Class<X> type, AnnotatedElement anno) {
		// Special case for file uploads
		if (type.isArray() && type.getComponentType() == Byte.TYPE) {
			return type.cast(fileUploads.get(name));
		}
		
		if (type.isArray() && anno.isAnnotationPresent(Input.MultiValued.class)) {
			// return type is an array; grab all
			Object o = inputParams.get(name);
			return type.cast(parseMultiValue(type, o, anno));
		}
		
		String v = Strings.extract(inputParams.get(name));
		
		// boolean is used for checkboxes, and false is encoded as a missing value
		if (type == Boolean.class || type == Boolean.TYPE) {
			@SuppressWarnings("unchecked")
			X x = (X) Boolean.valueOf(v != null);
			return x;
		}
		
		// the remaining types have proper null values
		if (v == null) return null;
		
		// Do not use Class.cast here since it does not work on primitive types
		@SuppressWarnings("unchecked")
		X x = (X) parseSingleValue(type, v, anno);
		return x;
	}
	
	/**
	 * @param rt some kind of array class
	 * @param data null, String or String[]
	 * @return parsed data as per parseSingleValue
	 * @throws AssertionError if parseSingleValue does
	 */
	private Object parseMultiValue(Class<?> rt, Object data, AnnotatedElement anno) throws AssertionError {
		String[] xs;
		// normalize the zero-and-one cases
		if (data == null) {
			xs = new String[0];
		} else if (data instanceof String[]) {
			xs = (String[]) data;
		} else {
			xs = new String[] { data.toString() };
		}
		
		Class<?> comp = rt.getComponentType();
		Object arr = Array.newInstance(rt.getComponentType(), xs.length);
		for (int i=0; i < xs.length; i++) {
			Array.set(arr, i, parseSingleValue(comp, xs[i], anno));
		}
		return arr;
	}

	private Object parseSingleValue(Class<?> rt, String v, AnnotatedElement anno) throws AssertionError {
		if (rt.isEnum()) {
			String vlow = v.toLowerCase();
			for (Enum e : rt.asSubclass(Enum.class).getEnumConstants()) {
				if (e.name().toLowerCase().equals(vlow)) return e;
			}
			throw new AssertionError("Enum constant not found: " + v);
		} else if (rt == Integer.class) {
			// map blank strings to null
			return Strings.hasContent(v) ? Integer.valueOf(v) : null;
		} else if (rt == Integer.TYPE) {
			// primitive int must have a value
			return Integer.valueOf(v);
		} else if (rt == Long.class) {
			// map blank strings to null
			return Strings.hasContent(v) ? Long.valueOf(v) : null;
		} else if (rt == Long.TYPE) {
			// primitive long must have a value
			return Long.valueOf(v);
		} else if (rt == Double.class) {
			// map blank strings to null
			return Strings.hasContent(v) ? Double.valueOf(v) : null;
		} else if (rt == Double.TYPE) {
			// primitive double must have a value
			return Double.valueOf(v);
		} else if (rt == String.class) {
			return v;
		} else if (rt.isArray()) {
			Input.List ann = anno.getAnnotation(Input.List.class);
			if (ann == null) throw new AssertionError("Array type but no annotation (see "+Input.class+"): "+anno);
			String separator = ann.separator();
			String[] strVals = v.split(separator, -1);
			Class<?> arrayType = rt.getComponentType();
			Object a = Array.newInstance(arrayType, strVals.length);
			for (int i = 0; i < strVals.length; i++) {
				Array.set(a, i, parseSingleValue(arrayType, strVals[i], anno));
			}
			return a;
		} else {
			InputArgParser<?> argParser = inputArgParsers.get(rt);
			if (argParser != null) {
				return argParser.parse(v);
			}
		}
		throw new AssertionError("Unknown return type " + rt + " (val: " + v + ")");
	}
	
	private static final AnnotatedElement emptyAnnotatedElement = new AnnotatedElement() {
		@Implement public <T extends Annotation> T getAnnotation(Class<T> annotationType) { return null; }
		@Implement public Annotation[] getAnnotations() { return null; }
		@Implement public Annotation[] getDeclaredAnnotations() { return null; }
		@Implement public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) { return false; }
	};

}
