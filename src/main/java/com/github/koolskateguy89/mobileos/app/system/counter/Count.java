package com.github.koolskateguy89.mobileos.app.system.counter;

import java.io.IOException;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;

import com.jfoenix.controls.JFXSlider;

import lombok.SneakyThrows;

class Count extends Node {

	@SneakyThrows(IOException.class)
	public Count(String title, int min, int max) {
		this.title = new SimpleStringProperty(title);
		this.min = new SimpleIntegerProperty(min);
		this.max = new SimpleIntegerProperty(max);

		FXMLLoader loader = new FXMLLoader(Count.class.getResource("Count.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		loader.load();
	}

	final StringProperty title;

	final IntegerProperty min;
	final IntegerProperty max;

	IntegerProperty progress;

	@FXML
	private Label titleLabel;

	@FXML
	private JFXSlider slider;

	@FXML
	private Label label;

	@FXML
	private void initialize() {
		titleLabel.textProperty().bind(title);
		label.textProperty().bind(slider.valueProperty().asString("%d"));   // ints
		slider.minProperty().bind(min);
		slider.maxProperty().bind(max);
	}

	@FXML
	void minus() {

	}

	@FXML
	void plus() {

	}

	public StringProperty titleProperty() {
		return title;
	}

	public String getTitle() {
		return title.get();
	}

	public void setTitle(String title) {
		this.title.set(title);
	}

	public IntegerProperty minProperty() {
		return min;
	}

	public int getMin() {
		return min.get();
	}

	public void setMin(int min) {
		this.min.set(min);
	}

	public IntegerProperty maxProperty() {
		return max;
	}

	public int getMax() {
		return max.get();
	}

	public void setMax(int max) {
		this.max.set(max);
	}

}
