package com.github.koolskateguy89.mobileos.app.system.notes;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

import org.controlsfx.control.textfield.CustomTextField;

import com.github.koolskateguy89.mobileos.app.system.notes.Note.NotePreview;
import com.github.koolskateguy89.mobileos.utils.Utils;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jfoenix.controls.JFXButton;

import lombok.SneakyThrows;

// TODO: Settings: Font,
// TODO: sort by Title/Content/DateCreated/DateModified
public class NotesController {

	static NotesController instance;
	public NotesController() {
		instance = this;
	}

	final SimpleObjectProperty<Note> currentNote = new SimpleObjectProperty<>();

	static final Path notesPath = Notes.dir.resolve("notes.json");
	final ObservableList<Note> notes = FXCollections.observableArrayList();

	final ObjectProperty<Font> fontProperty = new SimpleObjectProperty<>() {{
		InputStream is = NotesController.class.getClassLoader().getResourceAsStream("DINPro Bold.otf");
		Font font = Font.loadFont(is, 12);

		try {
			if (is != null)
				is.close();
		} catch (IOException ignored) {}

		set(font != null ? font : Font.getDefault());
	}};

	void goBack() {
		writeNotesToFile();
		loadUsualContent();
		currentNote.set(null);
	}

	void onClose() {
		writeNotesToFile();
	}

	@SneakyThrows(IOException.class)
	private void writeNotesToFile() {
		JsonArray notesJson = new JsonArray();
		for (Note note : notes) {
			notesJson.add(note.toJson());
		}

		try (BufferedWriter bw = Files.newBufferedWriter(notesPath)) {
			new GsonBuilder().setPrettyPrinting().create().toJson(notesJson, bw);
		}
	}

	void openNote(Note note) {
		currentNote.set(note);
		openEditor(NoteEditor.of(note));
	}

	@FXML
	private AnchorPane root;
	private List<Node> usualContent;

	@FXML
	private JFXButton resetSearch;
	private final BooleanProperty isSearch = new SimpleBooleanProperty(false);
	private List<Node> previousChildren;

	@FXML
	private CustomTextField searchBar;

	@FXML
	private VBox vbox;

	@FXML
	private void initialize() {
		usualContent = List.copyOf(root.getChildren());

		ObservableList<Node> vboxChildren = vbox.getChildren();
		notes.addListener((ListChangeListener<Note>) change -> {
			change.next();
			if (change.wasAdded()) {
				change.getAddedSubList().forEach(note -> {
					// add lines between NotePreviews, imitating iOS Notes
					if (!vboxChildren.isEmpty()) {
						Line line = new Line(0, 0, vbox.getPrefWidth(), 0);
						line.setStroke(Color.WHITE);
						vboxChildren.add(line);
					}
					vboxChildren.add(note.getPreview());
				});
			} else if (change.wasRemoved()) {
				// remove the preview & the relevant line
				change.getRemoved().forEach(note -> {
					int idx = vboxChildren.indexOf(note.getPreview());
					vboxChildren.remove(idx);
					int size = vboxChildren.size();
					if (size != 0) {
						if (idx == 0) {
							// remove the line that was 'in front'
							if (size != 1)
								vboxChildren.remove(0);
						} else {
							// remove the line that was 'behind'
							vboxChildren.remove(idx - 1);
						}
					}
				});
			}
		});

		resetSearch.visibleProperty().bind(isSearch);

		Utils.makeClearable(searchBar);

		try {
			JsonArray notesJson = JsonParser.parseString(Files.readString(notesPath)).getAsJsonArray();
			for (JsonElement elem : notesJson) {
				Note note = Note.fromJson(elem.getAsJsonObject());
				setupNotePreviewContextMenu(note);
				notes.add(note);
			}
		} catch (Exception ignored) {
		}
	}

	void setupNotePreviewContextMenu(Note note) {
		// TODO: notePreview contextMenu
		ContextMenu cm = new ContextMenu();

		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction((ActionEvent event) -> notes.remove(note));
		cm.getItems().add(delete);

		note.getPreview().addEventFilter(MouseEvent.ANY, (MouseEvent event) -> {
			if (event.isPopupTrigger()) {
				cm.show(note.getPreview(), event.getScreenX(), event.getScreenY());
				event.consume();
			}
		});
	}

	void openEditor(NoteEditor editor) {
		root.getChildren().setAll(editor);
	}

	void loadUsualContent() {
		root.getChildren().setAll(usualContent);
	}

	@FXML
	void search() {
		// TODO: SearchBar chrome bookmark

		isSearch.set(true);

		final String query = searchBar.getText();

		previousChildren = List.copyOf(vbox.getChildren());

		List<NotePreview> result = notes.stream().filter(note -> {
			String title = note.getTitle();
			String content = note.getContent();
			return Utils.containsIgnoreCase(title, query) || Utils.containsIgnoreCase(content, query);
		}).map(Note::getPreview).collect(Collectors.toList());

		vbox.getChildren().setAll(result);
	}

	@FXML
	void resetSearch() {
		isSearch.set(false);
		vbox.getChildren().setAll(previousChildren);
	}

	@FXML
	void newNote() {
		Note note = new Note();
		setupNotePreviewContextMenu(note);
		notes.add(note);
		openNote(note);
	}

}
