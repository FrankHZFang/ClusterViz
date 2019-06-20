package visualizer.viewer;

import com.archdia.dv8.cluster.Clustering;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import visualizer.viewer.tab.cluster.circlepackage.CirclePackageTab;

import java.util.Collections;
import java.util.List;

public class MainWindowControl extends Application {

	private static Stage initStage;

	private TabPane contentTabPane;
	private MenuBar menuBar;
	private List<Menu> extraToolBarMenus;

	@Override
	public void start(final Stage stage) {
		extraToolBarMenus = Collections.emptyList();
		initStage = stage;
		FileChooserManager.setDefaultStage(stage);
		initUI();
		initTabPaneListener();
	}

	private void initUI() {
		BorderPane mainPanel = new BorderPane();
		contentTabPane = new TabPane();
		contentTabPane.setSide(Side.LEFT);

		menuBar = createMenuBar();
		mainPanel.setTop(menuBar);

		mainPanel.setCenter(contentTabPane);

		Scene scene = new Scene(mainPanel, 960, 600);

		initStage.setOnCloseRequest(e -> System.exit(0));
		initStage.setTitle("Arch Visualizer");
		initStage.setScene(scene);
		initStage.show();
	}

	private void initTabPaneListener() {
		contentTabPane.getSelectionModel().selectedItemProperty().addListener((v, oldTab, newTab) -> {
			menuBar.getMenus().removeAll(extraToolBarMenus);
			if (newTab != null) {
				extraToolBarMenus = ((ContentTabControl) newTab).getToolBarMenus();
				menuBar.getMenus().addAll(extraToolBarMenus);
			}
		});
	}

	private MenuBar createMenuBar() {
		MenuBar menubar = new MenuBar();
		Menu file = new Menu("File");

		MenuItem openDv8ClusteringJsonItem = new MenuItem("Open Dv8 Clustering");
		openDv8ClusteringJsonItem.setOnAction((ActionEvent event) -> {
			Clustering c = FileChooserManager.loadDv8ClusteringJson();
			if (c != null) {
				contentTabPane.getTabs().add(new CirclePackageTab(c.getName(), c));
			}
		});

//		MenuItem openDv8SetJsonGeneralViewItem = new MenuItem("Open Dv8 Sets in General Set View");
//		openDv8SetJsonGeneralViewItem.setOnAction((ActionEvent event) -> {
//			Clustering c = FileChooserManager.loadDv8ClusteringJson();
//			if (c != null) {
//				contentTabPane.getTabs().add(new GeneralSetViewTab(c.getName(), c));
//			}
//		});


		MenuItem clearItem = new MenuItem("Clear Views");
		clearItem.setOnAction((ActionEvent event) -> {
			contentTabPane.getTabs().clear();
		});

		MenuItem exitItem = new MenuItem("Exit");
		exitItem.setOnAction((ActionEvent event) -> System.exit(0));

		file.getItems().addAll(openDv8ClusteringJsonItem,
//				openDv8SetJsonGeneralViewItem,
				clearItem,
				exitItem);

		menubar.getMenus().addAll(file);
		return menubar;
	}
}
