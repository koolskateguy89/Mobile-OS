package com.github.koolskateguy89.mobileos.app.system.notepad;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Stack;
import java.util.prefs.Preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import com.github.koolskateguy89.mobileos.view.utils.ExceptionDialog;
import com.github.koolskateguy89.mobileos.view.utils.FindText;
import com.github.koolskateguy89.mobileos.view.utils.FontSelector;

import lombok.Setter;

public class NotepadController {

	// TODO: {maybe} status bar (caret position, maybe encoding, LF/CRLF)
	// TODO: [edit] go to [line] (Ctrl+G)
	// TODO: [file] open recent

	// FIXME: Ctrl+H isn't working as the accelerator for replace, instead is seems to be doing 'Delete' instead

	@Setter
	static Preferences prefs;
	// maybe have a separate node for font? (it's not necessary but i dont like current impl.)

	private static final String FAMILY_KEY  = "font_family",
								BOLD_KEY    = "font_bold",
								ITALICS_KEY = "font_italics",
								SIZE_KEY    = "font_size";

	private static final FileChooser fc = new FileChooser();

	private static void handleException(Throwable e) {
		handleException(e, null);
	}

	private static void handleException(Throwable e, String alertText) {
		ExceptionDialog ed = new ExceptionDialog(e, alertText);
		ed.showAndWaitCopy();
	}

	private final FontSelector fs = new FontSelector(Main.getStage());

	private final ObjectProperty<File> fileProperty = new SimpleObjectProperty<>();
	ObjectProperty<File> fileProperty() {
		return fileProperty;
	}

	// TODO: text changed (maybe just simply if text is typed, set to true)
	private final BooleanProperty changed = new SimpleBooleanProperty();
	private String previousText;

	// this is basically the opposite of quit()
	void init() {
		if (finder == null)
			finder = new FindText(textArea);

		Font defaultFont = Font.getDefault();

		String family = prefs.get(FAMILY_KEY, defaultFont.getFamily());
		boolean bold = prefs.getBoolean(BOLD_KEY, false);
		boolean italics = prefs.getBoolean(ITALICS_KEY, false);
		double size = prefs.getDouble(SIZE_KEY, defaultFont.getSize());

		FontWeight weight = bold ? FontWeight.BOLD : FontWeight.NORMAL;
		FontPosture posture = italics ? FontPosture.ITALIC : FontPosture.REGULAR;

		Font font = Font.font(family, weight, posture, size);
		textArea.setFont(font);

		boolean wrap = prefs.getBoolean("wrap_text", false);
		wrapText.setSelected(wrap);
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

		// file
		//save.disableProperty().bind(fileProp.isNull());
		//saveAs.disableProperty().bind(textArea.textProperty().isEmpty());
		revertToSaved.disableProperty().bind(fileProperty().isNull().and(changed));

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


		// TODO: recents
		recent.getItems().add(new MenuItem("Yo my slime"));
	}

	// file

	@FXML
	void newFile() {
		if (changed.get()) {
			// TODO: ask to save because changed alert
			Alert a = new Alert(AlertType.CONFIRMATION, "Are you sure? [TODO]");

			// maybe do a ButtonType save

			ButtonType result = a.showAndWait().orElse(null);
			if (result == ButtonType.NO) {
				// save includes saveAs if file is null
				save();
			}
		}

		fileProperty.set(null);
		textArea.clear();
	}

	@FXML
	void open() {
		if (changed.get()) {
			// TODO: alert about current file/text isn't saved
		}

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
	void openRecent(ActionEvent event) {
		// TODO
		System.out.println("aaa");
		System.out.println(event.getSource());
		System.out.println(event.getTarget());
		System.out.println();
	}

	@FXML
	void save() {
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
	void saveAs() {
		fc.setTitle("Save as");
		File file = fc.showSaveDialog(Main.getStage());
		if (file != null) {
			fileProperty.set(file);
			save();
		}
	}

	@FXML
	private MenuItem revertToSaved;

	@FXML
	void revertToSaved() throws IOException {
		String saved = Files.readString(fileProperty.get().toPath());
		textArea.setText(saved);
		changed.set(false);
	}

	// edit

	@FXML
	void quit() {
		if (finder != null) {
			finder.close();
			finder = null;
		}

		Font font = textArea.getFont();

		String family = font.getFamily();
		String style = font.getStyle();
		boolean bold = style.contains("Bold");
		boolean italics = style.contains("Italic");
		double size = font.getSize();

		prefs.put(FAMILY_KEY, family);
		prefs.putBoolean(BOLD_KEY, bold);
		prefs.putBoolean(ITALICS_KEY, italics);
		prefs.putDouble(SIZE_KEY, size);

		prefs.putBoolean("wrap_text", wrapText.isSelected());
	}

	@FXML
	private MenuItem undo;

	@FXML
	void undo() {
		textArea.undo();
	}

	@FXML
	private MenuItem redo;

	@FXML
	void redo() {
		textArea.redo();
	}

	@FXML
	private MenuItem cut;

	@FXML
	void cut() {
		textArea.cut();
	}

	@FXML
	private MenuItem copy;

	@FXML
	void copy() {
		textArea.copy();
	}

	@FXML
	void paste() {
		textArea.paste();
	}

	private FindText finder;

	@FXML
	private MenuItem find;

	@FXML
	void find() {
		finder.find();
	}

	@FXML
	private MenuItem replace;

	@FXML
	void replace() {
		finder.findAndReplace();
	}

	@FXML
	private MenuItem goTo;

	@FXML
	void goTo() {
		// TODO
	}

	@FXML
	void selectAll() {
		textArea.selectAll();
	}

	@FXML
	void unselectAll() {
		textArea.deselect();
	}

	// format

	@FXML
	private CheckMenuItem wrapText;

	@FXML
	void font() {
		fs.setFont(textArea.getFont());

		// select a font
		fs.showAndWait();

		textArea.setFont(fs.getFont());
	}

}
