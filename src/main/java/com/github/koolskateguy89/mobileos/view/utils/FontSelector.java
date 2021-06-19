package com.github.koolskateguy89.mobileos.view.utils;

import java.io.IOException;

import javax.annotation.Nonnull;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import org.controlsfx.control.SearchableComboBox;

import com.github.koolskateguy89.mobileos.utils.Utils;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSlider;

import lombok.Getter;

public class FontSelector extends Stage {

	public FontSelector(Window owner) {
		initStyle(StageStyle.UTILITY);
		initOwner(owner);

		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("utils/FontSelector"));
		loader.setRoot(this);
		loader.setController(this);

		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Getter
	private final ObjectProperty<Font> fontProperty = new SimpleObjectProperty<>();

	@FXML
	private SearchableComboBox<String> families;

	@FXML
	private JFXCheckBox bold;

	@FXML
	private JFXCheckBox italics;

	@FXML
	private JFXComboBox<String> sizes;

	@FXML
	private JFXSlider sizeSlider;

	@FXML
	private Label preview;

	@FXML
	private void initialize() {
		// ESC closes this
		getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), this::close);

		families.getItems().addAll(Font.getFamilies());

		sizes.getItems().addAll("8", "10", "12", "14", "18", "24", "36", "48");

		// update GUI elements when font is updated
		fontProperty.addListener((obs, oldFont, newFont) -> {
			String family = newFont.getFamily();
			String style = newFont.getStyle();
			double size = newFont.getSize();

			boolean isBold = style.contains("Bold");
			boolean isItalics = style.contains("Italic");

			families.setValue(family);
			bold.setSelected(isBold);
			italics.setSelected(isItalics);

			if (size == Math.floor(size)) { // size is int
				sizes.setValue(Integer.toString((int) size));
			} else {
				sizes.setValue(Double.toString(size));
			}
		});
		// initial value is system default font
		fontProperty.set(Font.getDefault());
		sizeSlider.setValue(Double.parseDouble(sizes.getValue()));


		// update slider when size combo is updated
		sizes.valueProperty().addListener((obs, oldValue, newValue) -> {
			try {
				double newSize = Double.parseDouble(newValue);
				if (newSize != sizeSlider.getValue()) {
					sizeSlider.setValue(newSize);
				}
			} catch (NumberFormatException nfe) {
				((ObjectProperty<String>) obs).setValue(oldValue);
			}
		});

		// because the value shown by JFXSlider is rounded, is makes sense to round the actual value
		sizeSlider.valueProperty().addListener((obs, oldValue, newValue) ->
				sizeSlider.setValue(Math.round(newValue.doubleValue())));

		sizeSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
			if (!isChanging) {
				long rounded = (long) sizeSlider.getValue();
				sizes.setValue(Long.toString(rounded));
				updateFont();
			}
		});

		preview.fontProperty().bind(fontProperty);
	}

	@FXML
	void updateFont() {
		String family = families.getValue();
		double size = Double.parseDouble(sizes.getValue());

		FontWeight weight = bold.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL;
		FontPosture posture = italics.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR;

		setFont(Font.font(family, weight, posture, size));
	}

	public Font getFont() {
		return fontProperty.get();
	}

	public void setFont(@Nonnull Font font) {
		fontProperty.set(font);
	}

}
