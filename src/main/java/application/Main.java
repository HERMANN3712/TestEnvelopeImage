package application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Main extends Application {
	private Stage primaryStage;
	private GridPane root;

	static {
		try {
			loadJarDll("opencv_java440.dll");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadJarDll(String name) throws IOException {
		InputStream in = Main.class.getClassLoader().getResourceAsStream(name);

		byte[] buffer = new byte[1024];
		int read = -1;
		File temp = File.createTempFile(name, "");
		FileOutputStream fos = new FileOutputStream(temp);

		while ((read = in.read(buffer)) != -1) {
			fos.write(buffer, 0, read);
		}
		fos.close();
		in.close();

		System.load(temp.getAbsolutePath());
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			
			URL url = getClass().getResource("/AppMain.fxml");
			loader.setLocation(url);
			root = (GridPane) loader.load();

			// Show the scene containing the root layout.
			Scene scene = new Scene(root);
			this.primaryStage = primaryStage;
			this.primaryStage.setTitle("Envelope Image");
			this.primaryStage.getIcons().add(new Image("favicon.png"));
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() throws Exception {
		System.exit(0);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
