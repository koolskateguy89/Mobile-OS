package com.github.koolskateguy89.mobileos.app.system.counter;

import java.lang.reflect.Type;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TitledPane;

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

@JsonAdapter(CountGroup.Serializer.class)
class CountGroup extends TitledPane {

	final StringProperty titleProperty = new SimpleStringProperty();

	final ObservableList<Count> counts = FXCollections.observableArrayList();

	CountGroup(String title) {
		this(title, List.of());
	}

	private CountGroup(String title, List<Count> counts) {
		titleProperty.set(title);
		this.counts.addAll(counts);
		Bindings.bindContent(getChildren(), this.counts);
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


	static class Serializer implements JsonSerializer<CountGroup>, JsonDeserializer<CountGroup> {

		@Override
		public JsonElement serialize(CountGroup src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			obj.addProperty("title", src.getTitle());
			// TODO: 'counts'
			JsonArray a = new JsonArray();
			obj.add("counts", a);
			return obj;
		}

		@Override
		public CountGroup deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();


			Type listType = new TypeToken<List<Count>>() {}.getType();
			//List<Count> list = new Gson().fromJson(obj.getAsJsonArray("counts"), listType);

			Count[] arr = new Gson().fromJson(obj.getAsJsonArray("counts"), Count[].class);

			CountGroup countGroup = new CountGroup(obj.get("title").getAsString(), List.of(arr));
			// TODO: 'counts'
			return countGroup;
		}
	}

}
