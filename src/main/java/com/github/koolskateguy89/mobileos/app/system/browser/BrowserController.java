package com.github.koolskateguy89.mobileos.app.system.browser;

import java.net.URL;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;

import com.github.koolskateguy89.mobileos.app.App;

// TODO: add settings (default search engine - whether to search for invalid URLs)
public class BrowserController {

	@FXML
	private TabPane tabPane;
	private List<Tab> tabs;

	@FXML
	private Tab newTab;

	@FXML
	private void initialize() {
		// prevent switching tab on swipe (as it just creates new tabs constantly): https://stackoverflow.com/a/47841382
		tabPane.addEventFilter(SwipeEvent.ANY, SwipeEvent::consume);

		// this is in order to keep the newTab tab at the end
		tabs = tabPane.getTabs().subList(0, 0);

		newTab();
		// select the tab just made
		tabPane.getSelectionModel().select(0);

		newTab.setOnSelectionChanged(this::shush);
	}

	// TODO: rename lol
	private void shush(Event event) {
		// only make a new tab is newTab was selected
		if (newTab.isSelected())
			newTab();
	}

	private void newTab() {
		Tab tab = getNewTab();
		tab.setContextMenu(makeContextMenu(tab));
		tabs.add(tab);
		tabPane.getSelectionModel().select(tab);
	}

	private static Tab getNewTab() {
		WebBrowser browser = new WebBrowser();
		WebEngine engine = browser.getWebEngine();
		Tab tab = new Tab(null, browser);

		// favicon: https://stackoverflow.com/a/35327398
		ObjectBinding<Image> favIcon = Bindings.createObjectBinding(() -> {
			String location = engine.getLocation();

			// TODO: change icon (maybe loading icon or something idk)
			if (location.isEmpty())
				return App.AppConstants.FALLBACK_ICON;

			URL url = new URL(location);
			String host = url.getHost();

			String favIconUrl = String.format("http://www.google.com/s2/favicons?domain_url=%s", host);
			return new Image(favIconUrl);
		}, engine.locationProperty());

		ImageView iv = new ImageView();
		iv.imageProperty().bind(favIcon);

		Label lbl = new Label();
		lbl.setMinWidth(80);
		lbl.setMaxWidth(80);
		lbl.setTextOverrun(OverrunStyle.CLIP);
		lbl.textProperty().bind(engine.titleProperty());

		tab.setGraphic(new HBox(4, iv, lbl));

		return tab;
	}

	// TODO
	private ContextMenu makeContextMenu(Tab tab) {
		WebBrowser browser = (WebBrowser) tab.getContent();
		WebHistory history = browser.getWebHistory();

		ContextMenu cm = new ContextMenu();
		ObservableList<MenuItem> items = cm.getItems();

		MenuItem duplicate = new MenuItem("Duplicate");
		duplicate.setOnAction(event -> {
			Tab dupe = getNewTab();
			WebBrowser dupeBrowser = (WebBrowser) dupe.getContent();

			WebHistory dupeHistory = dupeBrowser.getWebHistory();
			// FIXME: entries is unmodifiable
			//dupeHistory.getEntries().addAll(history.getEntries());
			//dupeHistory.go(history.getCurrentIndex());

			tabs.add(dupe);
		});
		items.add(duplicate);

		return cm;
	}

}
