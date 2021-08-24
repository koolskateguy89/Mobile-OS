package com.github.koolskateguy89.mobileos.app.system.counter;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

class OverheadSection extends VBox {

	final SimpleStringProperty titleProperty;

	final List<CountGroup> countGroups = new ArrayList<>();

	OverheadSection(String title) {
		titleProperty = new SimpleStringProperty(title);
	}


	@FXML
	void newCountGroup() {
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


}
