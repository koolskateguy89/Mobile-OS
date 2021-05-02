package com.github.koolskateguy89.mobileos.view.utils;

import java.io.IOException;

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
		// TODO: match case (well ignore case)
		// TODO: make sure we find starting from caret pos

		String text = tic.getText();
		final String query = tf.getText();
		final int len = query.length();

		int pos = tic.getCaretPosition();
		if (lastIndex == 0)
			lastIndex = pos;

		// reset last index if making a new query
		if (!query.equals(lastQuery))
			lastIndex = pos;

		lastQuery = query;

		boolean down = this.down.isSelected();

		int index = down ? text.indexOf(query, lastIndex+1) : text.lastIndexOf(query, lastIndex-1);

		// If wrapping, try to find again from the start
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
