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

import com.github.koolskateguy89.mobileos.utils.Utils;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXRadioButton;
import org.controlsfx.control.textfield.CustomTextField;

// There is a problem in that though the text in the `tic` gets selected, it is 'in the background'
public class FindText extends Stage {

	private final TextInputControl tic;

	private String lastQuery;

	private int lastIndex = 0;

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
		// TODO: make sure we find starting from caret pos

		String text = tic.getText();
		String query = tf.getText();
		int len = query.length();
		boolean newQuery = !query.equals(lastQuery);

		if (!matchCase.isSelected()) {
			// invariant locale to TRY and avoid irregularities of just .toLowerCase when using different languages
			text = text.toLowerCase(Locale.ENGLISH);
			query = query.toLowerCase(Locale.ENGLISH);
		}

		int caret = tic.getCaretPosition();
		// reset lastIndex if making a new query
		if (newQuery)
			lastIndex = caret;

		lastQuery = query;

		boolean down = this.down.isSelected();

		// TODO: if not new query, increment/decrement lastIndex by `len` for indexOf
		int index = down ? text.indexOf(query, lastIndex+1) : text.lastIndexOf(query, lastIndex-1);

		// If wrapping, try to find again from 'start'
		if (index == -1 && wrap.isSelected())
			index = down ? text.indexOf(query) : text.lastIndexOf(query);

		if (index == -1) {
			// TODO: alert saying not found
			String error = "Cannot find \"%s\"".formatted(query);

			Alert a = new Alert(AlertType.INFORMATION, error);
			a.setHeaderText(null);
			a.showAndWait();

		} else {
			lastIndex = index;
			tic.selectRange(index, index + len);
		}
	}
}
