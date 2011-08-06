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
package com.medallia.tiny.string;

import java.util.Collection;
import java.util.Map;

import org.antlr.stringtemplate.AttributeRenderer;

import com.medallia.tiny.Empty;
import com.medallia.tiny.Encoding;
import com.medallia.tiny.Implement;
import com.medallia.tiny.string.StringTemplateBuilder.SimpleAttributeRenderer;

/**
 * Class that handles building JS data structures
 * from Java data structures, and handles proper
 * escaping of Strings.
 */
public class JsString extends StringBase {
	/** String template attribute renderer used for rendering JsStrings. */
	public static final AttributeRenderer ST_RENDERER = new SimpleAttributeRenderer() {
		public String toString(Object o) {
			return ((JsString)o).inScript().asString();
		}
	};

	private JsString(String s) {
		super(s);
	}

	/** @return an HtmlString that can be rendered in HTML attributes, e.g. onlick="foo.inAttr" */
	public HtmlString inAttr() {
		return HtmlString.fromText(s);
	}
	/** @return an HtmlString that can be rendered in a JS file */
	@SuppressWarnings("deprecation")
	public HtmlString inScript() {
		// not technically valid; string may contain </string> but no way
		// to escape it without full parser
		return HtmlString.rawUnsafe(s);
	}
	/** @return JsString representation of the given argument */
	public static JsString forString(String s) {
		 return new JsString(escapeStr(s));
	}
	/** @return JsString representation of the given argument */
	public static JsString forNumber(Number n) {
		return new JsString(n.toString());
	}
	/** @return JsString representation of the given argument */
	public static JsString forBoolean(Boolean b) {
		return new JsString(b.toString());
	}

	/** @return JsString representation of the given argument */
	public static JsString forIntArray(Collection<Integer> c) {
		StringBuilder sb = new StringBuilder("[");
		String sep = "";
		for (Integer i : c) {
			sb.append(sep).append(Integer.toString(i));
			sep=",";
		}
		return new JsString(sb.append("]").toString());
	}
	/** @return JsString representation of the given argument */
	public static JsString forArray(Collection<?> c) {
		StringBuilder sb = new StringBuilder("[");
		String sep = "";
		for (Object val : c) {
			sb.append(sep).append(forObject(val, DEFAULT_MAP_SEPARATOR).getRawJs());
			sep=",";
		}
		return new JsString(sb.append("]").toString());
	}
	/** Default map separator. */
	private static final String DEFAULT_MAP_SEPARATOR = ",\n";
	/** @return JsString representation of the given argument, using the default separator. */
	public static JsString forMap(Map<?,?> m) {
		return forMap(m, DEFAULT_MAP_SEPARATOR);
	}
	/** @return JsString representation of the given argument, using the given separator. */
	public static JsString forMap(Map<?,?> m, String separator) {
		StringBuilder sb = new StringBuilder("{");
		String sep = "";
		for (Map.Entry<?,?> entry : m.entrySet()) {
			sb.append(sep).append(escapeStr(String.valueOf(entry.getKey())))
			  .append(":").append(forObject(entry.getValue(), separator).getRawJs());
			sep = separator;
		}
		return new JsString(sb.append("}").toString());
	}
	/** @return JsString representation of the given argument */
	public static JsString forObject(Object o) {
		return forObject(o, DEFAULT_MAP_SEPARATOR);
	}
	/** @return JsString representation of the given argument */
	private static JsString forObject(Object o, String mapSeparator) {
		if (o == null) return raw("null");
		if (o instanceof JsString) return ((JsString)o);
		if (o instanceof Map) return forMap((Map)o, mapSeparator);
		if (o instanceof Collection) return forArray((Collection)o);
		if (o instanceof Number) return forNumber((Number)o);
		if (o instanceof Boolean) return forBoolean((Boolean)o);
		return forString(String.valueOf(o));
	}
	private static String escapeStr(String string) {
		if (string==null) {
			new RuntimeException("null string in js escape");
			return "***THIS STRING WAS NULL***";
		}
		if (string.isEmpty()) {
			return "\"\"";
		}

		char b, c = 0;
		int i, len = string.length();
		StringBuilder sb = new StringBuilder(len + 16);
		String t;

		sb.append('"');
		for (i = 0; i < len; i += 1) {
			b = c;
			c = string.charAt(i);
			switch (c) {
				case '\\':
				case '"':
					sb.append('\\');
					sb.append(c);
					break;
				case '/':
					if (b == '<') {
						sb.append('\\');
					}
					sb.append(c);
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\r':
					sb.append("\\r");
					break;
				default:
					if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
						t = "000" + Integer.toHexString(c);
						sb.append("\\u" + t.substring(t.length() - 4));
					} else {
						sb.append(c);
					}
			}
		}
		sb.append('"');
		return sb.toString();
	}
	
	@Implement public JsString subSequence(int arg0, int arg1) {
		return new JsString(s.substring(arg0,arg1));
	}

	public String asString() {
		return s;
	}

	private String getRawJs() { return s; }

	/** @return a strong hash of the content of the given JsString objects */
	public static String hash(JsString... jsStrings) {
		StringBuilder sb = Empty.sb();
		for (JsString js : jsStrings)
			sb.append(js.getRawJs());
		return Encoding.md5(sb.toString());
	}

	/** @return JsString representing the given string, which is not escaped. Use with care. */
	public static JsString raw(String string) {
		return new JsString(string);
	}
}
