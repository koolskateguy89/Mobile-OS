package com.github.koolskateguy89.mobileos.app.system.browser;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebHistory.Entry;
import javafx.stage.Window;

import org.reflections.ReflectionUtils;

import com.jfoenix.adapters.ReflectionHelper;
import com.sun.javafx.scene.control.ContextMenuContent;
import com.sun.javafx.scene.control.ContextMenuContent.MenuItemContainer;
import com.sun.webkit.WebPage;
import com.sun.webkit.network.CookieManager;

import agarkoff.cookiemanager.CookieUtils;

// TODO: add settings (default search engine - whether to search for invalid URLs); clear cookies
public class BrowserController {

	static Path dir;

	private static final String DEFAULT_URL = "https://google.com";

	@FXML
	private TabPane tabPane;
	private List<Tab> tabs;

	@FXML
	private Tab newTab;

	CookieManager cm;
	private final Path cookiesPath = dir.resolve("cookies.json");

	@FXML
	private void initialize() {
		// the default CookieHandler class that is used if no default CookieHandler is explicitly set
		cm = new CookieManager();
		CookieHandler.setDefault(cm);

		// prevent switching tab on trackpad(?) swipe (as it just creates new tabs constantly)
		tabPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
			// we only care about LEFT or RIGHT (triggered by the trackpad swipe)
			if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.RIGHT) {
				// if the target is the tabPane, it's an event that we want
				if (keyEvent.getTarget() != tabPane) {
					// consume the event so it doesn't do anything
					keyEvent.consume();
				}
			}
		});

		// this is in order to keep the newTab tab at the end
		tabs = tabPane.getTabs().subList(0, 0);
		// Once all tabs have been closed, close the app (?)
		ListChangeListener<Tab> lcl = change -> {
			change.next();
			if (change.getList().size() == 1) {
				// TODO:
				//Main.getInstance().goHome();
			}
		};
		tabPane.getTabs().addListener(lcl);

		newTab.setClosable(false);
		newTab.setOnSelectionChanged(this::newTabSelected);

		Platform.runLater(this::configureTabHeaders);
	}

	private static final Class<?> TAB_HEADER_AREA = ReflectionUtils.forName("javafx.scene.control.skin.TabPaneSkin$TabHeaderArea");
	private static final Class<?> TAB_HEADER_SKIN = ReflectionUtils.forName("javafx.scene.control.skin.TabPaneSkin$TabHeaderSkin");

	/*
	 * Found out the selector that gets stuff using ScenicView
	 * https://github.com/JonathanGiles/scenic-view
	 *
	 * TabPaneSkin uses TabPaneSkin$TabHeaderArea which uses a StackPane (headersRegion).
	 * It adds TabPaneSkin$TabHeaderSkin's to the StackPane
	 */
	private void configureTabHeaders() {
		Node tabHeaderArea = tabPane.lookup(".tab-header-area");

		StackPane headersRegion = ReflectionHelper.getFieldContent(TAB_HEADER_AREA, tabHeaderArea, "headersRegion");

		// configure the initial TabHeaderSkin as the tab has already been added once this is called
		Node firstTabSkin = headersRegion.getChildren().get(0);
		configureTabHeaderSkin(firstTabSkin);

		ListChangeListener<Node> lcl = change -> {
			if (change.next() && change.wasAdded())
				change.getAddedSubList().forEach(BrowserController::configureTabHeaderSkin);
		};
		headersRegion.getChildren().addListener(lcl);
	}

	private static void configureTabHeaderSkin(Node tabHeaderSkin) {
		Tab tab = ReflectionHelper.getFieldContent(TAB_HEADER_SKIN, tabHeaderSkin, "tab");
		Label label = ReflectionHelper.getFieldContent(TAB_HEADER_SKIN, tabHeaderSkin, "label");

		label.getStyleClass().add("normal-tab-label");

		WebBrowser browser = (WebBrowser) tab.getContent();

		Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(browser.getWebEngine().locationProperty());
		label.setTooltip(tooltip);
	}

	// this is very iffy
	private void newTabSelected(Event event) {
		// only make a new tab is newTab was selected
		if (newTab.isSelected())
			newTab();
	}

	void onOpen() {
		if (tabs.isEmpty()) {
			newTab();
			// select the tab just made
			tabPane.getSelectionModel().select(0);
		}
		try {
			if (Files.exists(cookiesPath) && !Files.isDirectory(cookiesPath))
				CookieUtils.load(cm, cookiesPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void onClose() {
		try {
			CookieUtils.store(cm, cookiesPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void back() {
		Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
		WebBrowser browser = (WebBrowser) currentTab.getContent();
		browser.back();
	}

	private void newTab() {
		Tab tab = getNewTab();
		tab.setContextMenu(makeContextMenu(tab));
		tabs.add(tab);
		tabPane.getSelectionModel().select(tab);
	}

	private Tab getNewTab() {
		WebBrowser browser = new WebBrowser(DEFAULT_URL);
		browser.loadDefaultUrl();
		WebEngine engine = browser.getWebEngine();
		Tab tab = new Tab(null, browser);
		tab.textProperty().bind(engine.titleProperty());

		// favicon: https://stackoverflow.com/a/35327398
		ObjectBinding<Image> favIcon = Bindings.createObjectBinding(() -> {
			String location = engine.getLocation();

			if (location.isEmpty())
				return null;

			URL url = new URL(location);
			String host = url.getHost();

			String favIconUrl = String.format("http://www.google.com/s2/favicons?domain_url=%s", host);
			return new Image(favIconUrl);
		}, engine.locationProperty());

		ImageView iv = new ImageView();
		iv.imageProperty().bind(favIcon);
		tab.setGraphic(iv);

		// How to alter WebView context menu: https://stackoverflow.com/a/27047819
		browser.getWebView().setOnContextMenuRequested(contextMenuEvent -> {
			for (Window window : Window.getWindows()) {
				if (!(window instanceof ContextMenu))
					continue;

				if (window.getScene() == null || window.getScene().getRoot() == null)
					continue;

				Parent root = window.getScene().getRoot();

				if (root.getChildrenUnmodifiable().isEmpty())
					continue;

				// access to context menu content
				Node popup = root.getChildrenUnmodifiable().get(0);

				if (popup.lookup(".context-menu") == null)
					continue;

				Node bridge = popup.lookup(".context-menu");
				ContextMenuContent cmc = (ContextMenuContent) ((Parent) bridge).getChildrenUnmodifiable().get(0);

				VBox itemsContainer = cmc.getItemsContainer();

				MenuItemContainer inNewWindow = (MenuItemContainer) itemsContainer.getChildren().get(1);
				MenuItem mi = inNewWindow.getItem();
				if (mi.getText().equals("Open Link in New Window")) {
					mi.setText("Open Link in New Tab");
					/*
					mi.setOnAction(e -> {
						// ahhh how do I get the link ffssss
						// maybe I let it start loading then use the location from the loading?
					});
					*/
				}
			}
		});

		return tab;
	}

	private ContextMenu makeContextMenu(Tab tab) {
		WebBrowser browser = (WebBrowser) tab.getContent();
		WebEngine engine = browser.getWebEngine();
		WebHistory history = browser.getWebHistory();

		ContextMenu cm = new ContextMenu();
		ObservableList<MenuItem> items = cm.getItems();

		MenuItem newTabRight = new MenuItem("New tab to the right");
		newTabRight.setOnAction(event -> {
			Tab newTab = getNewTab();
			tabs.add(tabs.indexOf(tab) + 1, newTab);
		});
		items.add(newTabRight);

		items.add(new SeparatorMenuItem());

		MenuItem reload = new MenuItem("Reload");
		reload.setOnAction(event -> engine.reload());
		// Ctrl+R
		reload.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
		items.add(reload);

		MenuItem duplicate = new MenuItem("Duplicate");
		duplicate.setOnAction(event -> {
			Tab dupe = getNewTab();
			dupe.setContextMenu(makeContextMenu(dupe));
			WebBrowser dupeBrowser = (WebBrowser) dupe.getContent();
			WebEngine dupeEngine = dupeBrowser.getWebEngine();

			WebHistory dupeHistory = dupeBrowser.getWebHistory();
			// FIXME: entries is unmodifiable
			// I could load every entry but that's not very efficient is it, I've tried and it doesn't work as I don't
			// wait for them to load ffs

			/* I've tried:
			 *  - using reflection to get the modifiable list in dupeHistory then adding to it. 'Operational' but doesn't
			 *      work as it seems the 'actual' impl. is through the BackForwardList, which I have no idea how to add
			 *      to.
			 *  - using reflection to get the BackForwardList, but I couldn't find a way to add to it even using private
			 *      methods.
			 */

			// this doesn't even work ffs
			WebPage page = ReflectionHelper.getFieldContent(dupeEngine, "page");
			for (Entry entry : history.getEntries()) {
				page.open(page.getMainFrame(), entry.getUrl());
			}

			//dupeBrowser.getWebEngine().load(engine.getLocation());

			tabs.add(dupe);
		});
		items.add(duplicate);

		items.add(new SeparatorMenuItem());

		MenuItem close = new MenuItem("Close");
		// This doesn't work when the tab is first :/
		close.setOnAction(event -> tabs.remove(tab));
		// Ctrl+W
		close.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
		items.add(close);

		MenuItem closeOthers = new MenuItem("Close other tabs");
		closeOthers.setOnAction(event -> {
			// I'm not really sure how best to do this
			tabs.removeIf(t -> t != tab);
		});
		items.add(closeOthers);

		MenuItem closeRight = new MenuItem("Close tabs to the right");
		closeRight.setOnAction(event -> {
			int idx = tabs.indexOf(tab);
			tabs.subList(idx + 1, tabs.size()).clear();
		});
		items.add(closeRight);

		return cm;
	}

}
