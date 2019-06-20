package visualizer.viewer.selecttable;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import visualizer.viewer.clustergroup.ClusterGroupSelectTableItem;

import java.util.Collection;
import java.util.Collections;

public class SelectTableView<T> extends TableView<SelectTableItem<T>> {

    public SelectTableView() {
        this(Collections.emptyList());
    }

    public SelectTableView(Collection<? extends SelectTableItem<T>> items) {
        super();
        this.editableProperty().set(true);

        TableColumn<SelectTableItem<T>, String> nameColumn = new TableColumn<>("Name");
        TableColumn<SelectTableItem<T>, Boolean> addColumn = new TableColumn<>("Add");
        TableColumn<SelectTableItem<T>, Integer> sizeColumn = new TableColumn<>("Size");

        this.getColumns().add(nameColumn);
        this.getColumns().add(addColumn);
        this.getColumns().add(sizeColumn);

        nameColumn.setCellValueFactory((item) -> item.getValue().name);
        addColumn.setCellValueFactory((item) -> item.getValue().isAdded);
        sizeColumn.setCellValueFactory((item) -> item.getValue().size.asObject());

        addColumn.setCellFactory(CheckBoxTableCell.forTableColumn(addColumn));

        nameColumn.editableProperty().set(false);
        sizeColumn.editableProperty().set(false);

        this.getItems().addAll(items);
    }
}
