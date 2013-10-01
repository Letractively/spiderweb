package com.medallia.spider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.medallia.tiny.Func;

/** Abstraction for an element in an HTML dropdown (option element) */
public class DropdownElement {
	
	private final String value, text;
	private final boolean selected;
	
	private DropdownElement(String value, String text, boolean selected) {
		this.value = value;
		this.text = text;
		this.selected = selected;
	}

	/** @return the value attribute */
	public String getValue() { return value; }
	/** @return the text to display for the option */
	public String getText() { return text; }
	/** @return true if this option is currently selected */
	public boolean isSelected() { return selected; }
	

	/** @return {@link DropdownElement} objects for the given list; the functions are used to obtain the value and text for each element */
	public static <X> List<DropdownElement> fromList(List<? extends X> l, final String selectedVar, final Func<? super X, String> valueFunc, final Func<? super X, String> textFunc) {
		return Lists.transform(l, new Function<X, DropdownElement>() {
			@Override
			public DropdownElement apply(X x) {
				String value = valueFunc.call(x);
				return new DropdownElement(value, textFunc.call(x), selectedVar != null && selectedVar.equals(value));
			}
		});
	}

	/** @return {@link DropdownElement} objects for the given enums */
	public static <X extends Enum> List<DropdownElement> fromEnum(Class<X> type, final X selected) {
		return Lists.transform(Arrays.asList(type.getEnumConstants()), new Function<X, DropdownElement>() {
			@Override
			public DropdownElement apply(X x) {
				return new DropdownElement(String.valueOf(x.name()), x.toString(), selected == x);
			}
		});
	}
	
	/** Abstraction for the HTML optgroup element */
	public interface DropdownOptGroup {
		/** @return the text label for the group */
		String getText();
		/** @return the dropdown elements in this group */
		List<DropdownElement> getOptions();
	}
	
	/** @return {@link DropdownOptGroup} objects for the given map */
	public static <X> List<DropdownOptGroup> fromMap(Map<?, List<X>> m, final X selectedItem, final Func<? super X, String> valueFunc, final Func<? super X, String> textFunc) {
		return FluentIterable.from(m.entrySet())
				.transform(new Function<Map.Entry<?, List<X>>, DropdownOptGroup>() {
					@Override
					public DropdownOptGroup apply(final Map.Entry<?, List<X>> me) {
						return new DropdownOptGroup() {
							@Override
							public String getText() { return String.valueOf(me.getKey()); }
							@Override
							public List<DropdownElement> getOptions() {
								return Lists.transform(me.getValue(), new Function<X, DropdownElement>() {
									@Override
									public DropdownElement apply(X x) {
										String value = valueFunc.call(x);
										return new DropdownElement(value, textFunc.call(x), selectedItem != null && valueFunc.call(selectedItem).equals(value));
									}
								});
							}
						};
					}
				})
				.toList();
	}
	
}
