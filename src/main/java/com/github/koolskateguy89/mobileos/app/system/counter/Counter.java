package com.github.koolskateguy89.mobileos.app.system.counter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.LombokOverride;
import com.github.koolskateguy89.mobileos.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.jfoenix.controls.JFXButton;

import lombok.Getter;
import lombok.SneakyThrows;

/*
FXML concept:

basically the same as FUTBIN's objective tracker screen, with overhead sections (like a pagination),
collapsible sections which contain other
 */

public class Counter extends App {

	static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	final Path jsonPath;

	public Counter(Path dir) {
		super(dir, new Properties() {{
			put("name", "Counter");
			put("version", Main.VERSION);
			put("backgroundColor", "transparent");
		}});
		jsonPath = dir.resolve("counter.json");
	}

	@Getter @LombokOverride // Icon made by Pixel perfect from www.flaticon.com
	private final Image icon = new Image("https://image.flaticon.com/icons/png/512/3424/3424716.png");

	private Node pane;

	@Override @SneakyThrows
	public Node getPane() {
		if (pane == null) {
			long start = System.currentTimeMillis();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Counter.fxml"));
			loader.setController(this);
			pane = loader.load();
			System.out.printf("ms: %s%n", System.currentTimeMillis() - start);
		}

		return pane;
	}


	final ObservableList<Section> sections = FXCollections.observableArrayList();

	private void readJson() {
		try {
			Section[] sections = gson.fromJson(Files.readString(jsonPath), Section[].class);
			if (sections != null)
				Collections.addAll(this.sections, sections);
		} catch (IOException | JsonSyntaxException ignored) {
		}
	}

	private void writeJson() {
		try (BufferedWriter br = Files.newBufferedWriter(jsonPath)) {
			gson.toJson(sections, br);
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	@FXML
	private HBox overhead;

	@FXML
	private ScrollPane sectionWrapper;

	@FXML
	private void initialize() {
		sections.addListener((ListChangeListener<? super Section>) change -> {
			if (!change.next())
				return;

			if (change.wasAdded()) {
				List<? extends Section> added = change.getAddedSubList();
				for (Section section : added) {
					JFXButton b = section.getOverheadButton();
					overhead.getChildren().add(b);
					b.setOnAction(actionEvent -> {
						deActivateAll();
						showSection(section);
					});
				}
			} else if (change.wasRemoved()) {
				List<? extends Section> removed = change.getRemoved();
				for (Section section : removed) {
					if (sectionWrapper.getContent() == section)
						sectionWrapper.setContent(null);
					overhead.getChildren().remove(section.getOverheadButton());
				}
			}
		});

		readJson();
		if (!sections.isEmpty()) {
			Section firstSection = sections.get(0);
			firstSection.getOverheadButton().activate();
			showSection(firstSection);
		}
	}

	@FXML
	void newSection() {
		String title = Utils.ask("New section title:");
		if (Utils.isNullOrBlank(title))
			return;

		Section section = new Section(title);
		sections.add(section);
	}

	private void deActivateAll() {
		overhead.getChildren().forEach(node -> {
			node.pseudoClassStateChanged(Section.active, false);
		});
	}

	private void showSection(Section section) {
		sectionWrapper.setContent(section);
	}

	@Override
	public void onOpen() {}

	@Override
	public void onClose() {
		writeJson();
	}

}
