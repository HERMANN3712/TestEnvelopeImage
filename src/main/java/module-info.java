module EnvelopeImage {
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;
	requires javafx.base;
	requires opencv.java;
	requires javafx.swing;
	requires java.desktop;

	opens application to javafx.fxml;

	exports application;
}