package application;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

			if (img.checked) {
				new Thread(() -> {
					Image newImg = img.getImageOut((int) Math.round(thresholdSlider.getValue()));
					javafx.application.Platform.runLater(() -> imageOut.setImage(newImg));
				}).start();

				imageSource.setImage(img.getImageSource());
				System.out.println("Image is changed");
				
			}
		}

	};

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		thresholdSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.format("New value %d\n", newValue.intValue());
				if (img == null)
					return;			
				
				
				new Thread(() -> {
					Image newImg = img.getImageOut((int) Math.round(newValue.intValue()));
					javafx.application.Platform.runLater(() -> imageOut.setImage(newImg));
				}).start();
				
			}
		});
		System.out.println("Initialize");
	}
}
