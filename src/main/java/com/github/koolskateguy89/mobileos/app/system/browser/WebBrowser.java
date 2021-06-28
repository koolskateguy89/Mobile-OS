package com.github.koolskateguy89.mobileos.app.system.browser;

import java.io.IOException;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
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
	private Worker<Void> loadWorker;

	// TODO: bind back&forward disableProperty
	// TODO: show history upon holding button
	@FXML
	private JFXButton back;
	@FXML
	private JFXButton forward;

	// TODO: add reload icon & change icon if loading
	@FXML
	private JFXButton reloadBtn;

	@FXML @Getter
	private JFXTextField addressBar;

	@FXML
	private void initialize() {
		webEngine = webView.getEngine();
		webHistory = webEngine.getHistory();
		loadWorker = webEngine.getLoadWorker();

		addOnLocationChange((obs, oldLocation, newLocation) -> {
			addressBar.setText(newLocation);
		});

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
	void go() {
		String url = addressBar.getText();
		webEngine.load(url);
	}

	@FXML
	void back() {
		if (webHistory.getCurrentIndex() > 0)
			webHistory.go(-1);
	}

	@FXML
	void forward() {
		if (webHistory.getCurrentIndex() < webHistory.getEntries().size() - 1)
			webHistory.go(1);
	}

	@FXML
	void reload() {
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

	public void addOnLocationChange(ChangeListener<String> listener) {
		webEngine.locationProperty().addListener(listener);
	}

	public void addOnLoadingWebpage(Runnable func) {
		loadWorker.runningProperty().addListener((obs, wasRunning, isRunning) -> {
			if (isRunning)
				func.run();
		});
	}

	public void setOnAlert(EventHandler<WebEvent<String>> handler) {
		webEngine.setOnAlert(handler);
	}

	public void setOnError(EventHandler<WebErrorEvent> handler) {
		webEngine.setOnError(handler);
	}

	public void setOnResized(EventHandler<WebEvent<Rectangle2D>> handler) {
		webEngine.setOnResized(handler);
	}

	public void setOnStatusChanged(EventHandler<WebEvent<String>> handler) {
		webEngine.setOnStatusChanged(handler);
	}

	public void setOnVisibilityChanged(EventHandler<WebEvent<Boolean>> handler) {
		webEngine.setOnVisibilityChanged(handler);
	}

}
