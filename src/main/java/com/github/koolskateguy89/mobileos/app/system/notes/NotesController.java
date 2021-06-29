package com.github.koolskateguy89.mobileos.app.system.notes;

import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

import org.controlsfx.control.textfield.CustomTextField;

import com.github.koolskateguy89.mobileos.utils.Utils;

// TODO: Settings: Font,
public class NotesController {

	static NotesController instance;
	public NotesController() {
		instance = this;
	}

	final SimpleObjectProperty<Note> currentNote = new SimpleObjectProperty<>();

	final ObservableList<Note> notes = FXCollections.observableArrayList();

	final ObjectProperty<Font> fontProperty = new SimpleObjectProperty<>(Font.getDefault());

	void openNote(Note note) {
		currentNote.set(note);
		// TODO
		System.out.println("Open: " + note.getTitle());
		NoteEditor editor; // = new NoteEditor(note, Path.of("Test.note"), this);
	}
	
	@FXML
	private AnchorPane root;
	private List<Node> usualContent;

	@FXML
	private CustomTextField searchBar;

	@FXML
	private VBox vbox;

	@FXML
	private void initialize() {
		// not sure this works how I want it to
		usualContent = List.copyOf(root.getChildren());
		//usualContent = new ArrayList<>(root.getChildren());

		Utils.makeClearable(searchBar);
	}

	void loadUsualContent() {
		root.getChildren().setAll(usualContent);
	}

	@FXML
	void search() {
		String query = searchBar.getText();

		List<Note> result = notes.stream().filter(note -> {
			String title = note.getTitle();
			String content = note.getContent();
			return Utils.containsIgnoreCase(title, query) || Utils.containsIgnoreCase(content, query);
		}).collect(Collectors.toList());
		System.out.println(result);

		// TODO: display only search results
	}

	@FXML
	void newNote() {
		Note note = new Note("TestTitle" + notes.size(), "TestContent");
		notes.add(note);

		// add lines between NotePreviews, imitating iOS Notes
		if (!vbox.getChildren().isEmpty()) {
			Line line = new Line(0, 0, vbox.getPrefWidth(), 0);
			line.setStroke(Color.WHITE);
			vbox.getChildren().add(line);
		}

		vbox.getChildren().add(note.getPreview());
	}

}
