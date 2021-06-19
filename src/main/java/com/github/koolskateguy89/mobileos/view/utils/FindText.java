package com.github.koolskateguy89.mobileos.view.utils;

import java.io.IOException;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import com.github.koolskateguy89.mobileos.Main;
import org.controlsfx.control.textfield.CustomTextField;

import com.github.koolskateguy89.mobileos.utils.Utils;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXRadioButton;

// There is a problem in that though the text in the `tic` gets selected, it is 'in the background'
public class FindText extends Stage {

	private final TextInputControl tic;

	private final BooleanProperty replace = new SimpleBooleanProperty();

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

		if (owner == null) {
			Stage main = Main.getStage();
			// Show in middle of main stage
			this.setOnShown(windowEvent -> {
				double mainX = main.getX();
				double mainY = main.getY();

				double xOff = (main.getWidth() - getWidth()) / 2;
				double yOff = (main.getHeight() - getHeight()) / 2;

				setX(mainX + xOff);
				setY(mainY + yOff);
			});
		}

		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("utils/Find"));
		loader.setRoot(this);
		loader.setController(this);

		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setup(boolean replace) {
		String selected = tic.getSelectedText();
		if (!selected.isEmpty())
			tf.setText(selected);

		this.replace.set(replace);
	}

	public void find() {
		setup(false);
		this.show();
		this.requestFocus();
	}

	public void findAndReplace() {
		setup(true);
		this.show();
		this.requestFocus();
	}

	public void setText(String text) {
		tf.setText(text);
	}

	public String getText() {
		return tf.getText();
	}

	@FXML
	private GridPane findParent;

	@FXML
	private CustomTextField tf;

	@FXML
	private Label replaceLbl;
	@FXML
	private CustomTextField replaceTf;

	// properties/options
	@FXML
	private JFXCheckBox matchCase;

	@FXML
	private JFXCheckBox wrap;

	@FXML
	private JFXCheckBox regex;

	// direction
	@FXML
	private JFXRadioButton up;
	@FXML
	private JFXRadioButton down;

	@FXML
	private VBox replaceBox;

	@FXML
	private void initialize() {
		Utils.makeClearable(tf);
		Utils.makeClearable(replaceTf);

		// for some reason when pressing enter when tf is focused, the find (default) action isn't triggered
		tf.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ENTER)
				if (!tf.getText().isEmpty())
					find();
		});

		BooleanBinding isEmpty = tic.textProperty().isEmpty();

		// can't do any operations if there's nothing (that makes no sense)
		findParent.disableProperty().bind(isEmpty);

		replaceTf.visibleProperty().bind(replace);
		replaceLbl.visibleProperty().bind(replaceTf.visibleProperty());

		replaceBox.visibleProperty().bind(replace);
		replaceBox.disableProperty().bind(isEmpty);

		regex.visibleProperty().bind(replace);
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

	@FXML
	void replaceFirst() {
		String text = tic.getText();

		String target = tf.getText();
		String replace = replaceTf.getText();

		if (regex.isSelected())
			text = handleRegex(text, s -> s.replaceFirst(target, replace));
		else    // String replaceFirst without regex: https://stackoverflow.com/a/43436659
			text = text.replaceFirst(Pattern.quote(target), Matcher.quoteReplacement(replace));

		tic.setText(text);
	}

	@FXML
	void replaceAll() {
		String text = tic.getText();

		String target = tf.getText();
		String replace = replaceTf.getText();

		if (regex.isSelected())
			text = handleRegex(text, s -> s.replaceAll(target, replace));
		else
			text = text.replace(target, replace);

		tic.setText(text);
	}

	private String handleRegex(String text, Function<String, String> regexFunc) {
		try {
			return regexFunc.apply(text);
		} catch (PatternSyntaxException pse) {
			Alert alert = new Alert(AlertType.ERROR, "Bad regex pattern");
			alert.initOwner(this);
			alert.initStyle(this.getStyle());
			alert.showAndWait();
		}
		return text;
	}
}
