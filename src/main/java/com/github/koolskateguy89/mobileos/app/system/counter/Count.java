package com.github.koolskateguy89.mobileos.app.system.counter;

import java.io.IOException;
import java.lang.reflect.Type;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;
import com.jfoenix.controls.JFXSlider;

import lombok.SneakyThrows;

@JsonAdapter(Count.Serializer.class)
class Count extends Pane {

	public Count(String title, int min, int max) {
		this(title, min, max, 0);
	}

	@SneakyThrows(IOException.class)
	private Count(String title, int min, int max, int progress) {
		this.title = new SimpleStringProperty(title);
		this.min = new SimpleIntegerProperty(min);
		this.max = new SimpleIntegerProperty(max);
		this.progress = new SimpleIntegerProperty(progress);

		FXMLLoader loader = new FXMLLoader(Count.class.getResource("Count.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		loader.load();
	}

	final StringProperty title;

	final IntegerProperty min;
	final IntegerProperty max;

	final IntegerProperty progress;

	@FXML
	private Label titleLabel;

	@FXML
	private JFXSlider slider;

	@FXML
	private Label label;

	@FXML
	private void initialize() {
		titleLabel.textProperty().bind(title);
		label.textProperty().bind(slider.valueProperty().asString("%.0f"));   // ints
		slider.minProperty().bind(min);
		slider.maxProperty().bind(max);
	}

	@FXML
	void minus() {

	}

	@FXML
	void plus() {

	}

	//<editor-fold desc="Getters & Setters">
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

	public IntegerProperty progressProperty() {
		return progress;
	}

	public int getProgress() {
		return progress.get();
	}

	public void setProgress(int progress) {
		this.progress.set(progress);
	}
	//</editor-fold>

	static class Serializer implements JsonSerializer<Count>, JsonDeserializer<Count> {

		@Override
		public JsonElement serialize(Count src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();

			obj.addProperty("title", src.getTitle());
			obj.addProperty("min", src.getMin());
			obj.addProperty("max", src.getMax());
			obj.addProperty("progress", src.getProgress());

			return obj;
		}

		@Override
		public Count deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();

			String title = obj.get("title").getAsString();
			int min = obj.get("min").getAsInt();
			int max = obj.get("max").getAsInt();
			int progress = obj.get("progress").getAsInt();

			return new Count(title, min, max, progress);
		}
	}

}
