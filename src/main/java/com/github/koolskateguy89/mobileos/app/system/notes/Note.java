package com.github.koolskateguy89.mobileos.app.system.notes;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

import lombok.Getter;
import lombok.Setter;

// TODO: I'm still debating whether to use some sort of ID for each note
@JsonAdapter(Note.Serializer.class)
class Note {

	// pattern of Date.toString()
	static final DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

	static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	@Getter
	private final Date dateCreated;

	private final ObjectProperty<Date> dateModified = new SimpleObjectProperty<>();

	private final StringProperty title = new SimpleStringProperty();
	private final StringProperty content = new SimpleStringProperty();

	@Getter(lazy = true)
	private final NotePreview preview = new NotePreview();

	@Getter @Setter
	private NoteEditor editor;

	Note() {
		this("", "");
	}

	Note(String title, String content) {
		setTitle(title);
		this.title.addListener((obs) -> update());
		setContent(content);
		this.content.addListener((obs) -> update());

		dateCreated = new Date();
		this.dateModified.set((Date) dateCreated.clone());
	}

	private Note(String title, String content, Date dateCreated, Date dateModified) {
		setTitle(title);
		this.title.addListener((obs) -> update());
		setContent(content);
		this.content.addListener((obs) -> update());

		this.dateCreated = dateCreated;
		this.dateModified.set(dateModified);
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
		return "Note{title=" + getTitle() +
					", content=" + getContent() +
					", dateCreated=\"" + getDateCreated() + '"' +
					", dateModified=\"" + getDateModified() + '"' +
				"}";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (o instanceof Note) {
			Note note = (Note) o;
			return this.getTitle().equals(note.getTitle()) &&
					this.getContent().equals(note.getContent()) &&
					this.getDateCreated().equals(note.getDateCreated());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getTitle(), getContent(), getDateCreated());
	}


	static class Serializer implements JsonSerializer<Note>, JsonDeserializer<Note> {

		@Override
		public JsonElement serialize(Note src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			obj.addProperty("title", src.getTitle());
			obj.addProperty("content", src.getContent());
			obj.addProperty("dateCreated", src.getDateCreated().toString());
			obj.addProperty("dateModified", src.getDateModified().toString());
			return obj;
		}

		@Override
		public Note deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			try {
				JsonObject obj = json.getAsJsonObject();

				String title = obj.get("title").getAsString();
				String content = obj.get("content").getAsString();

				Date dateCreated = df.parse(obj.get("dateCreated").getAsString());
				Date dateModified = df.parse(obj.get("dateModified").getAsString());

				return new Note(title, content, dateCreated, dateModified);
			} catch (Exception e) {
				throw new JsonParseException(e);
			}
		}
	}


	// TODO: finish context menu
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
