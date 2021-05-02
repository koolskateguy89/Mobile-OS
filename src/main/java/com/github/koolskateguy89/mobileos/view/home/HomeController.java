package com.github.koolskateguy89.mobileos.view.home;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.prefs.Settings;
import com.github.koolskateguy89.mobileos.utils.Constants;
import com.github.koolskateguy89.mobileos.utils.Utils;
import com.jfoenix.controls.JFXButton;

public class HomeController {

	@FXML
	private TabPane tabPane;
	private List<Tab> tabs;

	@FXML
	private VBox root;

	// TODO: 'faves'
	@FXML
	private JFXButton fav0;

	@FXML
	private JFXButton fav1;

	@FXML
	private JFXButton fav2;

	@FXML
	private JFXButton fav3;

	@FXML
	public void initialize() {
		tabs = tabPane.getTabs();

		root.backgroundProperty().bind(Settings.BACKGROUND_PROPERTY);

		// TODO: choosing background image
		Settings.setBackground(Constants.DEFAULT_BACKGROUND);
	}

	private static final URL grid = Utils.getFxmlUrl("home/HomeGrid");
	private static GridPane newGrid() {
		try {
			return FXMLLoader.load(grid);
		} catch (IOException e) {
			// should not happen
			e.printStackTrace();
			return new GridPane();
		}
	}

	// The first tab is for system applications
	public void initSystemApps(List<App> systemApps) {
		final int len = systemApps.size();
		int i = 0;

		GridPane grid = newGrid();
		tabs.add(0, new Tab("0", grid));
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
	}

	public void initApps(List<App> apps) {
		final int len = apps.size();
		int i = 0;
		int tab = 1;

		// Using a do while loop as we still want a tab to show even if there are no apps
		doWhile: do {
			GridPane grid = newGrid();
			tabs.add(new Tab(Integer.toString(tab), grid));
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

		// Show the second tab (first non-system tab)
		tabPane.getSelectionModel().select(1);
	}

	public void initFaves(List<App> faves) {

	}

}
