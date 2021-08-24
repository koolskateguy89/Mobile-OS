package com.github.koolskateguy89.mobileos.app.system.counter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(Section.Serializer.class)
class Section extends VBox {

	final SimpleStringProperty titleProperty;

	final ArrayList<Subject> subjects;

	Section(String title) {
		this(title, Collections.emptyList());
	}

	private Section(String title, List<Subject> countGroups) {
		titleProperty = new SimpleStringProperty(title);
		this.subjects = new ArrayList<>(countGroups);
	}

	@FXML
	void newSubject() {
		// TODO: alert with textfield for title
	}



	StringProperty titleProperty() {
		return titleProperty;
	}

	String getTitle() {
		return titleProperty().get();
	}

	void setTitle(String title) {
		titleProperty().set(title);
	}



	static class Serializer implements JsonSerializer<Section>, JsonDeserializer<Section> {

		@Override
		public JsonElement serialize(Section src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			obj.addProperty("title", src.getTitle());

			JsonArray subjects = new JsonArray();
			for (Subject subject : src.subjects) {
				subjects.add(Counter.gson.toJsonTree(subject, Subject.class));
			}
			obj.add("subjects", subjects);

			return obj;
		}

		@Override
		public Section deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();


			Type listType = new TypeToken<List<Subject>>() {}.getType();
			//List<Count> list = new Gson().fromJson(obj.getAsJsonArray("subjects"), listType);

			Subject[] arr = new Gson().fromJson(obj.getAsJsonArray("counts"), Subject[].class);
			return new Section(obj.get("title").getAsString(), List.of(arr));
		}
	}


}
