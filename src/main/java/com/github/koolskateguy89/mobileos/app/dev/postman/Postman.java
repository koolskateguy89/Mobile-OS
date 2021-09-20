package com.github.koolskateguy89.mobileos.app.dev.postman;

import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.LombokOverride;
import com.github.koolskateguy89.mobileos.utils.SelfControllerFactory;

import lombok.Getter;
import lombok.SneakyThrows;

import marco13.runtimecompiler.RuntimeCompiler;

// TODO: put into apps/Postman once done
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

	@Override @SneakyThrows
	public Node getPane() {
		if (pane == null) {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Postman.fxml"));
			loader.setControllerFactory(new SelfControllerFactory(this));
			pane = loader.load();
		}

		return pane;
	}

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
	private TextArea responseText;

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
