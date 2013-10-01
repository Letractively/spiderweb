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
package com.medallia.tiny;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/** Support for some functional idioms */
public class Funcs {
	public static final Func<Object,String> TO_STRING = new Func<Object, String>() {
		@Override
		public String call(Object a) {
			return a == null ? null : a.toString();
		}
	};
	
	/** Copy the array, passing each element through the function represented by func.
	 * Requires the Class<B> in order to allocate the array */
	public static <A,B> B[] mapArray(A[] as, Class<B> bClass, Func<A,B> func) {
		 B[] bs = CollUtils.newArray(bClass, as.length);
		 for (int i=0; i<as.length; i++) bs[i] = func.call(as[i]);
		 return bs;
	}
	
	/** @return a list with each element of the given iterable passed through the given function. */
	public static <A,B> List<B> map(Iterable<? extends A> as, Func<A,B> func) {
		 return map(Empty.<B>list(), as, func);
	}
	
	/**
	 * @return a list with each element of the given collection passed through
	 *         the given function. This method uses {@link Collection#size()} to
	 *         allocate the list ahead of time to avoid resizing operations.
	 */
	public static <A,B> List<B> map(Collection<? extends A> as, Func<A,B> func) {
		 return map(Empty.<B>list(as.size()), as, func);
	}
	
	private static <B, A> List<B> map(List<B> bs, Iterable<? extends A> as, Func<A, B> func) {
		for (A a : as)
			bs.add(func.call(a));
		return bs;
	}

	/**
	 * @return a map with the values obtained from the iterator as values and the
	 *         keys from the given function.
	 */
	public static <K, V> Map<K, V> buildMap(Iterable<? extends V> it, Func<V, K> func) {
		return buildMap(Empty.<K, V>hashMap(), it, func);
	}
	
	/**
	 * @return a map with the values obtained from the iterator as values and the
	 *         keys from the given function. The iteration order of the map is
	 *         the same as the given iterable.
	 */
	public static <K, V> Map<K, V> buildOrderedMap(Iterable<? extends V> it, Func<V, K> func) {
		return buildMap(Empty.<K, V>linkedHashMap(), it, func);
	}
	
	private static <K, V> Map<K, V> buildMap(Map<K, V> m, Iterable<? extends V> it, Func<V, K> func) {
		for (V v : it)
			m.put(func.call(v), v);
		return m;
	}

	/** @return map with keys obtained from the given function that maps to a list with all the values for that key */
	public static <K, V> Map<K, List<V>> partition(Iterable<? extends V> it, Func<V, K> func) {
		return partition(Empty.<K, List<V>>hashMap(), it, func);
	}
	/** @return linked map with keys obtained from the given function that maps to a list with all the values for that key */
	public static <K, V> Map<K, List<V>> partitionOrdered(Iterable<? extends V> it, Func<V, K> func) {
		return partition(Empty.<K, List<V>>linkedHashMap(), it, func);
	}
	private static <K, V> Map<K, List<V>> partition(Map<K, List<V>> m, Iterable<? extends V> it, Func<V, K> func) {
		for (V v : it)
			CollUtils.addToMapList(m, func.call(v), v);
		return m;
	}

	/** Lazy version of 'map' -- will map each element as it is returned by the iterator */
	public static <A,B> Iterable<B> wrapIterable(final Iterable<? extends A> as, final Func<A,B> func) {
		return new Iterable<B>() {
			@Override
			
			public Iterator<B> iterator() {
				final Iterator<? extends A> it = as.iterator();
				return new Iterator<B>() {
					@Override
					public boolean hasNext() {
						return it.hasNext();
					}
					@Override
					public B next() {
						return func.call(it.next());
					}
					@Override
					public void remove() {
						it.remove();
					}			
				};
			}
		};
	}
	/** Given a way to compute a (Comparable) sort key for an object, return a Comparator for the natural ordering of that. 
	 * E.g. compareBy(new Func<Person,String() { String call(Person p) { return p.getFirstName(); } }) */
	public static <X,Y extends Comparable<Y>> Comparator<X> compareBy(final Func<? super X, ? extends Y> func) {
		return new Comparator<X>() {
			@Override
			public int compare(X o1, X o2) {
				Y a1 = func.call(o1);
				Y a2 = func.call(o2);
				return (a1 == null) ? (a2 == null ? 0 : -1) : a1.compareTo(a2);
			}
		};
	}
	/** Sort by a function */
	public static <X,Y extends Comparable<Y>> void sortBy(List<X> coll, final Func<? super X, ? extends Y> func) {
		Collections.sort(coll, compareBy(func));
	}
	/** Identity function */
	public static <X> Func<X,X> identity() {
		return new Func<X,X>() {
			@Override
			public X call(X a) {
				return a;
			}
		};
	}
	/** Format with a fixed format (should have one %) */
	public static <X> Func<X,String> stringFormat(final String s) {
		return new Func<X,String>() {
			@Override
			public String call(X a) {
				return String.format(s, a);
			}
		};
	}
}
