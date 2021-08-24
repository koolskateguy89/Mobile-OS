package com.github.koolskateguy89.mobileos.app.system.counter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.LombokOverride;

import lombok.Getter;
import lombok.SneakyThrows;

/*
FXML concept:

basically the same as FUTBIN's objective tracker screen, with overhead sections (like a pagination),
collapsible sections which contain other

TODO: rename 'Section' bro tf ;-;'
 */

public class Counter extends App {

	private static final Properties props = new Properties() {{
		put("name", "Counter");
		put("version", Main.VERSION);
		put("backgroundColor", "transparent");
	}};

	public Counter(Path dir) {
		super(dir, props);
	}

	@Getter @LombokOverride // Icon made by Pixel perfect from www.flaticon.com
	private final Image icon = new Image("https://image.flaticon.com/icons/png/512/3424/3424716.png");

	private Node pane;

	@Override @SneakyThrows
	public Node getPane() {
		if (pane == null) {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Counter.fxml"));
			loader.setController(this);
			pane = loader.load();
		}

		return pane;
	}


	List<OverheadSection> overheadSections = new ArrayList<>();

	@FXML
	private void initialize() {
	}

	@FXML
	void newOverheadSection() {
		System.out.println(Database.class.getPackageName().replace('.', '/'));
	}



	@Override
	public void onOpen() {
	}

	@Override
	public void onClose() {
	}

}
