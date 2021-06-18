package com.github.koolskateguy89.mobileos.app.system.notes;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import com.github.koolskateguy89.mobileos.utils.Utils;

import lombok.Getter;

// Opening of Note is handles by NotesController (I hope)
class NotePreview extends AnchorPane {

	@Getter
	private final Note note;

	final NotesController controller;

	NotePreview(Note note, NotesController controller) {
		this.note = note;
		this.controller = controller;

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
	}

	@FXML
	private void open() {
		controller.openNote(this.note);
	}

}