package com.github.koolskateguy89.mobileos.fx;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Accordion;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class FailedAppsDialog extends Dialog<ButtonType> {

	public FailedAppsDialog(Map<String, List<Path>> failedApps) {
		DialogPane dialogPane = getDialogPane();

		setTitle("Error loading app(s)");

		dialogPane.setGraphic(new ImageView(new Image("https://github.com/controlsfx/controlsfx/blob/839a39aa6a75b8938256101918292a0044137fe7/controlsfx/src/main/resources/org/controlsfx/dialog/dialog-error.png?raw=true")));
		dialogPane.getButtonTypes().add(ButtonType.OK);

		// context
		//dialogPane.setHeaderText("Some apps have failed to load:");
		dialogPane.setContentText("Some apps have failed to load:");

		// expandable context
		Accordion acc = new Accordion();
		ObservableList<TitledPane> panes = acc.getPanes();

		failedApps.forEach((reason, apps) -> {
			VBox vb = new VBox(2);

			for (Path app : apps) {
				Label l = new Label(app.toString());
				vb.getChildren().add(l);
			}

			TitledPane tp = new TitledPane(reason, vb);
			panes.add(tp);
		});

		acc.setMaxWidth(Double.MAX_VALUE);

		// Update this dialog's height so the accordion can be expanded without resizing
		ChangeListener<Boolean> cl = new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean wasExpanded, Boolean isExpanded) {
				if (isExpanded) {
					double sum = acc.getPanes().stream().mapToDouble(TitledPane::getHeight).sum();

					FailedAppsDialog.this.setHeight(FailedAppsDialog.this.getHeight() + sum);

					dialogPane.expandedProperty().removeListener(this);
				}
			}
		};
		dialogPane.expandedProperty().addListener(cl);

		dialogPane.setExpandableContent(acc);
	}

}
