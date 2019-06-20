package visualizer.viewer;

import com.archdia.dv8.cluster.Clustering;
import com.archdia.dv8.matrix.DependencyMatrix;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import visualizer.dv8.utilities.IOUtilities;

import java.io.File;
import java.io.IOException;

public class FileChooserManager {
	private static final SimpleObjectProperty<File> tempPath = new SimpleObjectProperty<>(new File("./"));
	private static Stage defaultStage = null;

	public static void setDefaultStage(Stage stage) {
		FileChooserManager.defaultStage = stage;
	}

	public static File loadFile(ExtensionFilter filter) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(tempPath.getValue());
		fileChooser.getExtensionFilters().add(filter);
		fileChooser.setSelectedExtensionFilter(filter);

		File filePath = fileChooser.showOpenDialog(defaultStage);
		if (filePath != null) {
			tempPath.set(filePath.getParentFile());
		}
		return filePath;
	}

	private static File loadFile(Stage stage, ExtensionFilter filter) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(tempPath.getValue());
		fileChooser.getExtensionFilters().add(filter);
		fileChooser.setSelectedExtensionFilter(filter);

		File filePath = fileChooser.showOpenDialog(stage);
		if (filePath != null) {
			tempPath.set(filePath.getParentFile());
		}
		return filePath;
	}

	public static Clustering loadDv8ClusteringJson() {
		ExtensionFilter filter = new ExtensionFilter("dv8 cluster json", "*.json", "*.txt");
		File filePath = loadFile(filter);
		Clustering clustering = null;
		if (filePath != null) {
			try {
				clustering = IOUtilities.loadClusterFromJSON(filePath);
			} catch (IOException e) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Failed reading file");
				alert.setHeaderText("The input file may not be valid clustering json file");
				alert.setContentText(e.getMessage());
				alert.showAndWait();
			}
		}
		return clustering;
	}

	public static DependencyMatrix<String> loadDv8DepMatrix() {
		Stage dialog = new Stage();
		dialog.setTitle("Confirmation");
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(defaultStage);

		Button jsonButton = new Button("Choose matrix json...");
		Button typeMappingButton = new Button("Choose type mapping...");
		Button loadButton = new Button("load");

		VBox dialogVBox = new VBox();

		dialogVBox.getChildren().add(jsonButton);
		dialogVBox.getChildren().add(typeMappingButton);
		dialogVBox.getChildren().add(loadButton);

		SimpleObjectProperty<File> jsonFile = new SimpleObjectProperty<>(null);
		SimpleObjectProperty<File> typeMappingFile = new SimpleObjectProperty<>(null);
		SimpleObjectProperty<DependencyMatrix<String>> matrix = new SimpleObjectProperty<>(null);

		jsonButton.setOnAction((ActionEvent event) -> {
			ExtensionFilter matrixFilter = new ExtensionFilter("dv8 matrix json", "*.json", "*.txt");
			File file = loadFile(dialog, matrixFilter);
			if (file != null) {
				jsonFile.set(file);
				jsonButton.setText(file.getName());
			}
		});

		typeMappingButton.setOnAction((ActionEvent event) -> {
			ExtensionFilter typeFilter = new ExtensionFilter("dv8 dep type mapping", "*.mapping", "*.txt");
			File file = loadFile(dialog, typeFilter);
			if (file != null) {
				typeMappingFile.set(file);
				typeMappingButton.setText(file.getName());
			}
		});

		loadButton.setOnAction((ActionEvent event) -> {
			if (jsonFile.getValue() != null && typeMappingFile.getValue() != null) {
				try {
					matrix.set(IOUtilities.loadMatrixFromJSON(jsonFile.getValue(), typeMappingFile.getValue()));
					dialog.close();
				} catch (Exception e) {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Failed reading file");
					alert.setHeaderText("The input files may not be valid");
					alert.setContentText(e.getMessage());
					alert.showAndWait();
				}
			}
		});

		Scene dialogScene = new Scene(dialogVBox);
		dialog.setScene(dialogScene);
		dialog.showAndWait();
		return matrix.getValue();
	}
}
