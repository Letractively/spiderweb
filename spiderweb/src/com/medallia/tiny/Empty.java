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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Type-inferring methods for empty objects
 * 
 * @author kristian
 */

public final class Empty {

	public static <X> Set<X> synchronizedSet() { return Collections.synchronizedSet(Empty.<X>hashSet()); }
	public static <K, V> Map<K, V> synchronizedMap() { return Collections.synchronizedMap(Empty.<K, V>hashMap()); }
	public static <X,Y> ConcurrentMap<X, Y> concurrentMap() { return new ConcurrentHashMap<X,Y>(); }

	public static <K, V> HashMap<K, V> hashMap() { return new HashMap<K, V>(); }
	public static <X,Y> HashMap<X, Y> hashMap(Map<? extends X, ? extends Y> m) { return new HashMap<X,Y>(m); }
	public static <K, V> Map<K, V> hashMapWithInitialCapacity(int cap) { return new HashMap<K, V>(cap); }

	public static <X> Set<X> hashSet() { return realHashSet(); }
	public static <X> Set<X> hashSet(Collection<? extends X> c) { return new HashSet<X>(c); }

	public static <X> HashSet<X> realHashSet() { return new HashSet<X>(); }
	
	public static <X> Set<X> linkedHashSet() { return new LinkedHashSet<X>(); }
	public static <X> Set<X> linkedHashSet(Collection<? extends X> c) { return new LinkedHashSet<X>(c); }
	public static <X,Y> Map<X, Y> linkedHashMap() { return new LinkedHashMap<X,Y>(); }
	public static <X,Y> Map<X, Y> linkedHashMap(Map<? extends X, ? extends Y> m) { return new LinkedHashMap<X,Y>(m); }
	
	public static <X extends Comparable> SortedSet<X> sortedSet() { return new TreeSet<X>(); }
	public static <X extends Comparable> SortedSet<X> sortedSet(Collection<? extends X> c) { return new TreeSet<X>(c); }
	
	public static <X> CopyOnWriteArrayList<X> copyOnWriteList() { return new CopyOnWriteArrayList<X>(); }

	public static <X> List<X> list() { return arrayList(); }
	public static <X> List<X> list(int size) { return new ArrayList<X>(size); }
	public static <X> List<X> list(Collection<? extends X> coll) { return new ArrayList<X>(coll); }
	public static <X> List<X> synchronizedList() { return Collections.synchronizedList(new ArrayList<X>()); }
	
	public static <X> ArrayList<X> arrayList() { return new ArrayList<X>(); }

	public static <X> LinkedList<X> linkedList() { return new LinkedList<X>(); }
	public static <X> LinkedList<X> linkedList(Collection<? extends X> l) { return new LinkedList<X>(l); }

	public static <X> ConcurrentLinkedQueue<X> concurrentLinkedQueue() { return new ConcurrentLinkedQueue<X>(); }
	public static <X> LinkedBlockingQueue<X> linkedBlockingQueue() { return new LinkedBlockingQueue<X>(); }

	public static StringBuilder sb() { return new StringBuilder(); }

}
