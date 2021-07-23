package com.github.koolskateguy89.mobileos.app.system.browser;

import java.io.IOException;
import java.time.Duration;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
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
import javafx.scene.input.ContextMenuEvent;
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

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.fx.utils.ExceptionDialog;
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

		FXMLLoader loader = new FXMLLoader(getClass().getResource("WebBrowser.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		loader.load();
	}

	// Handle holding button: https://stackoverflow.com/a/41199986
	// Show button ContextMenu upon hold
	static class HoldHandler implements EventHandler<MouseEvent> {
		final long holdDuration;
		long startTime;

		final Button button;

		HoldHandler(Duration holdDuration, Button button) {
			this.holdDuration = holdDuration.toMillis();
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
					if (System.currentTimeMillis() - startTime > holdDuration) {
						ContextMenuEvent cme = new ContextMenuEvent(
								event.getSource(),
								event.getTarget(),
								ContextMenuEvent.CONTEXT_MENU_REQUESTED,
								event.getX(),
								event.getY(),
								event.getScreenX(),
								event.getScreenY(),
								true,
								event.getPickResult()
						);
						// request button's context menu
						button.fireEvent(cme);

						event.consume();
					}
				}
			}
		}
	}

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

	@FXML
	private JFXButton forward;
	private BooleanBinding canGoForward;

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

	// sort of broken - the title doesn't show until back/forward is actually pressed like twice (its really weird)
	// I'm not sure why but the Entry's title is blank despite that not being 'reality'

	private void backContextMenu(ContextMenuEvent cme) {
		ContextMenu cm = new ContextMenu();

		final int idx = webHistory.getCurrentIndex();

		// reverse order so start with most recent entry
		for (int i = idx - 1; i >= 0; i--) {
			Entry entry = webHistory.getEntries().get(i);
			MenuItem mi = new MenuItem(entry.getTitle());

			int finalI = i;
			mi.setOnAction(actionEvent -> webHistory.go(finalI - idx));

			cm.getItems().add(mi);
		}

		cm.show(back, cme.getScreenX(), cme.getScreenY());
	}

	private void forwardContextMenu(ContextMenuEvent cme) {
		ContextMenu cm = new ContextMenu();

		final int idx = webHistory.getCurrentIndex();
		final int size = webHistory.getEntries().size();

		for (int i = idx + 1; i < size; i++) {
			Entry entry = webHistory.getEntries().get(i);
			MenuItem mi = new MenuItem(entry.getTitle());

			int finalI = i;
			mi.setOnAction(actionEvent -> webHistory.go(finalI + 1));

			cm.getItems().add(mi);
		}

		cm.show(forward, cme.getScreenX(), cme.getScreenY());
	}

	private void setupMenu() {
		var items = menu.getItems();

		//<editor-fold desc="Zoom">
		JFXButton zoomOut = new JFXButton("-");
		zoomOut.setOnAction(actionEvent -> webView.setZoom(webView.getZoom() - 0.1));

		// ffs using 'webView.zoomProperty().multiply(100).asString().concat("%")' includes floating point imprecision
		StringBinding zoomBinding = Bindings.createStringBinding(() -> {
			double zoom = webView.getZoom() * 100;
			int accurateValue = (int) zoom;
			return accurateValue + "%";
		}, webView.zoomProperty());
		Label zoomLevel = new Label();
		zoomLevel.textProperty().bind(zoomBinding);

		JFXButton zoomIn = new JFXButton("+");
		zoomIn.setOnAction(actionEvent -> webView.setZoom(webView.getZoom() + 0.1));

		HBox zoomNode = new HBox(2);
		zoomNode.getChildren().addAll(
				new Label("Zoom       "), new Separator(Orientation.VERTICAL), zoomOut, zoomLevel, zoomIn);

		CustomMenuItem zoom = new CustomMenuItem(zoomNode);
		items.add(zoom);
		//</editor-fold>

		//<editor-fold desc="Font Scale">
		JFXButton fsDecrease = new JFXButton("-");
		fsDecrease.setOnAction(actionEvent -> webView.setFontScale(webView.getFontScale() - 0.1));

		StringBinding fsBinding = Bindings.createStringBinding(() -> {
			double fontScale = webView.getFontScale();
			// use int to eliminate floating point imprecision
			double accurateValue = ((int) fontScale * 100) * 0.01;
			return accurateValue + "x";
		}, webView.fontScaleProperty());
		Label fontScaleLbl = new Label();
		fontScaleLbl.textProperty().bind(fsBinding);

		JFXButton fsIncrease = new JFXButton("+");
		fsIncrease.setOnAction(actionEvent -> webView.setFontScale(webView.getFontScale() + 0.1));

		HBox fsNode = new HBox(2);
		fsNode.getChildren().addAll(
				new Label("Font Scale"), new Separator(Orientation.VERTICAL), fsDecrease, fontScaleLbl, fsIncrease);

		CustomMenuItem fontScale = new CustomMenuItem(fsNode);
		items.add(fontScale);
		//</editor-fold>

		//items.add(new SeparatorMenuItem());
	}

	@FXML
	private void initialize() {
		webEngine = webView.getEngine();
		webHistory = webEngine.getHistory();
		loadWorker = webEngine.getLoadWorker();

		setupMenu();

		Platform.runLater(() -> {
			// FIXME: this doesn't actually change the outerHeight :(
			//System.out.println(webEngine.executeScript("window.outerWidth"));
			//System.out.println(webEngine.executeScript("window.outerHeight"));

			webEngine.executeScript(String.format("window.resizeTo(%s, %s)", webView.getWidth(), webView.getHeight()));
		});

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
		final Duration holdDuration = Duration.ofMillis(500);
		back.addEventFilter(MouseEvent.ANY, new HoldHandler(holdDuration, back));
		forward.addEventFilter(MouseEvent.ANY, new HoldHandler(holdDuration, forward));

		// show history upon right clicking back/forward
		back.setOnContextMenuRequested(this::backContextMenu);
		forward.setOnContextMenuRequested(this::forwardContextMenu);

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

		// invalid url & dat
		loadWorker.exceptionProperty().addListener((obs, oldException, newException) -> {
			if (newException == null)
				return;

			/**
			 * {@link WebEngine.LoadWorker#describeError}
			 */
			ExceptionDialog ed = new ExceptionDialog(newException, "WebBrowser error");
			ed.initOwner(Main.getStage());
			ed.showAndWait();
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
