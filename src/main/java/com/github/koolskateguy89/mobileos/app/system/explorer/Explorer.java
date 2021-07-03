package com.github.koolskateguy89.mobileos.app.system.explorer;

import java.io.IOException;
import java.util.Properties;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.LombokOverride;
import com.github.koolskateguy89.mobileos.utils.Utils;

import lombok.Getter;

public final class Explorer extends App {

	private static final Properties props = new Properties() {{
		put("name", "Explorer");
		put("version", Main.VERSION);
		put("backgroundColor", "yellow");
	}};

	public Explorer() {
		super(null, props);
	}

	@Getter(lazy = true) @LombokOverride
	private final Node pane = new Pane() {{
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("system/explorer/Explorer"));
		loader.setRoot(this);
		try {
			loader.load();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}};

	@Getter @LombokOverride
	// Icon made by DinosoftLabs (www.flaticon.com/authors/dinosoftlabs) from www.flaticon.com
	private final Image icon = new Image("https://image.flaticon.com/icons/png/512/3617/3617042.png");

	@Override
	public void onOpen() {

	}

	@Override
	public void goBack(ActionEvent event) {

	}

	@Override
	public void onClose() {

	}
}
