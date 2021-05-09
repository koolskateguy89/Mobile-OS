package com.github.koolskateguy89.mobileos.app.system.notepad;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.prefs.Preferences;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.utils.ObservableLimitedList;
import com.github.koolskateguy89.mobileos.view.utils.ExceptionDialog;
import com.github.koolskateguy89.mobileos.view.utils.FindText;
import com.github.koolskateguy89.mobileos.view.utils.FontSelector;

import lombok.Setter;

public class NotepadController {

	// TODO: {maybe} status bar (caret position, maybe encoding, LF/CRLF)
	// TODO: [file] Open Recents: separator + "Clear Menu" (at bottom) & save in prefs


	// FIXME: Ctrl+H isn't working as the accelerator for replace, instead is seems to be doing 'Delete' instead

	// TODO: line numbers on left of text box

	@Setter
	static Preferences prefs;

	private static final String FAMILY_KEY  = "font_family",
								BOLD_KEY    = "font_bold",
								ITALICS_KEY = "font_italics",
								SIZE_KEY    = "font_size";

	// maybe use a separate node for recents then a separate prop for each recent - like how Scenebuilder does it
	// but it does that because it stores other info about it, which this isn't because I cba
	private static final String RECENTS_KEY = "recents";

	private static void handleException(Throwable e, String alertText) {
		ExceptionDialog ed = new ExceptionDialog(e, alertText);
		ed.showAndWaitCopy();
	}

	private final FileChooser fc = new FileChooser();
	{
		// TODO: better way to do this
		fc.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
	}

	private final FontSelector fs = new FontSelector(Main.getStage());

	final ObjectProperty<File> fileProperty = new SimpleObjectProperty<>();

	// max 8 recents
	private final int maxRecentsSize = 8;
	private final ObservableLimitedList<File> recents = new ObservableLimitedList<>(maxRecentsSize, true);

	// very simple: once textArea text changes, this is true
	final BooleanProperty changed = new SimpleBooleanProperty();

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

	// returns if to cancel
	private boolean cancelDueToUnsavedText() {
		if (changed.get()) {
			Alert save = new Alert(Alert.AlertType.WARNING, "Do you want to save current file?",
					ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

			ButtonType result = save.showAndWait().orElse(ButtonType.CANCEL);

			if (result == ButtonType.CANCEL)
				return true;

			if (result == ButtonType.YES)
				save();
		}
		return false;
	}

	private void openFile(File file) {
		recents.remove(file);
		recents.add(file);

		fileProperty.set(file);

		// Next time opening FileChooser, show same folder
		fc.setInitialDirectory(file.getParentFile());

		try {
			textArea.setText(Files.readString(file.toPath()));
			changed.set(false);
		} catch (IOException e) {
			handleException(e, file.getName() + " could not be opened");
		}
	}

	@FXML
	private TextArea lineThingy;

	@FXML
	private TextArea textArea;

	@FXML
	private void initialize() {
		// lines
		lineThingy.fontProperty().bind(textArea.fontProperty());

		// lineThingy.scrollTopProperty() can't be bound because it gets set after this
		textArea.scrollTopProperty().addListener((obs, oldVal, newVal) -> {
			lineThingy.setScrollTop(newVal.doubleValue());
		});

		// changed & lines
		textArea.textProperty().addListener(obs -> {
			// Not bothered to implement a better way to detect change
			changed.set(true);

			// re-calculate the lines
			int lines = (int) textArea.getText().lines().count();

			if (lines == 0) {
				lineThingy.setText("1");
				return;
			}

			StringBuilder sb = new StringBuilder(lines * 2 - 1);
			sb.append(1);
			for (int i = 2; i <= lines; i++) {
				sb.append('\n').append(i);
			}
			lineThingy.setText(sb.toString());
		});

		// I could be more efficient and not regenerate the entire recent menu every time but cba
		ListChangeListener<File> recentsListener = change -> {
			int len = recents.size();
			List<MenuItem> result = new ArrayList<>(len);

			if (len == 0) {
				recent.getItems().clear();
				return;
			}

			// iterate backwards through recents - so most recent file first
			ListIterator<File> it = recents.listIterator(len);
			while (it.hasPrevious()) {
				File recent = it.previous();
				// maybe to absolute path?
				MenuItem menu = new MenuItem(recent.getName());
				menu.setOnAction(event -> {
					if (!cancelDueToUnsavedText())
						openFile(recent);
				});
				result.add(menu);
			}

			recent.getItems().setAll(result);
		};
		recents.addListener(recentsListener);

		// file
		//save.disableProperty().bind(fileProp.isNull());
		//saveAs.disableProperty().bind(textArea.textProperty().isEmpty());
		revertToSaved.disableProperty().bind(fileProperty.isNull().and(changed));

		// edit
		undo.disableProperty().bind(textArea.undoableProperty().not());
		redo.disableProperty().bind(textArea.redoableProperty().not());
		//
		BooleanBinding selectedIsEmpty = textArea.selectedTextProperty().isEmpty();
		cut.disableProperty().bind(selectedIsEmpty);
		copy.disableProperty().bind(selectedIsEmpty);
		//
		BooleanBinding isEmpty = textArea.textProperty().isEmpty();
		find.disableProperty().bind(isEmpty);
		replace.disableProperty().bind(isEmpty);
		goTo.disableProperty().bind(isEmpty);

		// format
		textArea.wrapTextProperty().bind(wrapText.selectedProperty());
	}

	// file

	@FXML
	void newFile() {
		if (cancelDueToUnsavedText())
			return;

		fileProperty.set(null);
		textArea.clear();
	}

	@FXML
	void open() {
		if (cancelDueToUnsavedText())
			return;

		fc.setTitle("Open");
		File file = fc.showOpenDialog(Main.getStage());

		if (file != null)
			openFile(file);
	}

	@FXML
	private Menu recent;

	@FXML
	void save() {
		File file = fileProperty.get();
		if (file == null) {
			saveAs();
		} else {
			try {
				Files.writeString(file.toPath(), textArea.getText());
				changed.set(false);
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
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initStyle(StageStyle.UTILITY);
		alert.initOwner(Main.getStage());
		alert.setTitle("Go To");

		// TODO: add int-only filter
		TextField textField = new TextField("1");

		DialogPane dp = alert.getDialogPane();
		dp.setHeaderText("Line number:");
		dp.setContent(textField);

		textField.requestFocus();
		alert.showAndWait();

		int line;
		try {
			line = Integer.parseInt(textField.getText());
		} catch (NumberFormatException e) {
			// TODO: remove try/catch once int-only filter is on textField
			return;
		}

		try {
			// lines before the goTo line
			var linesBefore = textArea.getText().lines().limit(line - 1);

			int lenBefore = linesBefore.mapToInt(String::length).sum();

			lenBefore += line - 1; // account for new lines (bit weird - TODO: better this comment)
			textArea.selectPositionCaret(lenBefore);

			// don't select any text
			textArea.deselect();
		} catch (IndexOutOfBoundsException e) {
			new Alert(AlertType.ERROR, line + " is out of bounds").showAndWait();
		}
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
