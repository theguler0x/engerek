/*
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.web.component.assignment;

import com.evolveum.midpoint.web.component.data.ObjectDataProvider;
import com.evolveum.midpoint.web.component.data.TablePanel;
import com.evolveum.midpoint.web.component.data.column.LinkColumn;
import com.evolveum.midpoint.web.component.util.BasePanel;
import com.evolveum.midpoint.web.component.util.SelectableBean;
import com.evolveum.midpoint.web.page.PageBase;
import com.evolveum.midpoint.xml.ns._public.common.common_2.ResourceType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.IModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lazyman
 */
public class ResourceListPanel extends BasePanel {

    public ResourceListPanel(String id) {
        super(id, null);
    }

    protected void initLayout() {
        TablePanel resources = new TablePanel("table", new ObjectDataProvider((PageBase) getPage(),
                ResourceType.class), initColumns());
        resources.setOutputMarkupId(true);
        add(resources);
    }

    private List<IColumn> initColumns() {
        List<IColumn> columns = new ArrayList<IColumn>();

        IColumn column = new LinkColumn<SelectableBean<ResourceType>>(createStringResource("ObjectType.name"), "name",
                "value.name") {

            @Override
            public void onClick(AjaxRequestTarget target, IModel<SelectableBean<ResourceType>> rowModel) {
                ResourceType resource = rowModel.getObject().getValue();
                resourceSelectedPerformed(target, resource);
            }
        };
        columns.add(column);


        return columns;
    }

    public void resourceSelectedPerformed(AjaxRequestTarget target, ResourceType resource) {

    }
}
