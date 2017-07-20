/*
 * Copyright (c) 2010-2017 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.web.page.admin.users.component;

import com.evolveum.midpoint.gui.api.component.BasePanel;
import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.gui.api.util.ModelServiceLocator;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.gui.api.util.WebModelServiceUtils;
import com.evolveum.midpoint.model.api.ModelAuthorizationAction;
import com.evolveum.midpoint.model.api.ModelExecuteOptions;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.delta.ContainerDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.OrgFilter;
import com.evolveum.midpoint.prism.query.OrgFilter.Scope;
import com.evolveum.midpoint.prism.query.builder.QueryBuilder;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.ObjectTypeUtil;
import com.evolveum.midpoint.security.api.AuthorizationConstants;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.*;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.data.column.ColumnMenuAction;
import com.evolveum.midpoint.web.component.dialog.ConfirmationPanel;
import com.evolveum.midpoint.web.component.menu.cog.InlineMenuItem;
import com.evolveum.midpoint.web.component.util.SelectableBean;
import com.evolveum.midpoint.web.page.admin.orgs.OrgTreeAssignablePanel;
import com.evolveum.midpoint.web.page.admin.orgs.OrgTreePanel;
import com.evolveum.midpoint.web.page.admin.users.PageOrgTree;
import com.evolveum.midpoint.web.page.admin.users.PageOrgUnit;
import com.evolveum.midpoint.web.util.OnePageParameterEncoder;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Used as a main component of the Org tree page.
 * 
 * todo create function computeHeight() in midpoint.js, update height properly
 * when in "mobile" mode... [lazyman] todo implement midpoint theme for tree
 * [lazyman]
 *
 * @author lazyman
 * @author katkav
 */
<<<<<<< HEAD
public class TreeTablePanel extends AbstractTreeTablePanel {

    private static final Trace LOGGER = TraceManager.getTrace(TreeTablePanel.class);

    public TreeTablePanel(String id, IModel<String> rootOid) {
        super(id, rootOid);
        
        selected = new LoadableModel<OrgTreeDto>() {
            @Override
            protected OrgTreeDto load() {
                TabbedPanel currentTabbedPanel = null;
                MidPointAuthWebSession session = TreeTablePanel.this.getSession();
                SessionStorage storage = session.getSessionStorage();
                if (getTree().findParent(TabbedPanel.class) != null) {
                    currentTabbedPanel = getTree().findParent(TabbedPanel.class);
                    int tabId = currentTabbedPanel.getSelectedTab();
                    if (storage.getUsers().getSelectedTabId() != -1 && tabId != storage.getUsers().getSelectedTabId()){
                        storage.getUsers().setSelectedItem(null);
                    }
                }
                if (storage.getUsers().getSelectedItem() != null){
                    return storage.getUsers().getSelectedItem();
                } else {
                    return getRootFromProvider();
                }
            }
        };
    }

    @Override
    protected void initLayout() {
        add(new ConfirmationDialog(ID_CONFIRM_DELETE_POPUP,
                createStringResource("TreeTablePanel.dialog.title.confirmDelete"), createDeleteConfirmString()) {

            @Override
            public void yesPerformed(AjaxRequestTarget target) {
                close(target);

                switch (getConfirmType()) {
                    case CONFIRM_DELETE:
                        deleteConfirmedPerformed(target);
                        break;
                    case CONFIRM_DELETE_ROOT:
                        deleteRootConfirmedPerformed(target);
                        break;
                }

            }
        });

        add(new OrgUnitBrowser(ID_MOVE_POPUP) {

            @Override
            protected void createRootPerformed(AjaxRequestTarget target) {
                moveConfirmedPerformed(target, null, null, Operation.MOVE);
            }

            @Override
            protected void rowSelected(AjaxRequestTarget target, IModel<OrgTableDto> row, Operation operation) {
                moveConfirmedPerformed(target, selected.getObject(), row.getObject(), operation);
            }

            @Override
            public ObjectQuery createRootQuery(){
                ArrayList<String> oids = new ArrayList<>();
                ObjectQuery query = new ObjectQuery();

                if(isMovingRoot() && getRootFromProvider() != null){
                    oids.add(getRootFromProvider().getOid());
                }

                if(oids.isEmpty()){
                    return null;
                }

                ObjectFilter oidFilter = InOidFilter.createInOid(oids);
                query.setFilter(NotFilter.createNot(oidFilter));

                return query;
            }
        });

        add(new OrgUnitAddDeletePopup(ID_ADD_DELETE_POPUP){

            @Override
            public void addPerformed(AjaxRequestTarget target, OrgType selected){
                addOrgUnitToUserPerformed(target, selected);
            }

            @Override
            public void removePerformed(AjaxRequestTarget target, OrgType selected){
                removeOrgUnitToUserPerformed(target, selected);
            }

            @Override
            public ObjectQuery getAddProviderQuery(){
                return null;
            }

            @Override
            public ObjectQuery getRemoveProviderQuery(){
                return null;
            }
        });

        WebMarkupContainer treeHeader = new WebMarkupContainer(ID_TREE_HEADER);
        treeHeader.setOutputMarkupId(true);
        add(treeHeader);

        InlineMenu treeMenu = new InlineMenu(ID_TREE_MENU, new Model<>((Serializable) createTreeMenu()));
        treeHeader.add(treeMenu);

        ISortableTreeProvider provider = new OrgTreeProvider(this, getModel());
        List<IColumn<OrgTreeDto, String>> columns = new ArrayList<>();
        columns.add(new TreeColumn<OrgTreeDto, String>(createStringResource("TreeTablePanel.hierarchy")));

        WebMarkupContainer treeContainer = new WebMarkupContainer(ID_TREE_CONTAINER) {

            @Override
            public void renderHead(IHeaderResponse response) {
                super.renderHead(response);

                //method computes height based on document.innerHeight() - screen height;
                response.render(OnDomReadyHeaderItem.forScript("updateHeight('" + getMarkupId()
                        + "', ['#" + TreeTablePanel.this.get(ID_FORM).getMarkupId() + "'], ['#"
                        + TreeTablePanel.this.get(ID_TREE_HEADER).getMarkupId() + "'])"));
            }
        };
        add(treeContainer);

        TableTree<OrgTreeDto, String> tree = new TableTree<OrgTreeDto, String>(ID_TREE, columns, provider,
                Integer.MAX_VALUE, new TreeStateModel(this, provider)) {

            @Override
            protected Component newContentComponent(String id, IModel<OrgTreeDto> model) {
                return new SelectableFolderContent(id, this, model, selected) {

                    @Override
                    protected void onClick(AjaxRequestTarget target) {
                        super.onClick(target);

                        MidPointAuthWebSession session = TreeTablePanel.this.getSession();
                        SessionStorage storage = session.getSessionStorage();
                        storage.getUsers().setSelectedItem(selected.getObject());

                        selectTreeItemPerformed(target);
                    }
                };
            }

            @Override
            protected Item<OrgTreeDto> newRowItem(String id, int index, final IModel<OrgTreeDto> model) {
                Item<OrgTreeDto> item = super.newRowItem(id, index, model);
                item.add(AttributeModifier.append("class", new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        OrgTreeDto itemObject = model.getObject();
                        if (itemObject != null && itemObject.equals(selected.getObject())) {
                            return "success";
                        }

                        return null;
                    }
                }));
                return item;
            }

            @Override
            public void collapse(OrgTreeDto collapsedItem){
                super.collapse(collapsedItem);
                MidPointAuthWebSession session = TreeTablePanel.this.getSession();
                SessionStorage storage = session.getSessionStorage();
                Set<OrgTreeDto> items  = storage.getUsers().getExpandedItems();
                if (items != null && items.contains(collapsedItem)){
                    items.remove(collapsedItem);
                }
                storage.getUsers().setExpandedItems((TreeStateSet)items);
                storage.getUsers().setCollapsedItem(collapsedItem);
            }


            @Override
            protected void onModelChanged() {
                super.onModelChanged();

                Set<OrgTreeDto> items = getModelObject();

                MidPointAuthWebSession session = TreeTablePanel.this.getSession();
                SessionStorage storage = session.getSessionStorage();
                storage.getUsers().setExpandedItems((TreeStateSet<OrgTreeDto>) items);
            }
        };
        tree.getTable().add(AttributeModifier.replace("class", "table table-striped table-condensed"));
        tree.add(new WindowsTheme());
//        tree.add(AttributeModifier.replace("class", "tree-midpoint"));
        treeContainer.add(tree);

        initTables();
        initSearch();
    }

    private void initTables() {
        Form form = new Form(ID_FORM);
        form.setOutputMarkupId(true);
        add(form);

        //Child org. units container initialization
        final ObjectDataProvider childTableProvider = new ObjectDataProvider<OrgTableDto, OrgType>(this, OrgType.class) {

            @Override
            public OrgTableDto createDataObjectWrapper(PrismObject<OrgType> obj) {
                return OrgTableDto.createDto(obj);
            }

            @Override
            public ObjectQuery getQuery() {
                return createOrgChildQuery();
            }
        };
        childTableProvider.setOptions(WebModelUtils.createMinimalOptions());

        WebMarkupContainer childOrgUnitContainer = new WebMarkupContainer(ID_CONTAINER_CHILD_ORGS);
        childOrgUnitContainer.setOutputMarkupId(true);
        childOrgUnitContainer.setOutputMarkupPlaceholderTag(true);
        form.add(childOrgUnitContainer);

        List<IColumn<OrgTableDto, String>> childTableColumns = createChildTableColumns();

        MidPointAuthWebSession session = getSession();
        SessionStorage storage = session.getSessionStorage();
        int pageSize = storage.getUserProfile().getPagingSize(UserProfileStorage.TableId.TREE_TABLE_PANEL_CHILD);

        final TablePanel childTable = new TablePanel<>(ID_CHILD_TABLE, childTableProvider, childTableColumns,
                UserProfileStorage.TableId.TREE_TABLE_PANEL_CHILD, pageSize);
        childTable.setOutputMarkupId(true);
        childTable.getNavigatorPanel().add(new VisibleEnableBehaviour(){

            @Override
            public boolean isVisible() {
                return childTableProvider.size() > childTable.getDataTable().getItemsPerPage();
            }
        });
        childOrgUnitContainer.add(childTable);

        //Manager container initialization
        final ObjectDataProvider managerTableProvider = new ObjectDataProvider<OrgTableDto, UserType>(this, UserType.class) {

            @Override
            public OrgTableDto createDataObjectWrapper(PrismObject<UserType> obj) {
                return OrgTableDto.createDto(obj);
            }

            @Override
            public ObjectQuery getQuery() {
                return createManagerTableQuery();
            }
        };
        managerTableProvider.setOptions(WebModelUtils.createMinimalOptions());

        WebMarkupContainer managerContainer = new WebMarkupContainer(ID_CONTAINER_MANAGER);
        managerContainer.setOutputMarkupId(true);
        managerContainer.setOutputMarkupPlaceholderTag(true);
        form.add(managerContainer);

        List<IColumn<OrgTableDto, String>> managerTableColumns = createUserTableColumns(true);
        final TablePanel managerTablePanel = new TablePanel<>(ID_MANAGER_TABLE, managerTableProvider, managerTableColumns,
                UserProfileStorage.TableId.TREE_TABLE_PANEL_MANAGER, UserProfileStorage.DEFAULT_PAGING_SIZE);
        managerTablePanel.setOutputMarkupId(true);
        managerTablePanel.getNavigatorPanel().add(new VisibleEnableBehaviour(){
            @Override
            public boolean isVisible() {
                return managerTableProvider.size() > managerTablePanel.getDataTable().getItemsPerPage();
            }
        });
        managerContainer.add(managerTablePanel);

        //Member container initialization
        final ObjectDataProvider memberTableProvider = new ObjectDataProvider<OrgTableDto, UserType>(this, UserType.class) {

            @Override
            public OrgTableDto createDataObjectWrapper(PrismObject<UserType> obj) {
                return OrgTableDto.createDto(obj);
            }

            @Override
            public ObjectQuery getQuery() {
                return createMemberQuery();
            }
        };
        memberTableProvider.setOptions(WebModelUtils.createMinimalOptions());

        WebMarkupContainer memberContainer = new WebMarkupContainer(ID_CONTAINER_MEMBER);
        memberContainer.setOutputMarkupId(true);
        memberContainer.setOutputMarkupPlaceholderTag(true);
        form.add(memberContainer);

        List<IColumn<OrgTableDto, String>> memberTableColumns = createUserTableColumns(false);
        final TablePanel memberTablePanel = new TablePanel<>(ID_MEMBER_TABLE, memberTableProvider, memberTableColumns,
                UserProfileStorage.TableId.TREE_TABLE_PANEL_MEMBER, UserProfileStorage.DEFAULT_PAGING_SIZE);
        memberTablePanel.setOutputMarkupId(true);
        memberTablePanel.getNavigatorPanel().add(new VisibleEnableBehaviour(){

            @Override
            public boolean isVisible() {
                return memberTableProvider.size() > memberTablePanel.getDataTable().getItemsPerPage();
            }
        });
        memberContainer.add(memberTablePanel);
    }

    private List<InlineMenuItem> createTreeMenu() {
        List<InlineMenuItem> items = new ArrayList<>();

        InlineMenuItem item = new InlineMenuItem(createStringResource("TreeTablePanel.collapseAll"),
                new InlineMenuItemAction() {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        collapseAllPerformed(target);
                    }
                });
        items.add(item);
        item = new InlineMenuItem(createStringResource("TreeTablePanel.expandAll"),
                new InlineMenuItemAction() {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        expandAllPerformed(target);
                    }
                });
        items.add(item);
        items.add(new InlineMenuItem());
        item = new InlineMenuItem(createStringResource("TreeTablePanel.moveRoot"), new InlineMenuItemAction() {

            @Override
            public void onClick(AjaxRequestTarget target) {
                moveRootPerformed(target);
            }
        });
        items.add(item);

        item = new InlineMenuItem(createStringResource("TreeTablePanel.deleteRoot"), new InlineMenuItemAction() {

            @Override
            public void onClick(AjaxRequestTarget target) {
                deleteRootPerformed(target);
            }
        });
        items.add(item);

        item = new InlineMenuItem(createStringResource("TreeTablePanel.recomputeRoot"), new InlineMenuItemAction() {

            @Override
            public void onClick(AjaxRequestTarget target) {
                recomputeRootPerformed(target, OrgUnitBrowser.Operation.RECOMPUTE);
            }
        });
        items.add(item);

        item = new InlineMenuItem(createStringResource("TreeTablePanel.editRoot"), new InlineMenuItemAction() {

            @Override
            public void onClick(AjaxRequestTarget target) {
                editRootPerformed(target);
            }
        });
        items.add(item);

        return items;
    }

    private IModel<String> createDeleteConfirmString() {
        return new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                ConfirmationDialog dialog = (ConfirmationDialog) TreeTablePanel.this.get(ID_CONFIRM_DELETE_POPUP);
                switch (dialog.getConfirmType()) {
                    case CONFIRM_DELETE:
                        return createStringResource("TreeTablePanel.message.deleteObjectConfirm",
                                WebMiscUtil.getSelectedData(getOrgChildTable()).size()).getString();
                    case CONFIRM_DELETE_ROOT:
                        OrgTreeDto dto = getRootFromProvider();

                        return createStringResource("TreeTablePanel.message.deleteRootConfirm",
                                dto.getName(), dto.getDisplayName()).getString();
                }
                return null;
            }
        };
    }

    private List<IColumn<OrgTableDto, String>> createChildTableColumns() {
        List<IColumn<OrgTableDto, String>> columns = new ArrayList<>();

        columns.add(new CheckBoxHeaderColumn<OrgTableDto>());
        columns.add(new IconColumn<OrgTableDto>(createStringResource("")) {

            @Override
            protected IModel<String> createIconModel(IModel<OrgTableDto> rowModel) {
                OrgTableDto dto = rowModel.getObject();
                ObjectTypeGuiDescriptor guiDescriptor = ObjectTypeGuiDescriptor.getDescriptor(dto.getType());

                String icon = guiDescriptor != null ? guiDescriptor.getIcon() : ObjectTypeGuiDescriptor.ERROR_ICON;

                return new Model<>(icon);
            }
        });

        columns.add(new LinkColumn<OrgTableDto>(createStringResource("ObjectType.name"), OrgTableDto.F_NAME, "name") {

            @Override
            public boolean isEnabled(IModel<OrgTableDto> rowModel) {
                OrgTableDto dto = rowModel.getObject();
                return UserType.class.equals(dto.getType()) || OrgType.class.equals(dto.getType());
            }

            @Override
            public void onClick(AjaxRequestTarget target, IModel<OrgTableDto> rowModel) {
                OrgTableDto dto = rowModel.getObject();
                PageParameters parameters = new PageParameters();
                parameters.add(OnePageParameterEncoder.PARAMETER, dto.getOid());
                getSession().getSessionStorage().setPreviousPage(PageOrgTree.class);
                setResponsePage(PageOrgUnit.class, parameters);
            }
        });
        columns.add(new PropertyColumn<OrgTableDto, String>(createStringResource("OrgType.displayName"), OrgTableDto.F_DISPLAY_NAME));
        columns.add(new PropertyColumn<OrgTableDto, String>(createStringResource("OrgType.identifier"), OrgTableDto.F_IDENTIFIER));
        columns.add(new InlineMenuHeaderColumn(initOrgChildInlineMenu()));

        return columns;
    }

    private List<IColumn<OrgTableDto, String>> createUserTableColumns(boolean isManagerTable) {
        List<IColumn<OrgTableDto, String>> columns = new ArrayList<>();

        columns.add(new CheckBoxHeaderColumn<OrgTableDto>());
        columns.add(new IconColumn<OrgTableDto>(createStringResource("")) {

            @Override
            protected IModel<String> createIconModel(IModel<OrgTableDto> rowModel) {
                OrgTableDto dto = rowModel.getObject();
                OrgTreeDto selectedDto = selected.getObject();
                String selectedOid = dto != null ? selectedDto.getOid() : getModel().getObject();

                ObjectTypeGuiDescriptor guiDescriptor = null;
                if(dto != null && dto.getRelation() == null) {
                    guiDescriptor = ObjectTypeGuiDescriptor.getDescriptor(dto.getType());
                } else {
                    if(dto != null){
                        for(ObjectReferenceType parentOrgRef: dto.getObject().getParentOrgRef()){
                            if(parentOrgRef.getOid().equals(selectedOid) && SchemaConstants.ORG_MANAGER.equals(parentOrgRef.getRelation())){
                                guiDescriptor = ObjectTypeGuiDescriptor.getDescriptor(dto.getRelation());
                                String icon = guiDescriptor != null ? guiDescriptor.getIcon() : ObjectTypeGuiDescriptor.ERROR_ICON;
                                return new Model<>(icon);
                            }
                        }

                        guiDescriptor = ObjectTypeGuiDescriptor.getDescriptor(dto.getType());
                    }
                }

                String icon = guiDescriptor != null ? guiDescriptor.getIcon() : ObjectTypeGuiDescriptor.ERROR_ICON;

                return new Model<>(icon);
            }
        });

        columns.add(new LinkColumn<OrgTableDto>(createStringResource("ObjectType.name"), OrgTableDto.F_NAME, "name") {

            @Override
            public boolean isEnabled(IModel<OrgTableDto> rowModel) {
                OrgTableDto dto = rowModel.getObject();
                return UserType.class.equals(dto.getType()) || OrgType.class.equals(dto.getType());
            }

            @Override
            public void onClick(AjaxRequestTarget target, IModel<OrgTableDto> rowModel) {
                OrgTableDto dto = rowModel.getObject();
                PageParameters parameters = new PageParameters();
                parameters.add(OnePageParameterEncoder.PARAMETER, dto.getOid());
                getSession().getSessionStorage().setPreviousPage(PageOrgTree.class);
                setResponsePage(new PageUser(parameters, (PageTemplate) target.getPage()));
            }
        });
        columns.add(new PropertyColumn<OrgTableDto, String>(createStringResource("UserType.givenName"),
                UserType.F_GIVEN_NAME.getLocalPart(), OrgTableDto.F_OBJECT + ".givenName"));
        columns.add(new PropertyColumn<OrgTableDto, String>(createStringResource("UserType.familyName"),
                UserType.F_FAMILY_NAME.getLocalPart(), OrgTableDto.F_OBJECT + ".familyName"));
        columns.add(new PropertyColumn<OrgTableDto, String>(createStringResource("UserType.fullName"),
                UserType.F_FULL_NAME.getLocalPart(), OrgTableDto.F_OBJECT + ".fullName"));
        columns.add(new PropertyColumn<OrgTableDto, String>(createStringResource("UserType.emailAddress"),
                null, OrgTableDto.F_OBJECT + ".emailAddress"));
        columns.add(new InlineMenuHeaderColumn(isManagerTable ? initOrgManagerInlineMenu() : initOrgMemberInlineMenu()));

        return columns;
    }

    private List<InlineMenuItem> initOrgChildInlineMenu() {
        List<InlineMenuItem> headerMenuItems = new ArrayList<>();
        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.addOrgUnit"), false,
                new HeaderMenuAction(this) {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        addOrgUnitPerformed(target);
                    }
                }));
        headerMenuItems.add(new InlineMenuItem());

        //TODO - disabled until issue MID-1809 is resolved. Uncomment when finished
//        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.addToHierarchy"), true,
//                new HeaderMenuAction(this) {
//
//                    @Override
//                    public void onSubmit(AjaxRequestTarget target, Form<?> form) {
//                        addToHierarchyPerformed(target);
//                    }
//                }));
//        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.removeFromHierarchy"), true,
//                new HeaderMenuAction(this) {
//
//                    @Override
//                    public void onSubmit(AjaxRequestTarget target, Form<?> form) {
//                        removeFromHierarchyPerformed(target);
//                    }
//                }));
        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.enable"), true,
                new HeaderMenuAction(this) {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        updateActivationPerformed(target, true);
                    }
                }));
        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.disable"), true,
                new HeaderMenuAction(this) {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        updateActivationPerformed(target, false);
                    }
                }));
        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.move"), true,
                new HeaderMenuAction(this) {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        movePerformed(target, OrgUnitBrowser.Operation.MOVE);
                    }
                }));
        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.delete"), true,
                new HeaderMenuAction(this) {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        deletePerformed(target);
                    }
                }));

        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel,menu.recompute"), true,
                new HeaderMenuAction(this) {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form){
                        recomputePerformed(target, OrgUnitBrowser.Operation.RECOMPUTE);
                    }
                }));

        return headerMenuItems;
    }

    private List<InlineMenuItem> initOrgMemberInlineMenu() {
        List<InlineMenuItem> headerMenuItems = new ArrayList<>();
        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.addMember"), false,
                new HeaderMenuAction(this) {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        addUserPerformed(target, false);
                    }
                }));
        headerMenuItems.add(new InlineMenuItem());

        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.enable"), true,
                new HeaderMenuAction(this) {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        updateActivationPerformed(target, true);
                    }
                }));
        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.disable"), true,
                new HeaderMenuAction(this) {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        updateActivationPerformed(target, false);
                    }
                }));
        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.delete"), true,
                new HeaderMenuAction(this) {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        deletePerformed(target);
                    }
                }));

        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel,menu.recompute"), true,
                new HeaderMenuAction(this) {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form){
                        recomputePerformed(target, OrgUnitBrowser.Operation.RECOMPUTE);
                    }
                }));

        return headerMenuItems;
    }

    private List<InlineMenuItem> initOrgManagerInlineMenu() {
        List<InlineMenuItem> headerMenuItems = new ArrayList<>();
        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.addManager"), false,
                new HeaderMenuAction(this) {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        addUserPerformed(target, true);
                    }
                }));
        headerMenuItems.add(new InlineMenuItem());

        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.enable"), true,
                new HeaderMenuAction(this) {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        updateActivationPerformed(target, true);
                    }
                }));
        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.disable"), true,
                new HeaderMenuAction(this) {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        updateActivationPerformed(target, false);
                    }
                }));
        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.delete"), true,
                new HeaderMenuAction(this) {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                        deletePerformed(target);
                    }
                }));

        headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel,menu.recompute"), true,
                new HeaderMenuAction(this) {

                    @Override
                    public void onSubmit(AjaxRequestTarget target, Form<?> form){
                        recomputePerformed(target, OrgUnitBrowser.Operation.RECOMPUTE);
                    }
                }));

        return headerMenuItems;
    }


    private void addOrgUnitPerformed(AjaxRequestTarget target) {
        PrismObject<OrgType> object = addChildOrgUnitPerformed(target, new OrgType());
        if (object == null) {
            return;
        }
        PageOrgUnit next = new PageOrgUnit(object);
        setResponsePage(next);
    }

    private void addUserPerformed(AjaxRequestTarget target, boolean isUserManager) {
        PrismObject object = addUserPerformed(target, new UserType(), isUserManager);
        if (object == null) {
            return;
        }
        PageUser next = new PageUser(object);
        setResponsePage(next);
    }

    private PrismObject<OrgType> addChildOrgUnitPerformed(AjaxRequestTarget target, OrgType org) {
        PageBase page = getPageBase();
        try {
        	ObjectReferenceType ref = WebMiscUtil.createObjectRef(selected.getObject().getOid(), selected.getObject().getName(), OrgType.COMPLEX_TYPE);
//            ObjectReferenceType ref = new ObjectReferenceType();
//            ref.setOid(selected.getObject().getOid());
//            ref.setType(OrgType.COMPLEX_TYPE);
//            ref.setTargetName(WebMiscUtil.createPolyFromOrigString(selected.getObject().getName()));
            org.getParentOrgRef().add(ref);
            AssignmentType newOrgAssignment = new AssignmentType();

            newOrgAssignment.setTargetRef(ref);
            org.getAssignment().add(newOrgAssignment);

            PrismContext context = page.getPrismContext();
            context.adopt(org);

            return org.asPrismContainer();
        } catch (Exception ex) {
            LoggingUtils.logException(LOGGER, "Couldn't create child org. unit with parent org. reference", ex);
            page.error("Couldn't create child org. unit with parent org. reference, reason: " + ex.getMessage());

            target.add(page.getFeedbackPanel());
        }

        return null;
    }

    private PrismObject<UserType> addUserPerformed(AjaxRequestTarget target, UserType user, boolean isUserManager) {
        PageBase page = getPageBase();
        try {
            ObjectReferenceType ref = new ObjectReferenceType();
            ref.setOid(selected.getObject().getOid());
            ref.setType(OrgType.COMPLEX_TYPE);

            if(isUserManager){
                ref.setRelation(SchemaConstants.ORG_MANAGER);
            }

            user.getParentOrgRef().add(ref.clone());

            AssignmentType assignment = new AssignmentType();
            assignment.setTargetRef(ref);

            user.getAssignment().add(assignment);

            PrismContext context = page.getPrismContext();
            context.adopt(user);

            return user.asPrismContainer();
        } catch (Exception ex) {
            LoggingUtils.logException(LOGGER, "Couldn't create user with parent org. reference", ex);
            page.error("Couldn't create user with parent org. reference, reason: " + ex.getMessage());

            target.add(page.getFeedbackPanel());
        }

        return null;
    }

    /**
     * This method check selection in table.
     */
    private List<OrgTableDto> isAnythingSelected(AjaxRequestTarget target) {
        List<OrgTableDto> objects = WebMiscUtil.getSelectedData(getOrgChildTable());
        if (objects.isEmpty()) {
            warn(getString("TreeTablePanel.message.nothingSelected"));
            target.add(getPageBase().getFeedbackPanel());
        }

        return objects;
    }

    private void deletePerformed(AjaxRequestTarget target) {
        List<OrgTableDto> objects = isAnythingSelected(target);
        if (objects.isEmpty()) {
            return;
        }

        ConfirmationDialog dialog = (ConfirmationDialog) get(ID_CONFIRM_DELETE_POPUP);
        dialog.setConfirmType(CONFIRM_DELETE);
        dialog.show(target);
    }

    private void deleteConfirmedPerformed(AjaxRequestTarget target) {
        List<OrgTableDto> objects = isAnythingSelected(target);
        if (objects.isEmpty()) {
            return;
        }

        PageBase page = getPageBase();
        OperationResult result = new OperationResult(OPERATION_DELETE_OBJECTS);
        for (OrgTableDto object : objects) {
            OperationResult subResult = result.createSubresult(OPERATION_DELETE_OBJECT);
            WebModelUtils.deleteObject(object.getType(), object.getOid(), subResult, page);
            subResult.computeStatusIfUnknown();

            MidPointAuthWebSession session = getSession();
            SessionStorage storage = session.getSessionStorage();
            storage.getUsers().setExpandedItems(null);
        }
        result.computeStatusComposite();

        page.showResult(result);
        target.add(page.getFeedbackPanel());
        target.add(getTree());

        refreshTable(target);
    }

    private void movePerformed(AjaxRequestTarget target, OrgUnitBrowser.Operation operation) {
        movePerformed(target, operation, null, false);
    }

    private void movePerformed(AjaxRequestTarget target, OrgUnitBrowser.Operation operation, OrgTableDto selected, boolean movingRoot) {
        List<OrgTableDto> objects;
        if (selected == null) {
            objects = isAnythingSelected(target);
            if (objects.isEmpty()) {
                return;
            }
        } else {
            objects = new ArrayList<>();
            objects.add(selected);
        }

        OrgUnitBrowser dialog = (OrgUnitBrowser) get(ID_MOVE_POPUP);
        dialog.setMovingRoot(movingRoot);
        dialog.setOperation(operation);
        dialog.setSelectedObjects(objects);
        dialog.show(target);
    }

    private ObjectDelta createMoveDelta(PrismObject<OrgType> orgUnit, OrgTreeDto oldParent, OrgTableDto newParent,
                                        OrgUnitBrowser.Operation operation) {
        ObjectDelta delta = orgUnit.createDelta(ChangeType.MODIFY);
        PrismReferenceDefinition refDef = orgUnit.getDefinition().findReferenceDefinition(OrgType.F_PARENT_ORG_REF);
        ReferenceDelta refDelta = delta.createReferenceModification(OrgType.F_PARENT_ORG_REF, refDef);
        PrismReferenceValue value;
        switch (operation) {
            case ADD:
                LOGGER.debug("Adding new parent {}", new Object[]{newParent});
                //adding parentRef newParent to orgUnit
                value = createPrismRefValue(newParent);
                refDelta.addValuesToAdd(value);
                break;
            case REMOVE:
                LOGGER.debug("Removing new parent {}", new Object[]{newParent});
                //removing parentRef newParent from orgUnit
                value = createPrismRefValue(newParent);
                refDelta.addValuesToDelete(value);
                break;
            case MOVE:
                if (oldParent == null && newParent == null) {
                    LOGGER.debug("Moving to root");
                    //moving orgUnit to root, removing all parentRefs
                    PrismReference ref = orgUnit.findReference(OrgType.F_PARENT_ORG_REF);
                    if (ref != null) {
                        for (PrismReferenceValue val : ref.getValues()) {
                            refDelta.addValuesToDelete(val.clone());
                        }
                    }
                } else {
                    LOGGER.debug("Adding new parent {}, removing old parent {}", new Object[]{newParent, oldParent});
                    //moving from old to new, removing oldParent adding newParent refs
                    value = createPrismRefValue(newParent);
                    refDelta.addValuesToAdd(value);

                    value = createPrismRefValue(oldParent);
                    refDelta.addValuesToDelete(value);
                }
                break;
        }

        return delta;
    }

    private void moveConfirmedPerformed(AjaxRequestTarget target, OrgTreeDto oldParent, OrgTableDto newParent,
                                        OrgUnitBrowser.Operation operation) {
        OrgUnitBrowser dialog = (OrgUnitBrowser) get(ID_MOVE_POPUP);
        List<OrgTableDto> objects = dialog.getSelected();

        PageBase page = getPageBase();
        ModelService model = page.getModelService();
        Task task = getPageBase().createSimpleTask(OPERATION_MOVE_OBJECTS);
        OperationResult result = task.getResult();
        for (OrgTableDto object : objects) {
            OperationResult subResult = result.createSubresult(OPERATION_MOVE_OBJECT);

            PrismObject<OrgType> orgUnit = WebModelUtils.loadObject(OrgType.class, object.getOid(),
                    WebModelUtils.createOptionsForParentOrgRefs(), getPageBase(), task, subResult);
            try {
                ObjectDelta delta = createMoveDelta(orgUnit, oldParent, newParent, operation);

                model.executeChanges(WebMiscUtil.createDeltaCollection(delta), null,
                        page.createSimpleTask(OPERATION_MOVE_OBJECT), subResult);
            } catch (Exception ex) {
                subResult.recordFatalError("Couldn't move object " + null + " to " + null + ".", ex);
                LoggingUtils.logException(LOGGER, "Couldn't move object {} to {}", ex, object.getName());
            } finally {
                subResult.computeStatusIfUnknown();
            }
        }
        result.computeStatusComposite();

        ObjectDataProvider provider = (ObjectDataProvider) getOrgChildTable().getDataTable().getDataProvider();
        provider.clearCache();

        page.showResult(result);
        dialog.close(target);

        refreshTabbedPanel(target);
    }

    private WebMarkupContainer getMemberContainer() {
        return (WebMarkupContainer) get(createComponentPath(ID_FORM, ID_CONTAINER_MEMBER));
    }

    private WebMarkupContainer getManagerContainer() {
        return (WebMarkupContainer) get(createComponentPath(ID_FORM, ID_CONTAINER_MANAGER));
    }

    private TablePanel getMemberTable() {
        return (TablePanel) get(createComponentPath(ID_FORM, ID_CONTAINER_MEMBER, ID_MEMBER_TABLE));
    }

    private TablePanel getManagerTable() {
        return (TablePanel) get(createComponentPath(ID_FORM, ID_CONTAINER_MANAGER, ID_MANAGER_TABLE));
    }

    private void selectTreeItemPerformed(AjaxRequestTarget target) {
        BasicSearchPanel<String> basicSearch = (BasicSearchPanel) get(createComponentPath(ID_SEARCH_FORM, ID_BASIC_SEARCH));
        basicSearch.getModel().setObject(null);

        TablePanel orgTable = getOrgChildTable();
        orgTable.setCurrentPage(null);

        TablePanel memberTable = getMemberTable();
        memberTable.setCurrentPage(null);

        TablePanel managerTable = getManagerTable();
        managerTable.setCurrentPage(null);

        target.add(getOrgChildContainer(), getMemberContainer(), getManagerContainer());
        target.add(get(ID_SEARCH_FORM));
    }

    
	private ObjectQuery createManagerTableQuery(){
        ObjectQuery query = null;
        OrgTreeDto dto = selected.getObject();
        String oid = dto != null ? dto.getOid() : getModel().getObject();

        BasicSearchPanel<String> basicSearch = (BasicSearchPanel) get(createComponentPath(ID_SEARCH_FORM, ID_BASIC_SEARCH));
        String object = basicSearch.getModelObject();

        SubstringFilter substring;
        PolyStringNormalizer normalizer = getPageBase().getPrismContext().getDefaultPolyStringNormalizer();
        String normalizedString = normalizer.normalize(object);

        if (StringUtils.isEmpty(normalizedString)) {
            substring = null;
        } else {
            substring =  SubstringFilter.createSubstring(ObjectType.F_NAME, ObjectType.class, getPageBase().getPrismContext(),
                    PolyStringNormMatchingRule.NAME, normalizedString);
        }
        
        DropDownChoice<String> searchScopeChoice = (DropDownChoice) get(createComponentPath(ID_SEARCH_FORM, ID_SEARCH_SCOPE));
        String scope = searchScopeChoice.getModelObject();

        try {
            OrgFilter org;
            if (substring == null || SEARCH_SCOPE_ONE.equals(scope)) {
            	org = OrgFilter.createOrg(oid, OrgFilter.Scope.ONE_LEVEL);
            } else {
            	org = OrgFilter.createOrg(oid, OrgFilter.Scope.SUBTREE);
            }


            PrismReferenceValue referenceValue = new PrismReferenceValue();
            referenceValue.setOid(oid);
            referenceValue.setRelation(SchemaConstants.ORG_MANAGER);
            RefFilter relationFilter = RefFilter.createReferenceEqual(new ItemPath(FocusType.F_PARENT_ORG_REF),
                    UserType.class, getPageBase().getPrismContext(), referenceValue);

            if(substring != null){
                query = ObjectQuery.createObjectQuery(AndFilter.createAnd(org, relationFilter, substring));
            } else {
                query = ObjectQuery.createObjectQuery(AndFilter.createAnd(org, relationFilter));
            }


        } catch (SchemaException e) {
            LoggingUtils.logException(LOGGER, "Couldn't prepare query for org. managers.", e);
        }

        return query;
    }

    private ObjectQuery createMemberQuery(){
        ObjectQuery query = null;
        OrgTreeDto dto = selected.getObject();
        String oid = dto != null ? dto.getOid() : getModel().getObject();

        BasicSearchPanel<String> basicSearch = (BasicSearchPanel) get(createComponentPath(ID_SEARCH_FORM, ID_BASIC_SEARCH));
        String object = basicSearch.getModelObject();

        SubstringFilter substring;
        PolyStringNormalizer normalizer = getPageBase().getPrismContext().getDefaultPolyStringNormalizer();
        String normalizedString = normalizer.normalize(object);

        if (StringUtils.isBlank(normalizedString)) {
            substring = null;
        } else {
            substring =  SubstringFilter.createSubstring(ObjectType.F_NAME, ObjectType.class, getPageBase().getPrismContext(),
                    PolyStringNormMatchingRule.NAME, normalizedString);
        }

        DropDownChoice<String> searchScopeChoice = (DropDownChoice) get(createComponentPath(ID_SEARCH_FORM, ID_SEARCH_SCOPE));
        String scope = searchScopeChoice.getModelObject();

        try {
            OrgFilter org;
            if (substring == null || SEARCH_SCOPE_ONE.equals(scope)) {
            	org = OrgFilter.createOrg(oid, OrgFilter.Scope.ONE_LEVEL);
            	
                PrismReferenceValue referenceFilter = new PrismReferenceValue();
                referenceFilter.setOid(oid);
                referenceFilter.setRelation(null);
                RefFilter referenceOidFilter = RefFilter.createReferenceEqual(new ItemPath(FocusType.F_PARENT_ORG_REF),
                        UserType.class, getPageBase().getPrismContext(), referenceFilter);
                
                if (substring != null){
                    query = ObjectQuery.createObjectQuery(AndFilter.createAnd(org, referenceOidFilter , substring));
                } else {
                    query = ObjectQuery.createObjectQuery(AndFilter.createAnd(org, referenceOidFilter));
                }
                
            } else {
            	
            	org = OrgFilter.createOrg(oid, OrgFilter.Scope.SUBTREE);
            	
                if (substring != null){
                    query = ObjectQuery.createObjectQuery(AndFilter.createAnd(org, substring));
                } else {
                    query = ObjectQuery.createObjectQuery(org);
                }
            }

            if(LOGGER.isTraceEnabled()){
                LOGGER.trace("Searching members of org {} with query:\n{}", oid, query.debugDump());
            }

        } catch (SchemaException e) {
            LoggingUtils.logException(LOGGER, "Couldn't prepare query for org. managers.", e);
        }

        return query;
    }

    private void collapseAllPerformed(AjaxRequestTarget target) {
        TableTree<OrgTreeDto, String> tree = getTree();
        TreeStateModel model = (TreeStateModel) tree.getDefaultModel();
        model.collapseAll();

        target.add(tree);
    }

    private void expandAllPerformed(AjaxRequestTarget target) {
        TableTree<OrgTreeDto, String> tree = getTree();
        TreeStateModel model = (TreeStateModel) tree.getDefaultModel();
        model.expandAll();

        target.add(tree);
    }

    private void moveRootPerformed(AjaxRequestTarget target) {
        OrgTreeDto root = getRootFromProvider();
        OrgTableDto dto = new OrgTableDto(root.getOid(), root.getType());
        movePerformed(target, OrgUnitBrowser.Operation.MOVE, dto, true);
    }

    private void updateActivationPerformed(AjaxRequestTarget target, boolean enable) {
        List<OrgTableDto> objects = isAnythingSelected(target);
        if (objects.isEmpty()) {
            return;
        }

        PageBase page = getPageBase();
        OperationResult result = new OperationResult(OPERATION_UPDATE_OBJECTS);
        for (OrgTableDto object : objects) {
            if (!(FocusType.class.isAssignableFrom(object.getType()))) {
                continue;
            }

            OperationResult subResult = result.createSubresult(OPERATION_UPDATE_OBJECT);
            ObjectDelta delta = WebModelUtils.createActivationAdminStatusDelta(object.getType(), object.getOid(),
                    enable, page.getPrismContext());

            WebModelUtils.save(delta, subResult, page);
        }
        result.computeStatusComposite();

        page.showResult(result);
        target.add(page.getFeedbackPanel());

        refreshTable(target);
    }

    @Override
    protected void refreshTable(AjaxRequestTarget target) {
        ObjectDataProvider orgProvider = (ObjectDataProvider) getOrgChildTable().getDataTable().getDataProvider();
        orgProvider.clearCache();

        ObjectDataProvider memberProvider = (ObjectDataProvider) getMemberTable().getDataTable().getDataProvider();
        memberProvider.clearCache();

        ObjectDataProvider managerProvider = (ObjectDataProvider) getManagerTable().getDataTable().getDataProvider();
        managerProvider.clearCache();

        target.add(getOrgChildContainer(), getMemberContainer(), getManagerContainer());
    }

    private void recomputeRootPerformed(AjaxRequestTarget target, OrgUnitBrowser.Operation operation){
        OrgTreeDto root = getRootFromProvider();
        OrgTableDto dto = new OrgTableDto(root.getOid(), root.getType());
        recomputePerformed(target, operation, dto);
    }

    private void recomputePerformed(AjaxRequestTarget target, OrgUnitBrowser.Operation operation){
        recomputePerformed(target, operation, null);
    }

    private void recomputePerformed(AjaxRequestTarget target, OrgUnitBrowser.Operation operation, OrgTableDto orgDto){
        List<OrgTableDto> objects;
        if (orgDto == null) {
            objects = isAnythingSelected(target);
            if (objects.isEmpty()) {
                return;
            }
        } else {
            objects = new ArrayList<>();
            objects.add(orgDto);
        }

        Task task = getPageBase().createSimpleTask(OPERATION_RECOMPUTE);
        OperationResult result = new OperationResult(OPERATION_RECOMPUTE);

        try {
            for(OrgTableDto org: objects){

                PrismObject<TaskType> recomputeTask = prepareRecomputeTask(org);

                ObjectDelta taskDelta = ObjectDelta.createAddDelta(recomputeTask);

                if(LOGGER.isTraceEnabled()){
                    LOGGER.trace(taskDelta.debugDump());
                }
=======
public class TreeTablePanel extends BasePanel<String> {

	private static final long serialVersionUID = 1L;
	private PageBase parentPage;

	@Override
	public PageBase getPageBase() {
		return parentPage;
	}

	protected static final String DOT_CLASS = TreeTablePanel.class.getName() + ".";
	protected static final String OPERATION_DELETE_OBJECTS = DOT_CLASS + "deleteObjects";
	protected static final String OPERATION_DELETE_OBJECT = DOT_CLASS + "deleteObject";
	protected static final String OPERATION_CHECK_PARENTS = DOT_CLASS + "checkParents";
	protected static final String OPERATION_MOVE_OBJECTS = DOT_CLASS + "moveObjects";
	protected static final String OPERATION_MOVE_OBJECT = DOT_CLASS + "moveObject";
	protected static final String OPERATION_UPDATE_OBJECTS = DOT_CLASS + "updateObjects";
	protected static final String OPERATION_UPDATE_OBJECT = DOT_CLASS + "updateObject";
	protected static final String OPERATION_RECOMPUTE = DOT_CLASS + "recompute";
	protected static final String OPERATION_SEARCH_MANAGERS = DOT_CLASS + "searchManagers";
	protected static final String OPERATION_COUNT_CHILDREN = DOT_CLASS + "countChildren";

	private static final String ID_TREE_PANEL = "treePanel";
	private static final String ID_MEMBER_PANEL = "memberPanel";

	private static final Trace LOGGER = TraceManager.getTrace(TreeTablePanel.class);

	public TreeTablePanel(String id, IModel<String> rootOid, PageBase parentPage) {
		super(id, rootOid);
		this.parentPage = parentPage;
		setParent(parentPage);
		initLayout(parentPage);
	}

	protected void initLayout(ModelServiceLocator serviceLocator) {

		OrgTreePanel treePanel = new OrgTreePanel(ID_TREE_PANEL, getModel(), false, serviceLocator) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void selectTreeItemPerformed(SelectableBean<OrgType> selected,
					AjaxRequestTarget target) {
				TreeTablePanel.this.selectTreeItemPerformed(selected, target);
			}

			protected List<InlineMenuItem> createTreeMenu() {
				return TreeTablePanel.this.createTreeMenu();
			}

			@Override
			protected List<InlineMenuItem> createTreeChildrenMenu(OrgType org) {
				return TreeTablePanel.this.createTreeChildrenMenu(org);
			}

		};
		treePanel.setOutputMarkupId(true);
		add(treePanel);
		add(createMemberPanel(treePanel.getSelected().getValue()));
		setOutputMarkupId(true);
	}

	private OrgMemberPanel createMemberPanel(OrgType org) {
		OrgMemberPanel memberPanel = new OrgMemberPanel(ID_MEMBER_PANEL, new Model<OrgType>(org), parentPage);
		memberPanel.setOutputMarkupId(true);
		return memberPanel;
	}

	private OrgTreePanel getTreePanel() {
		return (OrgTreePanel) get(ID_TREE_PANEL);
	}

	private List<InlineMenuItem> createTreeMenu() {
		List<InlineMenuItem> items = new ArrayList<>();
		return items;
	}

	private List<InlineMenuItem> createTreeChildrenMenu(OrgType org) {
		List<InlineMenuItem> items = new ArrayList<>();
		try {
			boolean allowModify = org == null ||
					// TODO: the modify authorization here is probably wrong.
					// It is a model autz. UI autz should be here instead?
					parentPage.getSecurityEnforcer().isAuthorized(ModelAuthorizationAction.MODIFY.getUrl(),
							AuthorizationPhaseType.REQUEST, org.asPrismObject(),
							null, null, null);
			boolean allowRead = org == null ||
					// TODO: the authorization URI here is probably wrong.
					// It is a model autz. UI autz should be here instead?
					parentPage.getSecurityEnforcer().isAuthorized(ModelAuthorizationAction.READ.getUrl(),
							AuthorizationPhaseType.REQUEST, org.asPrismObject(),
							null, null, null);
			InlineMenuItem item;
			if (allowModify) {
				item = new InlineMenuItem(createStringResource("TreeTablePanel.move"),
						new ColumnMenuAction<SelectableBean<OrgType>>() {
							private static final long serialVersionUID = 1L;

							@Override
							public void onClick(AjaxRequestTarget target) {
								moveRootPerformed(getRowModel().getObject(), target);
							}
						});
				items.add(item);

				item = new InlineMenuItem(createStringResource("TreeTablePanel.makeRoot"),
						new ColumnMenuAction<SelectableBean<OrgType>>() {
							private static final long serialVersionUID = 1L;

							@Override
							public void onClick(AjaxRequestTarget target) {
								makeRootPerformed(getRowModel().getObject(), target);
							}
						});
				items.add(item);
			}

			boolean allowDelete = org == null ||
					// TODO: the authorization URI here is probably wrong.
					// It is a model autz. UI autz should be here instead?
					parentPage.getSecurityEnforcer().isAuthorized(ModelAuthorizationAction.DELETE.getUrl(),
							AuthorizationPhaseType.REQUEST, org.asPrismObject(),
							null, null, null);
			if (allowDelete) {
				item = new InlineMenuItem(createStringResource("TreeTablePanel.delete"),
						new ColumnMenuAction<SelectableBean<OrgType>>() {
							private static final long serialVersionUID = 1L;

							@Override
							public void onClick(AjaxRequestTarget target) {
								deleteNodePerformed(getRowModel().getObject(), target);
							}
						});
				items.add(item);
			}
			if (allowModify) {
				item = new InlineMenuItem(createStringResource("TreeTablePanel.recompute"),
						new ColumnMenuAction<SelectableBean<OrgType>>() {
							private static final long serialVersionUID = 1L;

							@Override
							public void onClick(AjaxRequestTarget target) {
								recomputeRootPerformed(getRowModel().getObject(), target);
							}
						});
				items.add(item);

				item = new InlineMenuItem(createStringResource("TreeTablePanel.edit"), Model.of(allowModify), Model.of(allowModify),
						new ColumnMenuAction<SelectableBean<OrgType>>() {
							private static final long serialVersionUID = 1L;

							@Override
							public void onClick(AjaxRequestTarget target) {
								editRootPerformed(getRowModel().getObject(), target);
							}
						});
				items.add(item);
			} else if (allowRead){
				item = new InlineMenuItem(createStringResource("TreeTablePanel.viewDetails"), Model.of(allowRead), Model.of(allowRead),
						new ColumnMenuAction<SelectableBean<OrgType>>() {
							private static final long serialVersionUID = 1L;

							@Override
							public void onClick(AjaxRequestTarget target) {
								editRootPerformed(getRowModel().getObject(), target);
							}
						});
				items.add(item);
			}

			// TODO: the modify authorization here is probably wrong.
			// It is a model autz. UI autz should be here instead?
			boolean allowAddNew = parentPage.getSecurityEnforcer().isAuthorized(ModelAuthorizationAction.ADD.getUrl(),
					AuthorizationPhaseType.REQUEST, (new OrgType(parentPage.getPrismContext())).asPrismObject(),
					null, null, null);
			if (allowModify && allowAddNew) {
				item = new InlineMenuItem(createStringResource("TreeTablePanel.createChild"),
						new ColumnMenuAction<SelectableBean<OrgType>>() {
							private static final long serialVersionUID = 1L;

							@Override
							public void onClick(AjaxRequestTarget target) {
								try {
									initObjectForAdd(
											ObjectTypeUtil.createObjectRef(getRowModel().getObject().getValue()),
											OrgType.COMPLEX_TYPE, null, target);
								} catch (SchemaException e) {
									throw new SystemException(e.getMessage(), e);
								}
							}
						});
				items.add(item);
			}
		} catch (SchemaException ex){
			LoggingUtils.logUnexpectedException(LOGGER, "Failed to check menu items authorizations", ex);
		}
		return items;
	}

	// TODO: merge this with AbstractRoleMemeberPanel.initObjectForAdd, also see MID-3233
	private void initObjectForAdd(ObjectReferenceType parentOrgRef, QName type, QName relation,
			AjaxRequestTarget target) throws SchemaException {
		TreeTablePanel.this.getPageBase().hideMainPopup(target);
		PrismContext prismContext = TreeTablePanel.this.getPageBase().getPrismContext();
		PrismObjectDefinition def = prismContext.getSchemaRegistry().findObjectDefinitionByType(type);
		PrismObject obj = def.instantiate();

		ObjectType objType = (ObjectType) obj.asObjectable();
		if (FocusType.class.isAssignableFrom(obj.getCompileTimeClass())) {
			AssignmentType assignment = new AssignmentType();
			assignment.setTargetRef(parentOrgRef);
			((FocusType) objType).getAssignment().add(assignment);
		}

		// Set parentOrgRef in any case. This is not strictly correct.
		// The parentOrgRef should be added by the projector. But
		// this is needed to successfully pass through security
		// TODO: fix MID-3234
		if (parentOrgRef == null) {
			ObjectType org = getTreePanel().getSelected().getValue();
			parentOrgRef = ObjectTypeUtil.createObjectRef(org);
			parentOrgRef.setRelation(relation);
			objType.getParentOrgRef().add(parentOrgRef);
		} else {
			objType.getParentOrgRef().add(parentOrgRef.clone());
		}

		WebComponentUtil.dispatchToObjectDetailsPage(obj, this);

	}

	private void selectTreeItemPerformed(SelectableBean<OrgType> selected, AjaxRequestTarget target) {
		if (selected.getValue() == null) {
			return;
		}
		getTreePanel().setSelected(selected);
		target.add(addOrReplace(createMemberPanel(selected.getValue())));
	}

	private void moveRootPerformed(SelectableBean<OrgType> root, AjaxRequestTarget target) {
		if (root == null) {
			root = getTreePanel().getRootFromProvider();
		}

		final SelectableBean<OrgType> orgToMove = root;

		OrgTreeAssignablePanel orgAssignablePanel = new OrgTreeAssignablePanel(
				parentPage.getMainPopupBodyId(), false, parentPage) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onItemSelect(SelectableBean<OrgType> selected, AjaxRequestTarget target) {
				moveConfirmPerformed(orgToMove, selected, target);
			}
		};

		parentPage.showMainPopup(orgAssignablePanel, target);

	}

	private void moveConfirmPerformed(SelectableBean<OrgType> orgToMove, SelectableBean<OrgType> selected,
			AjaxRequestTarget target) {
		getPageBase().hideMainPopup(target);

		Task task = getPageBase().createSimpleTask(OPERATION_MOVE_OBJECT);
		OperationResult result = new OperationResult(OPERATION_MOVE_OBJECT);

		OrgType toMove = orgToMove.getValue();
		if (toMove == null || selected.getValue() == null) {
			return;
		}
		ObjectDelta<OrgType> moveOrgDelta = ObjectDelta.createEmptyModifyDelta(OrgType.class, toMove.getOid(),
				getPageBase().getPrismContext());

		try {
			for (OrgType parentOrg : toMove.getParentOrg()) {
				AssignmentType oldRoot = new AssignmentType();
				oldRoot.setTargetRef(ObjectTypeUtil.createObjectRef(parentOrg));

				moveOrgDelta.addModification(ContainerDelta.createModificationDelete(OrgType.F_ASSIGNMENT,
						OrgType.class, getPageBase().getPrismContext(), oldRoot.asPrismContainerValue()));
				// moveOrgDelta.addModification(ReferenceDelta.createModificationDelete(OrgType.F_PARENT_ORG_REF,
				// toMove.asPrismObject().getDefinition(),
				// ObjectTypeUtil.createObjectRef(parentOrg).asReferenceValue()));
			}

			AssignmentType newRoot = new AssignmentType();
			newRoot.setTargetRef(ObjectTypeUtil.createObjectRef(selected.getValue()));
			moveOrgDelta.addModification(ContainerDelta.createModificationAdd(OrgType.F_ASSIGNMENT,
					OrgType.class, getPageBase().getPrismContext(), newRoot.asPrismContainerValue()));
			// moveOrgDelta.addModification(ReferenceDelta.createModificationAdd(OrgType.F_PARENT_ORG_REF,
			// toMove.asPrismObject().getDefinition(),
			// ObjectTypeUtil.createObjectRef(selected.getValue()).asReferenceValue()));

			getPageBase().getPrismContext().adopt(moveOrgDelta);
			getPageBase().getModelService()
					.executeChanges(WebComponentUtil.createDeltaCollection(moveOrgDelta), null, task, result);
			result.computeStatus();
		} catch (ObjectAlreadyExistsException | ObjectNotFoundException | SchemaException
				| ExpressionEvaluationException | CommunicationException | ConfigurationException
				| PolicyViolationException | SecurityViolationException e) {
			result.recordFatalError("Failed to move organization unit " + toMove, e);
			LoggingUtils.logUnexpectedException(LOGGER, "Failed to move organization unit" + toMove, e);
		}

		parentPage.showResult(result);
		target.add(parentPage.getFeedbackPanel());
		setResponsePage(PageOrgTree.class);

	}

	private void makeRootPerformed(SelectableBean<OrgType> newRoot, AjaxRequestTarget target) {
		Task task = getPageBase().createSimpleTask(OPERATION_MOVE_OBJECT);
		OperationResult result = new OperationResult(OPERATION_MOVE_OBJECT);

		OrgType toMove = newRoot.getValue();
		if (toMove == null) {
			return;
		}
		ObjectDelta<OrgType> moveOrgDelta = ObjectDelta.createEmptyModifyDelta(OrgType.class, toMove.getOid(),
				getPageBase().getPrismContext());

		try {
			for (ObjectReferenceType parentOrg : toMove.getParentOrgRef()) {
				AssignmentType oldRoot = new AssignmentType();
				oldRoot.setTargetRef(parentOrg);

				moveOrgDelta.addModification(ContainerDelta.createModificationDelete(OrgType.F_ASSIGNMENT,
						OrgType.class, getPageBase().getPrismContext(), oldRoot.asPrismContainerValue()));
			}

			getPageBase().getPrismContext().adopt(moveOrgDelta);
			getPageBase().getModelService()
					.executeChanges(WebComponentUtil.createDeltaCollection(moveOrgDelta), null, task, result);
			result.computeStatus();
		} catch (ObjectAlreadyExistsException | ObjectNotFoundException | SchemaException
				| ExpressionEvaluationException | CommunicationException | ConfigurationException
				| PolicyViolationException | SecurityViolationException e) {
			result.recordFatalError("Failed to move organization unit " + toMove, e);
			LoggingUtils.logUnexpectedException(LOGGER, "Failed to move organization unit" + toMove, e);
		}

		parentPage.showResult(result);
		target.add(parentPage.getFeedbackPanel());
		// target.add(getTreePanel());
		setResponsePage(PageOrgTree.class);
	}

	private void recomputeRootPerformed(SelectableBean<OrgType> root, AjaxRequestTarget target) {
		if (root == null) {
			root = getTreePanel().getRootFromProvider();
		}

		recomputePerformed(root, target);
	}

	private void recomputePerformed(SelectableBean<OrgType> orgToRecompute, AjaxRequestTarget target) {

		Task task = getPageBase().createSimpleTask(OPERATION_RECOMPUTE);
		OperationResult result = new OperationResult(OPERATION_RECOMPUTE);
		if (orgToRecompute.getValue() == null) {
			return;
		}
		try {
			ObjectDelta emptyDelta = ObjectDelta.createEmptyModifyDelta(OrgType.class,
					orgToRecompute.getValue().getOid(), getPageBase().getPrismContext());
			ModelExecuteOptions options = new ModelExecuteOptions();
			options.setReconcile(true);
			getPageBase().getModelService().executeChanges(WebComponentUtil.createDeltaCollection(emptyDelta),
					options, task, result);

			result.recordSuccess();
		} catch (Exception e) {
			result.recordFatalError(getString("TreeTablePanel.message.recomputeError"), e);
			LoggingUtils.logUnexpectedException(LOGGER, getString("TreeTablePanel.message.recomputeError"), e);
		}

		getPageBase().showResult(result);
		target.add(getPageBase().getFeedbackPanel());
		getTreePanel().refreshTabbedPanel(target);
	}

	private void deleteNodePerformed(final SelectableBean<OrgType> orgToDelete, AjaxRequestTarget target) {

		ConfirmationPanel confirmationPanel = new ConfirmationPanel(getPageBase().getMainPopupBodyId(),
				new AbstractReadOnlyModel<String>() {

					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						if (hasChildren(orgToDelete)) {
							return createStringResource("TreeTablePanel.message.warn.deleteTreeObjectConfirm",
									WebComponentUtil.getEffectiveName(orgToDelete.getValue(),
											OrgType.F_DISPLAY_NAME)).getObject();
						}
						return createStringResource("TreeTablePanel.message.deleteTreeObjectConfirm",
								WebComponentUtil.getEffectiveName(orgToDelete.getValue(),
										OrgType.F_DISPLAY_NAME)).getObject();
					}
				}) {
			private static final long serialVersionUID = 1L;

			@Override
			public void yesPerformed(AjaxRequestTarget target) {
					deleteNodeConfirmedPerformed(orgToDelete, target);
			}
		};

		confirmationPanel.setOutputMarkupId(true);
		getPageBase().showMainPopup(confirmationPanel, target);
	}

	private boolean hasChildren(SelectableBean<OrgType> orgToDelete) {
		ObjectQuery query = QueryBuilder.queryFor(ObjectType.class, getPageBase().getPrismContext())
				.isChildOf(orgToDelete.getValue().getOid())			// TODO what if orgToDelete.getValue()==null
				.build();
		Task task = getPageBase().createSimpleTask(OPERATION_COUNT_CHILDREN);
		OperationResult result = new OperationResult(OPERATION_COUNT_CHILDREN);
		try {
			int count = getPageBase().getModelService().countObjects(ObjectType.class,
					query, null, task, result);
			return (count > 0);
		} catch (SchemaException | ObjectNotFoundException | SecurityViolationException
				| ConfigurationException | CommunicationException | ExpressionEvaluationException e) {
			LoggingUtils.logUnexpectedException(LOGGER, e.getMessage(), e);
			result.recordFatalError("Could not count members for org " + orgToDelete.getValue(), e);
			return false;
		}
	}


	private void deleteNodeConfirmedPerformed(SelectableBean<OrgType> orgToDelete, AjaxRequestTarget target) {
		getPageBase().hideMainPopup(target);
		OperationResult result = new OperationResult(OPERATION_DELETE_OBJECT);

		PageBase page = getPageBase();

		if (orgToDelete == null) {
			orgToDelete = getTreePanel().getRootFromProvider();
		}
		if (orgToDelete.getValue() == null) {
			return;
		}
		String oidToDelete = orgToDelete.getValue().getOid();
		WebModelServiceUtils.deleteObject(OrgType.class, oidToDelete, result, page);

		result.computeStatusIfUnknown();
		page.showResult(result);

		// even if we theoretically could refresh page only if non-leaf node is deleted,
		// for simplicity we do it each time
		//
		// Instruction to refresh only the part would be:
		//  - getTreePanel().refreshTabbedPanel(target);
		//
		// But how to refresh whole page? target.add(getPage()) is not sufficient - content is unchanged;
		// so we use the following.
		// TODO is this ok? [pmed]
		throw new RestartResponseException(getPage().getClass());
	}

	private void editRootPerformed(SelectableBean<OrgType> root, AjaxRequestTarget target) {
		if (root == null) {
			root = getTreePanel().getRootFromProvider();
		}
		if (root.getValue() == null) {
			return;
		}
		PageParameters parameters = new PageParameters();
		parameters.add(OnePageParameterEncoder.PARAMETER, root.getValue().getOid());
		getPageBase().navigateToNext(PageOrgUnit.class, parameters);
	}
>>>>>>> midpoint/master

}
