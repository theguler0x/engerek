package com.evolveum.midpoint.web.component.data;

import com.evolveum.midpoint.web.component.util.SelectableBean;
import com.evolveum.midpoint.web.page.admin.resources.dto.ResourceDto;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

import java.util.List;

public class SelectableDataTable<T> extends DataTable<T, String> {

    public SelectableDataTable(String id, List<IColumn<T, String>> columns, IDataProvider<T> dataProvider, int rowsPerPage) {
        super(id, columns, dataProvider, rowsPerPage);
    }

    @Override
    protected Item<T> newRowItem(String id, int index, IModel<T> model) {
        final Item<T> rowItem = new Item<T>(id, index, model);

        rowItem.add(new AjaxEventBehavior("onclick") {

            @Override
            protected void onEvent(AjaxRequestTarget target) {

                String id = rowItem.getId();
                T object = rowItem.getModel().getObject();

                SelectableBean selectable = null;
                if (object instanceof SelectableBean<?>) {

                    selectable = (SelectableBean) object;
                }
                if (selectable == null) {
                    if (object instanceof ResourceDto) {
                        ResourceDto resource = (ResourceDto) object;
                        boolean enabled = !resource.isSigned();
                        resource.setSelected(enabled);
                        resource.setSigned(enabled);
                    }
                    return;
                }
                boolean enabled = !selectable.isSigned();
                ((SelectableBean) object).setSelected(enabled);
                ((SelectableBean) object).setSigned(enabled);

            }
        });

        rowItem.setOutputMarkupId(true);

        return rowItem;
    }
}
