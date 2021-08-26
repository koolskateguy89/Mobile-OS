package com.github.koolskateguy89.mobileos.app.system.counter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import com.github.koolskateguy89.mobileos.utils.ResourceBundleImpl;
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
import com.jfoenix.controls.JFXButton;

import lombok.Getter;
import lombok.SneakyThrows;

@JsonAdapter(Section.Serializer.class)
class Section extends AnchorPane implements Initializable {

	private final SimpleStringProperty titleProperty;

	@Getter
	final OverheadButton overheadButton;

	Section(String title) {
		this(title, Collections.emptyList());
	}

	@SneakyThrows(IOException.class)
	private Section(String title, List<Subject> subjects) {
		titleProperty = new SimpleStringProperty(title);
		overheadButton = new OverheadButton();
		overheadButton.textProperty().bind(titleProperty);

		ResourceBundleImpl rb = new ResourceBundleImpl();
		rb.put("subjects", subjects);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("Section.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		loader.setResources(rb);
		loader.load();
	}

	@FXML
	private VBox subjects;

	@FXML
	public void initialize(URL location, ResourceBundle rb) {
		subjects.getChildren().addAll((List<Subject>) rb.getObject("subjects"));
	}

	@FXML
	void newSubject() {
		String title = Utils.ask("New subject title:");
		if (Utils.isNullOrBlank(title))
			return;

		Subject subject = new Subject(title);
		subjects.getChildren().add(subject);
	}


	//<editor-fold desc="Getters & Setters">
	public StringProperty titleProperty() {
		return titleProperty;
	}

	public String getTitle() {
		return titleProperty().get();
	}

	public void setTitle(String title) {
		titleProperty().set(title);
	}
	//</editor-fold>

	static class Serializer implements JsonSerializer<Section>, JsonDeserializer<Section> {

		@Override
		public JsonElement serialize(Section src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			obj.addProperty("title", src.getTitle());

			JsonArray subjects = new JsonArray();
			for (Node subject : src.subjects.getChildren()) {
				if (subject instanceof Subject)
					subjects.add(Counter.gson.toJsonTree(subject, Subject.class));
			}
			obj.add("subjects", subjects);

			return obj;
		}

		@Override
		public Section deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
			JsonObject obj = json.getAsJsonObject();

			String title = obj.get("title").getAsString();
			Subject[] arr = new Gson().fromJson(obj.getAsJsonArray("subjects"), Subject[].class);
			List<Subject> subjects = arr == null ? Collections.emptyList() : List.of(arr);

			return new Section(title, subjects);
		}
	}


	static final PseudoClass active = new PseudoClass() {
		@Override
		public String getPseudoClassName() {
			return "active";
		}
	};

	class OverheadButton extends JFXButton {

		@SneakyThrows(IOException.class)
		OverheadButton() {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("OverheadButton.fxml"));
			loader.setRoot(this);
			loader.setController(this);
			loader.load();
			this.addEventHandler(ActionEvent.ACTION, actionEvent -> {
				activate();
			});
		}

		void activate() {
			this.pseudoClassStateChanged(active, true);
		}

		void deactivate() {
			this.pseudoClassStateChanged(active, false);
		}

		@FXML
		void edit() {
			// TODO: create custom dialog/alert asking for new title & asking if to remove

			String newTitle = Utils.ask("New title:");
			if (Utils.isNullOrBlank(newTitle))
				return;

			Section.this.setTitle(newTitle);
		}

	}

}
