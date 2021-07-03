package com.github.koolskateguy89.mobileos.app.system.browser;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebHistory.Entry;
import javafx.scene.web.WebView;

import com.github.koolskateguy89.mobileos.utils.Utils;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

public class WebBrowser extends VBox {

	@Getter @Setter
	private String defaultUrl;

	public WebBrowser() {
		this(null);
	}

	@SneakyThrows(IOException.class)
	public WebBrowser(String defaultUrl) {
		this.defaultUrl = defaultUrl;

		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("system/browser/WebBrowser"));
		loader.setRoot(this);
		loader.setController(this);
		loader.load();
	}

	// Handle holding button: https://stackoverflow.com/a/41199986
	// Show button ContextMenu upon hold
	static class HoldHandler implements EventHandler<MouseEvent> {
		final long duration;
		long startTime;

		final Button button;

		HoldHandler(Duration duration, Button button) {
			this.duration = duration.toMillis();
			this.button = button;
		}

		@Override
		public void handle(MouseEvent event) {
			if (event.isConsumed())
				return;

			// only on LMB hold
			if (event.getButton().equals(MouseButton.PRIMARY)) {
				if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
					startTime = System.currentTimeMillis();
				} else if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
					if (System.currentTimeMillis() - startTime > duration) {
						button.getContextMenu().show(button, event.getScreenX(), event.getScreenY());
						event.consume();
					}
				}
			}
		}
	}

	private static final List<Object> avoidGC = new ArrayList<>();

	@FXML @Getter
	private WebView webView;
	@Getter
	private WebEngine webEngine;
	@Getter
	private WebHistory webHistory;
	@Getter
	private Worker<Void> loadWorker;

	// TODO: show history upon holding button (done-ish)
	@FXML
	private JFXButton back;
	private BooleanBinding canGoBack;
	private final ContextMenu backMenu = new ContextMenu();

	@FXML
	private JFXButton forward;
	private BooleanBinding canGoForward;
	private final ContextMenu forwardMenu = new ContextMenu();

	private final PseudoClass LOADING_PS = PseudoClass.getPseudoClass("loading");

	@FXML
	private ImageView reloadView;

	@FXML @Getter
	private JFXTextField addressBar;

	@FXML
	private MenuButton menu;
	public ObservableList<MenuItem> getMenuItems() {
		return menu.getItems();
	}

	@FXML
	private JFXProgressBar progressBar;

	// sort of broken - the title doesn't show until back/forward is actually pressed I think (sometimes)
	// also there are [probably] realllly inefficient but I cannot be bothered

	private void setupBackContextMenu() {
		var entries = webHistory.getEntries();
		ObjectBinding<List<MenuItem>> binding = Bindings.createObjectBinding(() -> {
			final int idx = webHistory.getCurrentIndex(); // idx == backs.size()

			List<Entry> backs = entries.subList(0, idx);
			List<MenuItem> result = new ArrayList<>(idx);

			for (int i = 0; i < idx; i++) {
				Entry entry = backs.get(i);
				MenuItem mi = new MenuItem(entry.getTitle());
				final int finalI = i;
				// essentially go back enough times to get to this entry (don't load the url as it'll alter history)
				mi.setOnAction(event -> webHistory.go(finalI - idx));
				result.add(mi);
			}

			return result;
		}, entries, webHistory.currentIndexProperty());

		binding.addListener((obs, oldValue, newValue) -> {
			backMenu.getItems().setAll(newValue);
		});

		// need a global reference so the Binding won't be GC'd
		avoidGC.add(binding);
	}

	private void setupForwardContextMenu() {
		var entries = webHistory.getEntries();
		ObjectBinding<List<MenuItem>> binding = Bindings.createObjectBinding(() -> {
			final int idx = webHistory.getCurrentIndex();
			if (idx >= entries.size())
				return List.of();

			List<Entry> forwards = entries.subList(idx + 1, entries.size());
			List<MenuItem> result = new ArrayList<>(forwards.size());

			for (int i = 0; i < forwards.size(); i++) {
				Entry entry = forwards.get(i);
				MenuItem mi = new MenuItem(entry.getTitle());
				final int finalI = i;
				// essentially go forward enough to get to this entry
				mi.setOnAction(event -> webHistory.go(finalI + 1));
				result.add(mi);
			}

			return result;
		}, entries, webHistory.currentIndexProperty());

		binding.addListener((obs, oldValue, newValue) -> {
			forwardMenu.getItems().setAll(newValue);
		});

		// need a global reference so the Binding won't be GC'd
		avoidGC.add(binding);
	}

	private void setupMenu() {
		var items = menu.getItems();

		JFXButton zoomOut = new JFXButton("-");
		zoomOut.setOnAction(actionEvent -> {
			webView.setZoom(webView.getZoom() - 0.1);
		});

		// ffs using 'webView.zoomProperty().multiply(100).asString().concat("%")' includes floating point imprecision
		StringBinding zoomBinding = Bindings.createStringBinding(() -> {
			double zoom = webView.getZoom() * 100;
			int accurateValue = (int) zoom;
			return accurateValue + "%";
		}, webView.zoomProperty());
		Label zoomLevel = new Label();
		zoomLevel.textProperty().bind(zoomBinding);

		JFXButton zoomIn = new JFXButton("+");
		zoomIn.setOnAction(actionEvent -> {
			webView.setZoom(webView.getZoom() + 0.1);
		});

		HBox zoomNode = new HBox(2);
		zoomNode.getChildren().addAll(new Label("Zoom"), new Separator(Orientation.VERTICAL), zoomOut, zoomLevel, zoomIn);

		CustomMenuItem zoom = new CustomMenuItem(zoomNode);
		items.add(zoom);
		
		//items.add(new SeparatorMenuItem());
	}

	@FXML
	private void initialize() {
		webEngine = webView.getEngine();
		webHistory = webEngine.getHistory();
		loadWorker = webEngine.getLoadWorker();

		setupMenu();

		// update addressBar on location change
		addLocationListener((obs, oldLocation, newLocation) -> {
			addressBar.setText(newLocation);
		});

		// disable forward/back depending on history
		canGoBack = webHistory.currentIndexProperty().greaterThan(0);
		back.disableProperty().bind(canGoBack.not());
		canGoForward = webHistory.currentIndexProperty().lessThan(Bindings.size(webHistory.getEntries()).subtract(1));
		forward.disableProperty().bind(canGoForward.not());

		// show history upon holding back/forward (using contextMenu)
		final Duration duration = Duration.ofMillis(500);
		back.addEventFilter(MouseEvent.ANY, new HoldHandler(duration, back));
		forward.addEventFilter(MouseEvent.ANY, new HoldHandler(duration, forward));

		// show history upon right clicking back/forward
		back.setContextMenu(backMenu);
		setupBackContextMenu();
		forward.setContextMenu(forwardMenu);
		setupForwardContextMenu();

		// update reloadBtn icon when loading webpage (using the ":loading" pseudoclass)
		loadWorker.runningProperty().addListener((obs, wasRunning, isRunning) -> {
			reloadView.pseudoClassStateChanged(LOADING_PS, isRunning);
		});

		// bind progressBar progress to loadWorker progress
		progressBar.progressProperty().bind(loadWorker.progressProperty());
		// implement the user of ":loading" pseudoclass
		loadWorker.runningProperty().addListener((obs, wasRunning, isRunning) -> {
			progressBar.pseudoClassStateChanged(LOADING_PS, isRunning);
		});



		// TODO: basically handle address error
		loadWorker.stateProperty().addListener((obs, oldState, newState) -> {
			if (newState == Worker.State.FAILED) {
				// TODO
				// maybe still be null (no known exception despite fail)
				Throwable e = loadWorker.getException();
				if (e != null) {
					// TODO
				}
			}
		});

		// invalid url & dat
		loadWorker.exceptionProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal == null)
				return;

			String reason = newVal.getMessage();
			/**
			 * TODO: switch case on reason according to {@link WebEngine.LoadWorker#describeError}
			 */
			switch (reason) {

			}
		});
	}

	@FXML
	public void go() {
		String url = addressBar.getText();
		webEngine.load(url);
	}

	@FXML
	public void back() {
		if (canGoBack())
			webHistory.go(-1);
	}

	@FXML
	public void forward() {
		if (canGoForward())
			webHistory.go(1);
	}

	/**
	 * Different behavior when loading: cancels loading & goes back to previous page
	 */
	@FXML
	public void reload() {
		if (isLoading()) {
			loadWorker.cancel();
			back();
		} else {
			webEngine.reload();
		}
	}

	public void loadDefaultUrl() {
		load(defaultUrl);
	}

	public void load(String url) {
		webEngine.load(url);
	}

	public boolean isLoading() {
		return loadWorker.isRunning();
	}

	public boolean canGoBack() {
		return canGoBack.get();
	}

	public boolean canGoForward() {
		return canGoForward.get();
	}

	public void addLocationListener(ChangeListener<String> listener) {
		webEngine.locationProperty().addListener(listener);
	}

	public void addOnWebPageLoad(Runnable func) {
		loadWorker.runningProperty().addListener((obs, wasRunning, isRunning) -> {
			if (isRunning)
				func.run();
		});
	}

	/**
	 * {@link WebEngine#onAlertProperty()}
	 */
	public void setOnAlert(EventHandler<WebEvent<String>> handler) {
		webEngine.setOnAlert(handler);
	}

	/**
	 * {@link WebEngine#onErrorProperty()}
	 */
	public void setOnError(EventHandler<WebErrorEvent> handler) {
		webEngine.setOnError(handler);
	}

	/**
	 * {@link WebEngine#onResizedProperty()}
	 */
	public void setOnResized(EventHandler<WebEvent<Rectangle2D>> handler) {
		webEngine.setOnResized(handler);
	}

	/**
	 * {@link WebEngine#onStatusChangedProperty()}
	 */
	public void setOnStatusChanged(EventHandler<WebEvent<String>> handler) {
		webEngine.setOnStatusChanged(handler);
	}

	/**
	 * {@link WebEngine#onVisibilityChangedProperty()}
	 */
	public void setOnVisibilityChanged(EventHandler<WebEvent<Boolean>> handler) {
		webEngine.setOnVisibilityChanged(handler);
	}

	/**
	 * {@link WebEngine#setUserAgent(String)}
	 */
	public void setUserAgent(String userAgent) {
		webEngine.setUserAgent(userAgent);
	}

}
