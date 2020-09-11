package application;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;

import javafx.application.Platform;
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

	private static String THREADNAME = "Process Image " + UUID.randomUUID().toString();

	@FXML
	private Button openButton;
	@FXML
	private Button statusButton;
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
				statusButton.setText("Intermediate");

			}
		}
	};

	// Event Listener on Button[#openButton].onAction
	@FXML
	public void onIntermediateButton(ActionEvent event) {
		if (img == null)
			return;
		boolean status = statusButton.getText().equals("Intermediate");
		statusButton.setText(status ? "Origin" : "Intermediate");
		if (status) {
			imageSource.setImage(img.getImageIntermediate((int) thresholdSlider.getValue()));
		} else {
			imageSource.setImage(img.getImageSource());
		}
	}

	// Event Listener on Button[#statusButton].onMouseReleased
	@FXML
	public void onMouseReleased(MouseEvent event) {
		int value = (int) Math.round(thresholdSlider.getValue());

		if (img != null && value != lastSliderValue) {
			if (!statusButton.getText().equals("Intermediate")) {
				imageSource.setImage(img.getImageIntermediate((int) thresholdSlider.getValue()));
			}
			runTraitementImage();
			lastSliderValue = value;
		}
	}

	void runTraitementImage() {

		stopThreadImage();

		new Thread(() -> {
			Thread.currentThread().setName(THREADNAME);
			Image newImg = img.getImageOut((int) Math.round(thresholdSlider.getValue()));
			javafx.application.Platform.runLater(() -> {
				imageOut.setImage(newImg);
			});
		}).start();
	}

	public static void stopThreadImage() {
		for (Thread t : Thread.getAllStackTraces().keySet()) {
			if (t.getName().equals(THREADNAME)) {
				if (t != null && !t.isInterrupted()) {
					System.out.println(THREADNAME + " interrupted");
					t.interrupt();
				}
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
