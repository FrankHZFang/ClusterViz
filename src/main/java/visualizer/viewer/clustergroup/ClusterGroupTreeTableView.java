package visualizer.viewer.clustergroup;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;

public class ClusterGroupTreeTableView extends TreeTableView<ClusterGroupSelectTableItem> {
    public ClusterGroupTreeTableView() {
        super();
        this.editableProperty().set(true);

        TreeTableColumn<ClusterGroupSelectTableItem, String> nameColumn = new TreeTableColumn<>("Name");
        TreeTableColumn<ClusterGroupSelectTableItem, Boolean> addColumn = new TreeTableColumn<>("Add");
        TreeTableColumn<ClusterGroupSelectTableItem, Integer> sizeColumn = new TreeTableColumn<>("Size");

        this.getColumns().add(nameColumn);
        this.getColumns().add(addColumn);
        this.getColumns().add(sizeColumn);

        nameColumn.setCellValueFactory((item) -> item.getValue().getValue().name);
        addColumn.setCellValueFactory((item) -> item.getValue().getValue().isAdded);
        sizeColumn.setCellValueFactory((item) -> item.getValue().getValue().size.asObject());

        addColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(addColumn));

        sizeColumn.editableProperty().set(false);
    }
}
