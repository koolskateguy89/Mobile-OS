package com.github.koolskateguy89.mobileos.app.system.notes;

import java.util.Comparator;
import java.util.List;

import lombok.Getter;

// TODO: SortBy
public enum SortBy {

	TITLE(Comparator.comparing(Note::getTitle)),

	DATE_CREATED(Comparator.comparing(Note::getDateCreated)),

	DATE_MODIFIED(Comparator.comparing(Note::getDateModified)),

	;

	public enum Order {
		ASCENDING, DESCENDING;
	}


	@Getter
	private final Comparator<Note> comparator;

	private SortBy(Comparator<Note> comparator) {
		this.comparator = comparator;
	}

	public void sort(List<Note> notes) {
		notes.sort(comparator);
	}

	public static SortBy getDefault() {
		return DATE_CREATED;
	}

}
