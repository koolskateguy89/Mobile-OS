package com.github.koolskateguy89.mobileos.app.system.notes;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

import com.github.koolskateguy89.mobileos.utils.Utils;
import com.jfoenix.controls.JFXTextField;

import lombok.SneakyThrows;

// TODO: everything (including FXML file)
class NoteEditor extends AnchorPane {

	static final NotesController controller = NotesController.instance;

	final Note note;

	static NoteEditor of(Note note) {
		NoteEditor editor = note.getEditor();

		if (editor == null) {
			editor = new NoteEditor(note);
			note.setEditor(editor);
		}

		return editor;
	}

	@SneakyThrows(IOException.class)
	private NoteEditor(Note note) {
		this.note = note;

		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("system/notes/NoteEditor"));
		loader.setRoot(this);
		loader.setController(this);
		loader.load();
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

		title.setText(note.getTitle());
		content.setText(note.getContent());

		note.titleProperty().bind(title.textProperty());
		note.contentProperty().bind(content.textProperty());
	}

	@FXML
	void back() {
		controller.loadUsualContent();
	}

}
