package com.github.koolskateguy89.mobileos.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import com.github.koolskateguy89.mobileos.Main;

public class MainController {

	@FXML
	private StackPane mainPane;

	private Node screen;

	public void setScreen(Node pane) {
		if (pane == screen)
			return;

		screen = pane;
		var children = mainPane.getChildren();

		if (children.isEmpty())
			children.add(pane);
		else    // disable previous pane
			children.set(0, pane).setDisable(true);

		pane.setDisable(false);
	}

	@FXML
	void back(ActionEvent event) {
		Main.getInstance().back(event);
	}

	@FXML
	void home() {
		Main.getInstance().goHome();
	}

}
