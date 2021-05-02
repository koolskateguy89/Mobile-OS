package com.github.koolskateguy89.mobileos.view.utils;

import java.io.IOException;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import org.controlsfx.control.textfield.CustomTextField;

import com.github.koolskateguy89.mobileos.utils.Utils;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXRadioButton;

// There is a problem in that though the text in the `tic` gets selected, it is 'in the background'
// TODO: FindReplace
public class FindText extends Stage {

	private final TextInputControl tic;

	private String lastQuery;

	private int lastIndex = 0;
	private int lastCaret = -1;

	public FindText(TextInputControl tic) {
		this(tic, null);
	}

	public FindText(TextInputControl tic, Window owner) {
		this.tic = tic;
		initStyle(StageStyle.UTILITY);
		initOwner(owner);

		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("utils/text/Find"));
		loader.setRoot(this);
		loader.setController(this);

		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private CustomTextField tf;

	@FXML
	private JFXCheckBox matchCase;

	@FXML
	private JFXCheckBox wrap;

	@FXML
	private JFXRadioButton up;
	@FXML
	private JFXRadioButton down;

	@FXML
	private void initialize() {
		Utils.makeClearable(tf);
		tf.setText(tic.getSelectedText());
	}

	@FXML
	void findNext() {
		String text = tic.getText();
		String query = tf.getText();
		final int len = query.length();
		final int caret = tic.getCaretPosition();
		boolean newQuery = !query.equals(lastQuery);

		// reset lastIndex if making a new query
		if (newQuery) {
			lastIndex = caret;
		} else {
			// attempts to check if it actually is a new query
			newQuery = caret != lastCaret; // im not too sure about this logic yet
			lastCaret = caret;
		}

		if (!matchCase.isSelected()) {
			// invariant locale to TRY and avoid irregularities of just .toLowerCase when using different languages
			text = text.toLowerCase(Locale.ENGLISH);
			query = query.toLowerCase(Locale.ENGLISH);
		}

		// should this be called before matchCase is called? it should right?
		lastQuery = query;

		boolean down = this.down.isSelected();

		// if not making a new query, basically don't include letters from previous results
		int offset = newQuery ? 1 : len;

		int index = down ? text.indexOf(query, lastIndex + offset) :
						   text.lastIndexOf(query, lastIndex - offset);

		// If wrapping, try to find again from 'start'
		if (index == -1 && wrap.isSelected())
			index = down ? text.indexOf(query) : text.lastIndexOf(query);

		if (index == -1) {
			String error = "Cannot find \"%s\"".formatted(query);
			Alert alert = new Alert(AlertType.INFORMATION, error);
			alert.setHeaderText(null);
			alert.showAndWait();
		} else {
			lastIndex = index;
			tic.selectRange(index, index + len);
		}
	}
}
