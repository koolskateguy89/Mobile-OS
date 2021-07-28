package com.github.koolskateguy89.mobileos.fx.home;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import com.github.koolskateguy89.mobileos.Settings;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.Constants;

public class HomeController {

	// TODO: resize to accommodate no longer having faves

	@FXML
	private StackPane homePaneWrapper;
	private HomePane homePane;

	@FXML
	private VBox root;

	@FXML
	private void initialize() {
		homePane = new HomePane();
		homePaneWrapper.getChildren().add(homePane);

		root.backgroundProperty().bind(Settings.BACKGROUND_PROPERTY);

		// TODO: choosing background image (in/from Settings)
		Settings.setBackground(Constants.DEFAULT_BACKGROUND);
	}

	// The first tab is for system applications
	public void initSystemApps(List<App> systemApps) {
		homePane.initSystemApps(systemApps);
	}

	// Installed apps start from second tab
	public void initApps(List<App> apps) {
		homePane.initApps(apps);
	}

	public void addApp(App app) {
		homePane.addApp(app);
	}

}
