package org;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class LazyPersistentList<T> implements List<T> {

	protected List<T> l;
	protected boolean isLoaded = false;

	protected void checkIfHaveToLoad() {
		if (!isLoaded) {
			this.l = load();
			this.isLoaded = true;
		}
	}

	public boolean isLoaded() {
		return isLoaded;
	}

	public abstract List<T> load();

	// ============================================================
	// ============================================================
	// ============================================================

	@Override
	public String toString() {
		checkIfHaveToLoad();
		return l.toString();
	}

	public void forEach(Consumer<? super T> action) {
		checkIfHaveToLoad();
		l.forEach(action);
	}

	public int size() {
		checkIfHaveToLoad();
		return l.size();
	}

	public boolean isEmpty() {
		checkIfHaveToLoad();
		return l.isEmpty();
	}

	public boolean contains(Object o) {
		checkIfHaveToLoad();
		return l.contains(o);
	}

	public Iterator<T> iterator() {
		checkIfHaveToLoad();
		return l.iterator();
	}

	public Object[] toArray() {
		checkIfHaveToLoad();
		return l.toArray();
	}

	public <TT> TT[] toArray(TT[] a) {
		checkIfHaveToLoad();
		return l.toArray(a);
	}

	public boolean add(T e) {
		checkIfHaveToLoad();
		return l.add(e);
	}

	public boolean remove(Object o) {
		checkIfHaveToLoad();
		return l.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		checkIfHaveToLoad();
		return l.containsAll(c);
	}

	public boolean addAll(Collection<? extends T> c) {
		checkIfHaveToLoad();
		return l.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends T> c) {
		checkIfHaveToLoad();
		return l.addAll(index, c);
	}

	public boolean removeAll(Collection<?> c) {
		checkIfHaveToLoad();
		return l.removeAll(c);
	}

	public <TT> TT[] toArray(IntFunction<TT[]> generator) {
		checkIfHaveToLoad();
		return l.toArray(generator);
	}

	public boolean retainAll(Collection<?> c) {
		checkIfHaveToLoad();
		return l.retainAll(c);
	}

	public void replaceAll(UnaryOperator<T> operator) {
		checkIfHaveToLoad();
		l.replaceAll(operator);
	}

	public void sort(Comparator<? super T> c) {
		checkIfHaveToLoad();
		l.sort(c);
	}

	public void clear() {
		checkIfHaveToLoad();
		l.clear();
	}

	public boolean equals(Object o) {
		checkIfHaveToLoad();
		return l.equals(o);
	}

	public int hashCode() {
		checkIfHaveToLoad();
		return l.hashCode();
	}

	public T get(int index) {
		checkIfHaveToLoad();
		return l.get(index);
	}

	public boolean removeIf(Predicate<? super T> filter) {
		checkIfHaveToLoad();
		return l.removeIf(filter);
	}

	public T set(int index, T element) {
		checkIfHaveToLoad();
		return l.set(index, element);
	}

	public void add(int index, T element) {
		checkIfHaveToLoad();
		l.add(index, element);
	}

	public T remove(int index) {
		checkIfHaveToLoad();
		return l.remove(index);
	}

	public int indexOf(Object o) {
		checkIfHaveToLoad();
		return l.indexOf(o);
	}

	public int lastIndexOf(Object o) {
		checkIfHaveToLoad();
		return l.lastIndexOf(o);
	}

	public ListIterator<T> listIterator() {
		checkIfHaveToLoad();
		return l.listIterator();
	}

	public ListIterator<T> listIterator(int index) {
		checkIfHaveToLoad();
		return l.listIterator(index);
	}

	public List<T> subList(int fromIndex, int toIndex) {
		checkIfHaveToLoad();
		return l.subList(fromIndex, toIndex);
	}

	public Spliterator<T> spliterator() {
		checkIfHaveToLoad();
		return l.spliterator();
	}

	public Stream<T> stream() {
		checkIfHaveToLoad();
		return l.stream();
	}

	public Stream<T> parallelStream() {
		checkIfHaveToLoad();
		return l.parallelStream();
	}

}
