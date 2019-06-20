package visualizer.viewer.graphics;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import server.ResourceLoader;
import visualizer.data.BufferedObservableValue;

import java.util.Random;

public class D3jsControl implements GraphicsControl {
	private WebEngine webEngine;
	private WebView webView;

	private static final Random idGenerator = new Random(System.currentTimeMillis());
	private static final String D3JS_PATH = "/html/d3.v4.min.js";
	private static final String JSON_EMPTY_DATA_NAME = "'empty_data.json'";

	private String emptyPage = "http://127.0.0.1:14285/%s/empty_page.html";
	private String initScript = "/html/%s/init.js";

	private String instantDataPath = "/html/%s/instant_data%d.json";
	private String instantDataName = "'instant_data%d.json'";

	private D3jsUpCall upCall;

	private boolean jsInitiated = false;
	private ObjectProperty<Long> sizeChangeState = new SimpleObjectProperty<>(Long.MIN_VALUE);

	private int updateInterval = 500;

	public D3jsControl(AbstractDrawingModel visModel) {
		int id;
		synchronized (D3jsControl.idGenerator) {
			id = idGenerator.nextInt();
		}

		String folderName = visModel.getFolderName();

		instantDataPath = String.format(instantDataPath, folderName, id);
		instantDataName = String.format(instantDataName, id);

		emptyPage = String.format(emptyPage, folderName);
		initScript = String.format(initScript, folderName);

		webView = new WebView();
		webView.setContextMenuEnabled(false);
		webEngine = webView.getEngine();
		webEngine.load(emptyPage);
		initListeners();
		initJs(initScript);

		visModel.currentState().addListener((value, oldValue, newValue) -> {
					refreshSize();
					updateDrawing(visModel.getGraphicsJson());
				}
		);

		BufferedObservableValue<Long> bufferedSizeChange = new BufferedObservableValue<>(sizeChangeState, updateInterval);
		bufferedSizeChange.getBufferedValue().addListener((value, oldValue, newValue) -> {
			refreshSize();
			updateDrawing(visModel.getGraphicsJson());
		});

	}

	public D3jsControl(AbstractDrawingModel visModel, D3jsUpCall upCall) {
		this(visModel);
		registerUpCall(upCall.getName(), upCall);
	}

	private void registerUpCall(String name, Object upcall) {
		JSObject jso = (JSObject) webEngine.executeScript("window");
		jso.setMember(name, upcall);
	}

	private void initListeners() {
		webView.widthProperty().addListener((value, oldV, newV) -> {
			sizeChangeState.setValue(sizeChangeState.get() + 1);
		});
		webView.heightProperty().addListener((value, oldV, newV) -> {
			sizeChangeState.setValue(sizeChangeState.get() + 1);
		});
	}

	private void refreshSize() {
		resizeWidth((int) webView.getWidth());
		resizeHeight((int) webView.getHeight());
	}

	private void initJs(String script) {
		jsInitiated = true;
		JSObject selectedNode = (JSObject) webEngine.executeScript("window");
		selectedNode.setMember("upCall", upCall);
		executeScript(ResourceLoader.getNonNullStringResource(D3JS_PATH));
		executeScript(ResourceLoader.getNonNullStringResource(script));
		resizeWidth((int) webView.getWidth());
		resizeHeight((int) webView.getHeight());
	}

	private void executeScript(String script) {
		if (jsInitiated) {
			try {
				webEngine.executeScript(script);
			} catch (JSException e) {
				System.err.println(e.getMessage());
				System.err.println("at: " + script.substring(0, 10) + "....");
			}
		}
	}

	private void resizeWidth(int width) {
		String s = String.format("resizeWidth(%d)", width - 20);
		executeScript(s);
	}

	private void resizeHeight(int height) {
		String s = String.format("resizeHeight(%d)", height - 20);
		executeScript(s);
	}

	@Override
	public Node getPanel() {
		return webView;
	}

	@Override
	public void updateDrawing(String graphicsJson) {
		if (graphicsJson != null) {
			ResourceLoader.registerResource(instantDataPath, graphicsJson);
			executeScript(String.format("loadData(%s)", instantDataName));
		} else {
			executeScript(String.format("loadData(%s)", JSON_EMPTY_DATA_NAME));
		}
	}
}
