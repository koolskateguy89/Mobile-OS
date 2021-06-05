package com.github.koolskateguy89.mobileos.app.system.browser;

import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.web.WebHistory;

public class BrowserController {

	@FXML
	private TabPane tabPane;
	private List<Tab> tabs;

	@FXML
	private Tab newTab;

	@FXML
	private void initialize() {
		// this is in order to keep the newTab tab at the end
		tabs = tabPane.getTabs().subList(0, 0);

		newTab();
		// select the tab just made
		tabPane.getSelectionModel().select(0);

		newTab.setOnSelectionChanged(this::shush);
	}

	// TODO: rename lol
	private void shush(Event event) {
		// newTab was deselected
		if (!newTab.isSelected())
			return;

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
		Tab tab = new Tab(null, browser);
		// TODO: change this to basically pad/cut the title
		tab.textProperty().bind(browser.getWebEngine().titleProperty());
		// TODO: give better name
		StringBinding tabTitle = Bindings.createStringBinding(() -> {
			String title = browser.getWebEngine().getTitle();

			// TODO

			return title;
		}, browser.getWebEngine().titleProperty());
		//tab.textProperty().bind(tabTitle);
		return tab;
	}

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
			// entries is unmodifiable
			//dupeHistory.getEntries().addAll(history.getEntries());
			//dupeHistory.go(history.getCurrentIndex());

			tabs.add(dupe);
		});
		items.add(duplicate);

		return cm;
	}

}
