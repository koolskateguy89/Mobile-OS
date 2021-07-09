package com.github.koolskateguy89.mobileos.app.system.notes;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;

import com.github.koolskateguy89.mobileos.utils.Utils;
import com.google.gson.JsonObject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
class Note {

	// pattern of Date.toString()
	static final DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

	@Getter
	private final Date dateCreated;

	private final ObjectProperty<Date> dateModified;

	private final StringProperty title;
	private final StringProperty content;

	@Getter(lazy = true)
	private final NotePreview preview = new NotePreview();

	@Getter @Setter
	private NoteEditor editor;

	Note() {
		this("", "");
	}

	Note(String title, String content) {
		this.title = new SimpleStringProperty(title);
		this.title.addListener((obs) -> update());
		this.content = new SimpleStringProperty(content);
		this.content.addListener((obs) -> update());

		dateCreated = new Date();
		dateModified = new SimpleObjectProperty<>((Date) dateCreated.clone());

	}

	private Note(String title, String content, Date dateCreated, Date dateModified) {
		this.title = new SimpleStringProperty(title);
		this.title.addListener((obs) -> update());
		this.content = new SimpleStringProperty(content);
		this.content.addListener((obs) -> update());

		this.dateCreated = dateCreated;
		this.dateModified = new SimpleObjectProperty<>(dateModified);
	}

	private void update() {
		dateModified.set(new Date());
	}

	ReadOnlyObjectProperty<Date> dateModifiedProperty() {
		return dateModified;
	}

	Date getDateModified() {
		return dateModified.get();
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

	@Override
	public String toString() {
		return "Note[title=" + getTitle() + ", content=" + getContent() + "]";
	}

	public JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("title", getTitle());
		obj.addProperty("content", getContent());
		obj.addProperty("dateCreated", getDateCreated().toString());
		obj.addProperty("dateModified", getDateModified().toString());
		return obj;
	}

	public static Note fromJson(JsonObject obj) throws ParseException {
		Date dateCreated = df.parse(obj.get("dateCreated").getAsString());

		Date dateModified = df.parse(obj.get("dateModified").getAsString());

		String title = obj.get("title").getAsString();

		String content = obj.get("content").getAsString();

		return new Note(title, content, dateCreated, dateModified);
	}


	// TODO: context menu
	class NotePreview extends AnchorPane {

		@Getter
		final Note note = Note.this;

		NotePreview() {
			FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("system/notes/NotePreview"));
			loader.setRoot(this);
			loader.setController(this);
			try {
				loader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@FXML
		private Label title;

		@FXML
		private Label dateModified;

		@FXML
		private Label preview;

		@FXML
		private void initialize() {
			title.textProperty().bind(note.titleProperty());
			dateModified.textProperty().bind(note.dateModifiedProperty().asString());
			preview.textProperty().bind(note.contentProperty());

			handleFont(this);
		}

		private void handleFont(Node node) {
			if (node instanceof Parent) {
				Parent p = (Parent) node;
				p.getChildrenUnmodifiable().forEach(this::handleFont);
			}
			if (node instanceof Labeled) {
				Labeled l = (Labeled) node;
				if (!l.fontProperty().isBound()) {
					final double size = l.getFont().getSize();

					ObjectBinding<Font> binding = Bindings.createObjectBinding(() -> {
						String family = NotesController.instance.fontProperty.get().getFamily();
						return Font.font(family, size);
					}, NotesController.instance.fontProperty);
					l.fontProperty().bind(binding);
				}
			}
		}

		@FXML
		void open(MouseEvent event) {
			NotesController.instance.openNote(this.note);
		}

	}

}
