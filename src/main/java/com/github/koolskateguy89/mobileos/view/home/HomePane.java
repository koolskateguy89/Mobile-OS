package com.github.koolskateguy89.mobileos.view.home;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.Utils;

// This is basically meant to be a more tailored TabPane
class HomePane extends AnchorPane {

	HomePane() {
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("home/HomePane"));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private StackPane gridWrapper;
	private final List<GridPane> grids = new ArrayList<>();

	@FXML
	private HBox bottom;

	@FXML
	private void initialize() {
	}

	private static final URL grid = Utils.getFxmlUrl("home/HomeGrid");
	private static GridPane newGrid() {
		try {
			return FXMLLoader.load(grid);
		} catch (IOException io) {
			// should not happen
			io.printStackTrace();
			return new GridPane();
		}
	}

	private Button newBottomButton(GridPane grid, int pos) {
		String s = Integer.toString(pos + 1);
		// FIXME: text isn't showing for some reason
		Button button = new Button(s); // maybe don't have any text?
		button.getStyleClass().add("bottom-circle");

		button.setOnAction(e -> openTab(grid, button));

		// Has to use Platform.runLater() because when this is called, controls are not fully initialized so scene is null
		Platform.runLater(() -> {
			Scene scene = Main.getStage().getScene();
			// When CTRL+pos is pressed, open this tab
			// This could be problematic if other apps want to use CTRL+number
			scene.getAccelerators().put(new KeyCharacterCombination(s, KeyCombination.CONTROL_DOWN), () -> {
				// Need to make sure we are home as the accelerator is 'global'
				// HomePane.this.isFocused nor isVisible work
				if (Main.getInstance().atHome()) {
					openTab(grid, button);
				}
			});
		});

		return button;
	}

	void initSystemApps(List<App> systemApps) {
		GridPane grid = newGrid();
		grids.add(grid);

		Button button = newBottomButton(grid, 0);
		bottom.getChildren().add(button);

		final int len = systemApps.size();
		int i = 0;

		rowLoop: for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 4; col++) {
				if (i >= len)
					break rowLoop;

				App app = systemApps.get(i);
				i++;

				Node appNode = app.getNode();
				grid.add(appNode, col, row);
			}
		}

		// We show the first tab (system apps)
		openTab(grid, button);
	}

	// Installed apps start from second tab
	void initApps(List<App> apps) {
		final int len = apps.size();
		int i = 0;
		int tab = 1;

		// Using a do while loop as we still want a tab to show even if there are no apps
		doWhile: do {
			GridPane grid = newGrid();
			grids.add(grid);

			Button button = newBottomButton(grid, tab);
			bottom.getChildren().add(button);

			tab++;

			for (int row = 0; row < 4; row++) {
				for (int col = 0; col < 4; col++) {
					if (i >= len)
						break doWhile;

					App app = apps.get(i);
					i++;

					Node appNode = app.getNode();
					grid.add(appNode, col, row);
				}
			}
		} while (i < len);
	}

	void addApp(App app) {
		int size = grids.size();

		GridPane grid = grids.get(size - 1);

		// full grid
		if (grid.getChildren().size() == 16) {
			grid = newGrid();
			grids.add(grid);

			Button newButton = newBottomButton(grid, size);
			bottom.getChildren().add(newButton);

			grid.add(app.getNode(), 0, 0);
		} else {
			int apps = grid.getChildren().size();

			int row = apps / 4;
			int col = apps & 3; //apps % 4

			grid.add(app.getNode(), col, row);
		}
	}

	void openTab(GridPane grid, Button button) {
		// show the corresponding grid
		gridWrapper.getChildren().setAll(grid);

		// unhighlight other buttons
		bottom.getChildren().forEach(n -> n.setStyle(""));

		// highlight this button
		button.setStyle("-fx-background-color: white");
	}

}
