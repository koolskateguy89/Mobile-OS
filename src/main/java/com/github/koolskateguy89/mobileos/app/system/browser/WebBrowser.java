package com.github.koolskateguy89.mobileos.app.system.browser;

import java.io.IOException;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import com.github.koolskateguy89.mobileos.utils.Utils;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

// TODO: history forward/back
public class WebBrowser extends AnchorPane {

	@Getter @Setter
	private String defaultUrl;

	public WebBrowser() {
		this("https://google.com");
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
			String reason = newVal.getMessage();
			/**
			 * TODO: switch case on reason according to {@link WebEngine.LoadWorker#describeError}
			 */
			switch (reason) {

			}
		});

		webEngine.load(defaultUrl);
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
		// TODO: check if loading

		webEngine.reload();
	}

}
