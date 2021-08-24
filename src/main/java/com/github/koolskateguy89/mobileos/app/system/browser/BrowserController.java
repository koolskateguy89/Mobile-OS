package com.github.koolskateguy89.mobileos.app.system.browser;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
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

import com.github.koolskateguy89.mobileos.Main;
import com.jfoenix.adapters.ReflectionHelper;
import com.sun.javafx.scene.control.ContextMenuContent;
import com.sun.javafx.scene.control.ContextMenuContent.MenuItemContainer;
import com.sun.javafx.webkit.theme.ContextMenuImpl;
import com.sun.webkit.ContextMenuItem;
import com.sun.webkit.WebPage;
import com.sun.webkit.network.CookieManager;

import lombok.SneakyThrows;

import agarkoff.cookiemanager.CookieUtils;

// TODO: add settings (default search engine - whether to search for invalid URLs); clear cookies;
public class BrowserController {

	static Path dir;

	private static final String DEFAULT_URL = "https://google.com";

	@FXML
	private TabPane tabPane;
	private List<Tab> tabs;
	// added to the Menu of each WebBrowser
	private final List<MenuItem> browserMenuItems = new ArrayList<>() {{
		add(new SeparatorMenuItem());

		MenuItem newTab = new MenuItem("New tab");
		newTab.setOnAction(actionEvent -> newTab());
		// CTRL+T
		newTab.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
		add(newTab);

		MenuItem settings = new MenuItem("Settings");
		// TODO: browser settings
		add(settings);
	}};

	@FXML
	private Tab newTab;

	private final File userDataDirectory = dir.resolve("data").toFile();

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
				change.getAddedSubList().forEach(this::configureTabHeaderSkin);
		};
		headersRegion.getChildren().addListener(lcl);
	}

	private void configureTabHeaderSkin(Node tabHeaderSkin) {
		Tab tab = ReflectionHelper.getFieldContent(TAB_HEADER_SKIN, tabHeaderSkin, "tab");
		Label label = ReflectionHelper.getFieldContent(TAB_HEADER_SKIN, tabHeaderSkin, "label");
		StackPane closeBtn = ReflectionHelper.getFieldContent(TAB_HEADER_SKIN, tabHeaderSkin, "closeBtn");

		label.getStyleClass().add("normal-tab-label");

		// oh my ScenicView is so good
		closeBtn.setOnMousePressed(mouseEvent -> tabs.remove(tab));

		WebBrowser browser = (WebBrowser) tab.getContent();

		Tooltip tooltip = new Tooltip();
		tooltip.textProperty().bind(browser.getWebEngine().locationProperty());
		label.setTooltip(tooltip);
	}

	// this is quite iffy ?
	private void newTabSelected(Event event) {
		// for some reason tabs.isEmpty causes a concurrent mod exc >:(
		if (tabPane.getTabs().size() == 1) {
			// Once all tabs have been closed, close the app
			Main.getInstance().goHome();
		} else if (newTab.isSelected()) {
			// only make a new tab is newTab was selected
			newTab();
		}
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

	@SneakyThrows
	void clearCookies() {
		// TODO: clear cookiemanager
		Files.deleteIfExists(cookiesPath);
	}

	private void newTab() {
		// close every tab's context menu (newTab causes a ConcurrentModificationException if a CM is open)
		// lmao it doesn't even work, I think cos this is all on FxApplicationThread
		tabs.forEach(tab -> tab.getContextMenu().hide());

		Tab tab = getNewTab();
		tab.setContextMenu(makeContextMenu(tab));
		tabs.add(tab);
		tabPane.getSelectionModel().select(tab);
	}

	private Tab getNewTab() {
		WebBrowser browser = new WebBrowser(DEFAULT_URL);
		browser.loadDefaultUrl();
		browser.getMenuItems().addAll(browserMenuItems);

		WebEngine engine = browser.getWebEngine();
		engine.setUserDataDirectory(userDataDirectory);

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

		/**
		 * The ContextMenu is implemented by {@link ContextMenuImpl#show(com.sun.webkit.ContextMenu.ShowContext, int, int)},
		 *   which uses {@link ContextMenuImpl#fillMenu()} to fill the ContextMenu with items according to
		 *   {@link ContextMenuImpl#items}, which creates either a {@link ContextMenuImpl.MenuItemImpl} or
		 *   {@link ContextMenuImpl.CheckMenuItemImpl}.
		 *
		 * Each implemented MenuItem has a {@link ContextMenuItem} as an "itemPeer", which has an
		 *   '[int] action' {@link ContextMenuItem#action}. This action is passed to
		 *   {@link com.sun.webkit.ContextMenu.ShowContext#notifyItemSelected(int)} which uses
		 *   {@link com.sun.webkit.ContextMenu#twkHandleItemSelected(long, int)} which is native so yeah.
		 *
		 * Actions: (may be more)
		 *  1 = Open Link in New Window
		 *  3 = Copy Link to Clipboard
		 *  9 = Go Back
		 *  10 = Go Forward
		 *  11 = Stop loading
		 *  12 = Reload page
		 *  33 = Open Link
		 */
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

				Node bridge = popup.lookup(".context-menu");

				if (bridge == null)
					continue;

				ContextMenuContent cmc = (ContextMenuContent) ((Parent) bridge).getChildrenUnmodifiable().get(0);

				VBox itemsContainer = cmc.getItemsContainer();

				if (itemsContainer.getChildren().size() < 2)
					continue;

				MenuItemContainer inNewWindow = (MenuItemContainer) itemsContainer.getChildren().get(1);
				MenuItem mi = inNewWindow.getItem();
				if (mi.getText().equals("Open Link in New Window")) {
					itemsContainer.getChildren().remove(1);
					//mi.setText("Open Link in New Tab");
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

			// I'm pretty sure it uses native code to do web stuff sooooo

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
		close.setOnAction(event -> tabs.remove(tab));
		// Ctrl+W
		close.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
		items.add(close);

		MenuItem closeOthers = new MenuItem("Close other tabs");
		closeOthers.setOnAction(event -> {
			// I'm not really sure how best to do this
			tabs.removeIf(t -> t != tab);
		});
		closeOthers.disableProperty().bind(Bindings.size(tabPane.getTabs()).isEqualTo(2));
		items.add(closeOthers);

		MenuItem closeRight = new MenuItem("Close tabs to the right");
		closeRight.setOnAction(event -> {
			int idx = tabs.indexOf(tab);
			tabs.subList(idx + 1, tabs.size()).clear();
		});
		BooleanBinding noTabsToTheRight = Bindings.createBooleanBinding(() -> {
			int idx = tabs.indexOf(tab);
			return idx < tabs.size();
		}, tabPane.getTabs());
		closeRight.disableProperty().bind(noTabsToTheRight);
		items.add(closeRight);

		return cm;
	}

}
