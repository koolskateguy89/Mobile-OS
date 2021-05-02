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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.prefs.Prefs;
import com.github.koolskateguy89.mobileos.view.utils.ExceptionDialog;
import com.github.koolskateguy89.mobileos.view.utils.FindText;
import com.github.koolskateguy89.mobileos.view.utils.FontSelector;

// TODO: decide what [close] will do and do newFile
public class NotepadController {

	// TODO: status bar (caret position, maybe encoding, LF/CRLF)
	// TODO: [edit] replace (Ctrl+H), go to [line] (Ctrl+G)
	// TODO: [file] open recent

	private static final FileChooser fc = new FileChooser();

	private static final String FAMILY_KEY  = "Notepad/family",
								BOLD_KEY    = "Notepad/bold",
								ITALICS_KEY = "Notepad/italics",
								SIZE_KEY    = "Notepad/size";

	private static void handleException(Exception e, String alertText) {
		ExceptionDialog ed = new ExceptionDialog(e, alertText);
		ed.showAndWaitCopy();
	}

	private final FontSelector fs = new FontSelector(Main.getStage());

	private final ObjectProperty<File> fileProperty = new SimpleObjectProperty<>();
	public ObjectProperty<File> fileProperty() {
		return fileProperty;
	}

	private boolean changed = false;
	private String previousText;

	// this is basically the opposite of quit()
	public void init() {
		if (finder == null)
			finder = new FindText(textArea);

		Font defaultFont = Font.getDefault();

		String family = Prefs.get(FAMILY_KEY, defaultFont.getFamily());
		boolean bold = Prefs.getBoolean(BOLD_KEY, false);
		boolean italics = Prefs.getBoolean(ITALICS_KEY, false);
		double size = Prefs.getDouble(SIZE_KEY, defaultFont.getSize());

		FontWeight weight = bold ? FontWeight.BOLD : FontWeight.NORMAL;
		FontPosture posture = italics ? FontPosture.ITALIC : FontPosture.REGULAR;

		Font font = Font.font(family, weight, posture, size);
		fs.setFont(font);
	}

	@FXML
	private TextArea textArea;

	@FXML
	private void initialize() {
		// TODO: changed
		/*textArea.textProperty().addListener(obs -> {
			StringProperty sp = (StringProperty) obs;
			String text = sp.get();

			changed = !text.equals(previousText);
			previousText = text;
			System.out.println("Changed: " + changed);
		});
		 */
		init();

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
		replace.disableProperty().bind(textArea.textProperty().isEmpty());
		goTo.disableProperty().bind(textArea.textProperty().isEmpty());

		// format
		textArea.wrapTextProperty().bind(wrapText.selectedProperty());


		// TODO
		recent.getItems().add(new MenuItem("Yo my slime"));
	}

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
		if (finder != null) {
			finder.close();
			finder = null;
		}


		Font font = textArea.getFont();

		String family = font.getFamily();
		String style = font.getStyle();
		boolean bold = style.contains("Bold");
		boolean italics = style.contains("Italics");
		double size = font.getSize();

		Prefs.put(FAMILY_KEY, family);
		Prefs.putBoolean(BOLD_KEY, bold);
		Prefs.putBoolean(ITALICS_KEY, italics);
		Prefs.putDouble(SIZE_KEY, size);
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
	private MenuItem goTo;

	@FXML
	public void goTo() {
		// TODO
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
