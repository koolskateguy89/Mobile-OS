package com.github.koolskateguy89.mobileos.app.system.notepad;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Stack;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import lombok.Getter;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.utils.Utils;
import com.github.koolskateguy89.mobileos.view.utils.ExceptionDialog;
import com.github.koolskateguy89.mobileos.view.utils.FindText;
import com.github.koolskateguy89.mobileos.view.utils.FontSelector;
import com.google.common.base.Throwables;

// TODO: decide what [close] will do and do newFile
public class NotepadController {

	// TODO: status bar (caret position, maybe encoding, LF/CRLF)
	// TODO: [edit] find (Ctrl+F), replace (Ctrl+H), go to (Ctrl+G)
	// TODO: [format] font (settings - maybe save as Preferences)
	// TODO: [file] open recent

	private static final FileChooser fc = new FileChooser();

	private static void handleException(Exception e, String alertText) {
		ExceptionDialog ed = new ExceptionDialog(e, alertText);
		ed.showAndWaitCopy();
		if (true)
			return;

		Alert a = new Alert(AlertType.ERROR, alertText);

		ButtonType showException = new ButtonType("Show details");
		a.getButtonTypes().add(showException);


		if (a.showAndWait().orElse(null) == showException) {
			String stackTrace = Throwables.getStackTraceAsString(e);
			TextArea textArea = new TextArea(stackTrace);


			a = new Alert(AlertType.INFORMATION);
			//a.setHeaderText(null);
			//a.setGraphic(textArea);

			ButtonType copy = new ButtonType("Copy");
			a.getButtonTypes().add(copy);

			DialogPane dp = a.getDialogPane();
			dp.setContent(textArea);

			if (a.showAndWait().orElse(null) == copy)
				Utils.copyToClipboard(stackTrace);
		}
	}

	private final FontSelector fs = new FontSelector(Main.getStage());

	@Getter
	private final ObjectProperty<File> fileProperty = new SimpleObjectProperty<>();

	private boolean changed = false;
	private String previousText;


	@FXML
	private void initialize() {
		/*textArea.textProperty().addListener(obs -> {
			StringProperty sp = (StringProperty) obs;
			String text = sp.get();

			changed = !text.equals(previousText);
			previousText = text;
			System.out.println("Changed: " + changed);
		});
		 */

		// file
		//save.disableProperty().bind(fileProp.isNull());
		//saveAs.disableProperty().bind(textArea.textProperty().isEmpty());

		// edit
		undo.disableProperty().bind(textArea.undoableProperty().not());
		redo.disableProperty().bind(textArea.redoableProperty().not());
		//
		var selected = textArea.selectedTextProperty();
		cut.disableProperty().bind(selected.isEmpty());
		copy.disableProperty().bind(selected.isEmpty());
		//
		find.disableProperty().bind(textArea.textProperty().isEmpty());
		finder = new FindText(textArea);
		replace.disableProperty().bind(textArea.textProperty().isEmpty());

		// format
		textArea.wrapTextProperty().bind(wrapText.selectedProperty());



		// TODO
		recent.getItems().add(new MenuItem("Yo my slime"));
	}

	@FXML
	private TextArea textArea;

	// file

	// TODO
	@FXML
	public void newFile() {
		if (changed) {
			Alert a = new Alert(AlertType.CONFIRMATION, "Are you sure? [TODO]");

			// maybe do a ButtonType save

			ButtonType result = a.showAndWait().orElse(null);
			if (result == ButtonType.NO) {
				// save includes saveAs if file is null
				save();
			}
		}
		// TODO: check changed (ask to save if changed)

		fileProperty.set(null);
		textArea.clear();
	}

	@FXML
	public void open() {
		// TODO: check if current file isn't saved
		fc.setTitle("Open");
		File file = fc.showOpenDialog(Main.getStage());

		if (file != null)
			openFile(file);
	}

	private void openFile(File file) {
		fileProperty.set(file);

		try {
			textArea.setText(Files.readString(file.toPath()));
		} catch (IOException e) {
			handleException(e, file.getName() + " could not be opened");
		}
	}

	// I'm not sure which to use, hashset gives only unique which is good
	// for each file in here, I'll need a MenuItem whose onAction will be to open that file
	private Stack<File> recents;
	private LinkedHashSet<File> recentSet;
	//             'index'
	private HashMap<Integer, File> recentMap;

	@FXML
	private Menu recent;

	@FXML
	public void openRecent(ActionEvent event) {
		// TODO
		System.out.println("aaa");
		System.out.println(event.getSource());
		System.out.println(event.getTarget());
		System.out.println();
	}

	@FXML
	public void save() {
		File file = fileProperty.get();
		if (file == null) {
			saveAs();
		} else {
			try {
				Files.writeString(file.toPath(), textArea.getText());
			} catch (IOException e) {
				handleException(e, file.getName() + " could not be saved");
			}
		}
	}

	@FXML
	public void saveAs() {
		fc.setTitle("Save as");
		File file = fc.showSaveDialog(Main.getStage());
		if (file != null) {
			fileProperty.set(file);
			save();
		}
	}

	// edit

	@FXML
	public void quit() {
		// TODO
		// idrk what I'm gonna do here, maybe close and go home?
		finder.close();
	}

	@FXML
	private MenuItem undo;

	@FXML
	public void undo() {
		textArea.undo();
	}

	@FXML
	private MenuItem redo;

	@FXML
	public void redo() {
		textArea.redo();
	}

	@FXML
	private MenuItem cut;

	@FXML
	public void cut() {
		textArea.cut();
	}

	@FXML
	private MenuItem copy;

	@FXML
	public void copy() {
		textArea.copy();
	}

	@FXML
	public void paste() {
		textArea.paste();
	}

	@FXML
	private MenuItem find;

	private FindText finder;

	@FXML
	public void find() {
		finder.show();
		finder.requestFocus();
	}

	@FXML
	private MenuItem replace;

	@FXML
	public void replace() {
		System.out.println(textArea.getAnchor());
		System.out.println(textArea.getCaretPosition());
		System.out.println();
	}

	@FXML
	public void selectAll() {
		textArea.selectAll();
	}

	@FXML
	public void unselectAll() {
		textArea.deselect();
	}

	// format

	@FXML
	private CheckMenuItem wrapText;

	@FXML
	public void font() {
		fs.setFont(textArea.getFont());

		// select a font
		fs.showAndWait();

		textArea.setFont(fs.getFont());
	}

}
