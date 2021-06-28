package com.github.koolskateguy89.mobileos.app.system.browser;

import java.io.IOException;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import com.github.koolskateguy89.mobileos.utils.Utils;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

// TODO: progressBar using loadWorker.progressProperty() (ReadOnlyDoubleProperty)
public class WebBrowser extends AnchorPane {

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

	@FXML @Getter
	private WebView webView;
	@Getter
	private WebEngine webEngine;
	@Getter
	private WebHistory webHistory;
	@Getter
	private Worker<Void> loadWorker;

	// TODO: show history upon holding button
	@FXML
	private JFXButton back;
	private BooleanBinding canGoBack;

	@FXML
	private JFXButton forward;
	private BooleanBinding canGoForward;

	@FXML
	private ImageView reloadView;

	@FXML @Getter
	private JFXTextField addressBar;

	@FXML
	private void initialize() {
		webEngine = webView.getEngine();
		webHistory = webEngine.getHistory();
		loadWorker = webEngine.getLoadWorker();

		// update addressBar on location change
		addLocationListener((obs, oldLocation, newLocation) -> {
			addressBar.setText(newLocation);
		});

		// update reloadBtn icon when loading webpage
		loadWorker.runningProperty().addListener((obs, wasRunning, isRunning) -> {
			reloadView.setId(isRunning ? "cancel-img" : "reload-img");
		});

		// disable forward/back depending on history
		canGoBack = webHistory.currentIndexProperty().greaterThan(0);
		back.disableProperty().bind(canGoBack.not());
		canGoForward = webHistory.currentIndexProperty().lessThan(
				Bindings.size(webHistory.getEntries()).subtract(1)
		);
		forward.disableProperty().bind(canGoForward.not());

		// TODO: basically handle address error
		loadWorker.stateProperty().addListener((obs, oldState, newState) -> {
			//System.out.println(newState);
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
