package application;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class AppMainController implements Initializable {
	@FXML
	private Button openButton;
	@FXML
	private GridPane gridPane;
	@FXML
	private Slider thresholdSlider;
	@FXML
	private ImageView imageSource;
	@FXML
	private ImageView imageOut;

	private OpenCvObject img = null;

	private int lastSliderValue;

	// Event Listener on Button[#openButton].onAction
	@FXML
	public void onOpenButton(ActionEvent event) {
		System.out.println("Click on Button 'Open'");

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().addAll(
				// new ExtensionFilter("Text Files", "*.txt"),
				// new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
				// new ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
				// new ExtensionFilter("All Files", "*.*"));
				new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));

		Stage stage = (Stage) gridPane.getScene().getWindow();

		File selectedFile = fileChooser.showOpenDialog(stage);
		if (selectedFile != null) {
			System.out.format("File selected : %s\n", selectedFile);
			img = new OpenCvObject(selectedFile.toString());

			if (img != null) {
				runTraitementImage();
				imageSource.setImage(img.getImageSource());

				System.out.println("Image is changed");

			}
		}

	};

	private void ListingThreads() {
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();

		String line = "-------------------------------\n";
		System.out.format(line + "Thread counter : %d\n" + line, threadSet.size());
		for (Thread thread : threadSet) {
			System.out.println(thread.getName());
		}
		System.out.println(line);
	}

	@FXML
	public void onMouseReleased(MouseEvent event) {
		System.out.println("Click released");

		int value = (int) Math.round(thresholdSlider.getValue());
		System.out.format("Value threshold : %d\n", value);
		if (img != null && value != lastSliderValue) {
			runTraitementImage();
			lastSliderValue = value;
		}
	}

	void runTraitementImage() {

		ListingThreads();
		for (Thread t : Thread.getAllStackTraces().keySet()) {
			if (t.getName().equals("Process Image")) {
				if (t != null) {
					t.interrupt();
				}
			}
		}
		ListingThreads();

		new Thread(() -> {
			System.out.println("Inside : " + Thread.currentThread().getName());
			Thread.currentThread().setName("Process Image");
			Image newImg = img.getImageOut((int) Math.round(thresholdSlider.getValue()));
			javafx.application.Platform.runLater(() -> imageOut.setImage(newImg));
		}).start();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("Initialize");
	}
}
