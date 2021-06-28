package com.github.koolskateguy89.mobileos.app.system.notes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/*
 * Not sure whether to include the NotePreview & NoteEditor here & lazily instantiate.
 */

@EqualsAndHashCode
class Note {

	@Getter
	private final Date dateCreated;

	private final ObjectProperty<Date> dateModified;

	private final StringProperty title;
	private final StringProperty content;

	@Getter(lazy = true)
	private final NotePreview preview = new NotePreview(this);

	Note(String title, String content) {
		this.title = new SimpleStringProperty(title);
		this.content = new SimpleStringProperty(content);

		dateCreated = new Date();
		dateModified = new SimpleObjectProperty<>((Date) dateCreated.clone());
	}

	private Note(String title, String content, Date dateCreated, Date dateModified) {
		this.title = new SimpleStringProperty(title);
		this.content = new SimpleStringProperty(content);

		this.dateCreated = dateCreated;
		this.dateModified = new SimpleObjectProperty<>(dateModified);
	}

	ReadOnlyObjectProperty<Date> dateModifiedProperty() {
		return dateModified;
	}

	StringProperty titleProperty() {
		return title;
	}

	String getTitle() {
		return title.get();
	}

	void setTitle(String title) {
		this.title.set(title);
		update();
	}

	StringProperty contentProperty() {
		return content;
	}

	String getContent() {
		return content.get();
	}

	void setContent(String content) {
		this.content.set(content);
		update();
	}

	private void update() {
		dateModified.set(new Date());
	}

	@Override
	public String toString() {
		return title.get() + "=" + content.get();
	}

	public void saveToFile(@Nonnull Path file) throws IOException {
		String str = String.join("\n",
				dateCreated.toString(),
				dateModified.get().toString(),
				title.get(),
				contentProperty().get()
		);

		Files.writeString(file, str);
	}

	public static void saveToFile(@Nonnull Note note, @Nonnull Path file) throws IOException {
		note.saveToFile(file);
	}

	public static Note loadFromFile(@Nonnull Path file) throws IOException, ParseException {
		List<String> lines = Files.readAllLines(file);

		DateFormat df = new SimpleDateFormat();

		String created = lines.get(0);
		Date dateCreated = df.parse(created);

		String modified = lines.get(1);
		Date dateModified = df.parse(modified);

		String title = lines.get(2);
		String content = String.join("\n", lines.subList(3, lines.size()));

		return new Note(title, content, dateCreated, dateModified);
	}

}
