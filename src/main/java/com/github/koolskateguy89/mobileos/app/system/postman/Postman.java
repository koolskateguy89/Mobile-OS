package com.github.koolskateguy89.mobileos.app.system.postman;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import org.controlsfx.control.textfield.CustomTextField;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.LombokOverride;
import com.github.koolskateguy89.mobileos.utils.SingleControllerFactory;

import lombok.Getter;
import lombok.SneakyThrows;

public class Postman extends App {

	public Postman() {
		super(null, new Properties() {{
			put("name", "Postman");
			put("version", Main.getVersion());
			put("backgroundColor", "transparent");
		}});
	}

	@Getter @LombokOverride
	// Icon made by Freepik from www.flaticon.com
	private final Image icon = new Image("https://cdn-icons-png.flaticon.com/512/2601/2601981.png");

	private Node pane;

	private Node results;
	private ResultsController resultsController;

	@Override @SneakyThrows
	public Node getPane() {
		if (pane == null) {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Postman.fxml"));
			loader.setControllerFactory(new SingleControllerFactory(this));
			pane = loader.load();

			loader = new FXMLLoader(getClass().getResource("Results.fxml"));
			loader.setControllerFactory(new SingleControllerFactory(new ResultsController()));
			results = loader.load();
			resultsController = loader.getController();
		}

		return pane;
	}

	@FXML
	private CustomTextField url;

	@FXML
	private ChoiceBox<RequestMethod> methodChoiceBox;

	@FXML
	private void initialize() {
		methodChoiceBox.getItems().addAll(RequestMethod.values());
	}

	@FXML
	void send() throws URISyntaxException {
		final String url = this.url.getText(); new URI(url); // try and check if valid url (not working really)
		final String method = methodChoiceBox.getSelectionModel().getSelectedItem().toString();

		System.out.printf("%s: %s%n", url, method);
	}

	void openResults() {
		((Pane)pane.getParent()).getChildren().setAll(results);
	}

	void openMain() {
		((Pane)results.getParent()).getChildren().setAll(pane);
	}

}
