package com.github.koolskateguy89.mobileos.app.system.counter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

import lombok.SneakyThrows;

@JsonAdapter(Subject.Serializer.class)
class Subject extends TitledPane {

	final StringProperty titleProperty = new SimpleStringProperty();

	final ObservableList<Count> counts = FXCollections.observableArrayList();

	Subject(String title) {
		this(title, Collections.emptyList());
	}

	@SneakyThrows(IOException.class)
	private Subject(String title, List<Count> counts) {
		titleProperty.set(title);
		this.textProperty().bind(titleProperty);
		this.counts.addAll(counts);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("Subject.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		loader.load();
	}

	@FXML
	private VBox countWrapper;

	@FXML
	private void initialize() {
		Bindings.bindContent(countWrapper.getChildren(), this.counts);
	}

	@FXML
	void edit() {
		// TODO: ask if want to remove
		String newTitle = Utils.ask("New title:");
		if (!Utils.isNullOrBlank(newTitle))
			setTitle(newTitle);
	}

	@FXML
	void newCount() {
		// TODO: custom dialog/alert asking for title, min & max
		String title = Utils.ask("Count title");

		Runnable invalid = () -> {
			Alert a = new Alert(Alert.AlertType.ERROR, "Not a valid number");
			a.initOwner(Main.getStage());
			a.showAndWait();
		};

		int min;
		try {
			min = Integer.parseInt(Utils.ask("Count min"));
		} catch (NumberFormatException nfe) {
			invalid.run();
			return;
		}

		int max;
		try {
			max = Integer.parseInt(Utils.ask("Count max"));
		} catch (NumberFormatException nfe) {
			invalid.run();
			return;
		}

		Count count = new Count(title, min, max);
		counts.add(count);
	}


	//<editor-fold desc="Getters & Setters">
	StringProperty titleProperty() {
		return titleProperty;
	}

	String getTitle() {
		return titleProperty().get();
	}

	void setTitle(String title) {
		titleProperty().set(title);
	}
	//</editor-fold>

	static class Serializer implements JsonSerializer<Subject>, JsonDeserializer<Subject> {

		@Override
		public JsonElement serialize(Subject src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			obj.addProperty("title", src.getTitle());

			JsonArray counts = new JsonArray();
			for (Count count : src.counts) {
				counts.add(Counter.gson.toJsonTree(count, Count.class));
			}
			obj.add("counts", counts);

			return obj;
		}

		@Override
		public Subject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
			JsonObject obj = json.getAsJsonObject();

			String title = obj.get("title").getAsString();
			Count[] arr = new Gson().fromJson(obj.getAsJsonArray("counts"), Count[].class);
			List<Count> list = arr == null ? Collections.emptyList() : List.of(arr);

			return new Subject(title, list);
		}
	}

}
