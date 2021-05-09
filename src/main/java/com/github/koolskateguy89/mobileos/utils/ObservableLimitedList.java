package com.github.koolskateguy89.mobileos.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.util.Callback;

import com.sun.javafx.collections.ObservableListWrapper;

import lombok.Getter;
import lombok.Setter;

/**
 * AFAIK, all operations that increase the list's size internally use {@link #add(E)}, so only overriding that
 * <i>should</i> be fine.
 * @param <E> list type
 */
public class ObservableLimitedList<E> extends ObservableListWrapper<E> {

	// Mutations made to the backing list are don't trigger observers
	@Getter
	private final List<E> backingList;

	@Getter @Setter
	private boolean tail;

	@Getter
	private final int limit;

	public ObservableLimitedList(int limit, boolean tail) {
		this(limit, tail, new ArrayList<>(limit));
	}

	public ObservableLimitedList(int limit, boolean tail, Callback<E, Observable[]> extractor) {
		this(limit, tail, new ArrayList<>(limit), extractor);
	}

	/**
	 * This is basically the same as calling {@link FXCollections#observableList(List)}.
	 *
	 * If {@code tail} is false, the list will just be the first {@code limit} elements and all {@link #add(E)}
	 * operations are no-ops, unless an element is removed
	 *
	 * @param limit size limit
	 * @param tail whether to use the last n elements or first n elements, given n = {@code limit}
	 * @param backingList the backing list
	 * @param <L> the type of the backing list (has to be RandomAccess)
	 */
	public <L extends List<E> & RandomAccess> ObservableLimitedList(int limit, boolean tail, L backingList) {
		super(backingList);
		this.limit = limit;
		this.tail = tail;
		this.backingList = backingList;
	}

	/**
	 * This is basically the same as calling {@link FXCollections#observableList(List, Callback)}.
	 *
	 * @param limit size limit
	 * @param tail whether to use the last n elements or first n elements, given n = {@code limit}
	 * @param backingList the backing list
	 * @param extractor element to {@code Observable[]} converter
	 * @param <L> the type of the backing list (has to be RandomAccess)
	 */
	public <L extends List<E> & RandomAccess> ObservableLimitedList(int limit, boolean tail, L backingList,
	                                                                Callback<E, Observable[]> extractor) {
		super(backingList, extractor);
		this.limit = limit;
		this.tail = tail;
		this.backingList = backingList;
	}


	@Override
	public boolean add(E value) {
		final int size = this.size();

		if (size == limit) {
			if (tail) {
				// remove the first value then add new value to end
				backingList.remove(0);
			} else {
				// no-op
				// If using head, no point of adding
				return false;
			}
		} else if (size > limit) {
			// should not happen
			throw new RuntimeException("Somehow reached above limit (possibly an 'addAll' op doesn't use 'add')");
		}

		return super.add(value);
	}

}
