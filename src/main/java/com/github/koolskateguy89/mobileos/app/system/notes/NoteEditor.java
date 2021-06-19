package com.github.koolskateguy89.mobileos.app.system.notes;

import java.io.IOException;
import java.nio.file.Path;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

import com.github.koolskateguy89.mobileos.utils.Utils;
import com.jfoenix.controls.JFXTextField;

// TODO: everything
class NoteEditor extends AnchorPane {

	private final Note note;

	private final Path file;

	private final NotesController controller;

	NoteEditor(Note note, Path file, NotesController controller) {
		this.note = note;
		this.file = file;
		this.controller = controller;

		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("system/notes/NoteEditor"));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private JFXTextField title;

	@FXML
	private TextArea content;

	/*
	 * Maybe don't bind and instead set?
	 */

	@FXML
	private void initialize() {
		title.fontProperty().bind(controller.fontProperty);
		content.fontProperty().bind(controller.fontProperty);

		note.titleProperty().bind(title.textProperty());
		note.contentProperty().bind(content.textProperty());
	}

	void close() throws IOException {
		note.titleProperty().unbind();
		note.contentProperty().unbind();
		note.saveToFile(file);
	}

	@FXML
	void back() {
		//controller.goToMain() or something
	}

}
