package com.github.koolskateguy89.mobileos.app.system.notes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import org.controlsfx.control.textfield.CustomTextField;

import com.github.koolskateguy89.mobileos.utils.Utils;
import com.jfoenix.controls.JFXButton;

public class NotesController {

	List<Note> notes = new ArrayList<>();

	void openNote(Note note) {
		// TODO
		System.out.println("Open: " + note.getTitle());
	}

	@FXML
	private CustomTextField searchBar;

	@FXML
	private JFXButton newNote;

	@FXML
	private VBox vbox;

	@FXML
	private void initialize() {
		Utils.makeClearable(searchBar);
	}

	@FXML
	private void search() {
		String query = searchBar.getText();

		List<Note> result = notes.stream().filter(note -> {
			String title = note.getTitle();
			String content = note.getContent();
			return Utils.containsIgnoreCase(title, query) || Utils.containsIgnoreCase(content, query);
		}).collect(Collectors.toList());

		// TODO: diplay only results
	}

	@FXML
	private void newNote() {
		Note note = new Note("TestTitle" + notes.size(), "TextContent");
		notes.add(note);
		vbox.getChildren().add(new NotePreview(note, this));
	}

}
