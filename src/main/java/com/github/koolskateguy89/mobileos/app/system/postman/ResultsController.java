package com.github.koolskateguy89.mobileos.app.system.postman;

import java.lang.reflect.Constructor;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import marco13.runtimecompiler.RuntimeCompiler;

class ResultsController {

	// TODO: change to a textFlow? with package locked, imports not, class declaration locked, method not, everything else locked

	static final String packageName = System.getProperty("user.name") + ".postman";
	static final String src =
			"""
			package %1$s;

			import java.util.function.Consumer;

			class Responder implements Consumer<String> {

				@Override
				public void accept(String response) {
					System.out.println(response);
				}

			}
			""".formatted(packageName);



	@FXML
	TextArea responseText;

	@FXML
	private void initialize() {
		responseText.setText(src);
	}

	@FXML
	void runResponse() throws Exception {
		final String src = responseText.getText();

		final String className = packageName + ".Responder";

		RuntimeCompiler compiler = new RuntimeCompiler();
		compiler.addClass(className, src);

		compiler.compile();

		Class<?> clazz = compiler.getCompiledClass(className);
		Constructor<?> constructor = clazz.getDeclaredConstructor();
		constructor.setAccessible(true);

		Consumer<String> responseConsumer = (Consumer<String>) constructor.newInstance();
		responseConsumer.accept("oh my goodness");
	}

}
