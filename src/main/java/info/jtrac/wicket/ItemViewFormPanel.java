/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.jtrac.wicket;

import info.jtrac.domain.History;
import info.jtrac.domain.Item;
import info.jtrac.domain.ItemSearch;
import info.jtrac.domain.ItemUser;
import info.jtrac.domain.Space;
import info.jtrac.domain.State;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import info.jtrac.util.UserUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.BoundCompoundPropertyModel;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.model.Model;

/**
 * Form to update history for item
 */
public class ItemViewFormPanel extends BasePanel {
    
    private JtracFeedbackMessageFilter filter;
    private ItemSearch itemSearch;
    
    public ItemViewFormPanel(String id, Item item, ItemSearch itemSearch) {
        super(id);
        this.itemSearch = itemSearch;
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        filter = new JtracFeedbackMessageFilter();
        feedback.setFilter(filter);
        add(feedback);
        add(new ItemViewForm("form", item));
    }
    
    /**
     * wicket form
     */    
    private class ItemViewForm extends Form {
        
        private FileUploadField fileUploadField;
        private long itemId;
        private DropDownChoice assignedToChoice;
        private DropDownChoice statusChoice;
        
        public ItemViewForm(String id, final Item item) {
            super(id);
            setMultiPart(true);
            this.itemId = item.getId();
            final History history = new History();
            history.setItemUsers(item.getItemUsers());
			history.fetchDefaultValue(item);
            final BoundCompoundPropertyModel model = new BoundCompoundPropertyModel(history);
            setModel(model);
            add(new TextArea("comment").setRequired(false).add(new ErrorHighlighter()));
            // custom fields ===================================================
            User user = getPrincipal();
            add(new CustomFieldsFormPanel("fields", model, item, user));
            // =================================================================
            final Space space = item.getSpace();
            final List<UserSpaceRole> userSpaceRoles = getJtrac().findUserRolesForSpace(space.getId());            
            // assigned to ===================================================== 
            final WebMarkupContainer border = new WebMarkupContainer("border");
            border.setOutputMarkupId(true);
            final WebMarkupContainer hide = new WebMarkupContainer("hide");
            border.add(hide);                       
            final List<User> emptyList = new ArrayList<User>(0);  // will be populated over Ajax
            assignedToChoice = new DropDownChoice("assignedTo", emptyList, new IChoiceRenderer() {
                public Object getDisplayValue(Object o) {
                    return ((User) o).getName();
                }
                public String getIdValue(Object o, int i) {
                    return ((User) o).getId() + "";
                }
            });
            assignedToChoice.setOutputMarkupId(true);
            assignedToChoice.setVisible(false);
            assignedToChoice.setNullValid(true);            
            border.add(new ErrorHighlighter(assignedToChoice));
            border.add(assignedToChoice);
            add(border);
            // status ==========================================================
            final Map<Integer, String> statesMap = item.getPermittedTransitions(user);
            List<Integer> states = new ArrayList(statesMap.keySet());
            statusChoice = new IndicatingDropDownChoice("status", states, new IChoiceRenderer() {
                public Object getDisplayValue(Object o) {
                    return statesMap.get(o);
                }
                public String getIdValue(Object o, int i) {
                    return o.toString();
                }
            });
            statusChoice.setNullValid(true);
            statusChoice.add(new ErrorHighlighter());
            statusChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {
                protected void onUpdate(AjaxRequestTarget target) {
                    Integer selectedStatus = (Integer) getFormComponent().getConvertedInput();
                    if (selectedStatus == null) {                        
                        assignedToChoice.setVisible(false);
                        hide.setVisible(true);
                    } else {
                        List<User> assignable = UserUtils.filterUsersAbleToTransitionFrom(userSpaceRoles, space, selectedStatus);
                        assignedToChoice.setChoices(assignable);
                        assignedToChoice.setVisible(true);
                        hide.setVisible(false);
                    }
                    target.addComponent(border);
                }
            });
            add(statusChoice);
            // notify list =====================================================
            List<ItemUser> choices = UserUtils.convertToItemUserList(userSpaceRoles);
            ListMultipleChoice itemUsers = new JtracCheckBoxMultipleChoice("itemUsers", choices, new IChoiceRenderer() {
                public Object getDisplayValue(Object o) {
                    return ((ItemUser) o).getUser().getName();
                }
                public String getIdValue(Object o, int i) {
                    return ((ItemUser) o).getUser().getId() + "";
                }
            }, true);
            add(itemUsers);
            // attachment ======================================================
            fileUploadField = new FileUploadField("file");
            add(fileUploadField);
            setMaxSize(Bytes.megabytes(getJtrac().getAttachmentMaxSizeInMb()));
            // send notifications===============================================
            CheckBox sendNotifications = new CheckBox("sendNotifications");
            add(sendNotifications);
            // validation that assignedTo is not null if status is not null and not CLOSED
            // have to use FormValidator because this is conditional validation across two FormComponents
            add(new AbstractFormValidator() {
                public FormComponent[] getDependentFormComponents() {
                    // actually we depend on assignedToChoice also, but wicket logs a warning when the
                    // component is not visible but we are doing ajax.  anyway we use assignedToChoice.getInput()
                    // not assignedToChoice.convertedInput() so no danger there
                    return new FormComponent[] {statusChoice};
                }
                public void validate(Form unused) {
                    if(assignedToChoice.getInput() == null || assignedToChoice.getInput().trim().length() == 0) {
                        Integer i = (Integer) statusChoice.getConvertedInput();
                        if (i != null && i != State.CLOSED) {
                            // user may have customized the name of the CLOSED State e.g. for i18n
                            // so when reporting the error, use the display name
                            String closedDisplayName = space.getMetadata().getStatusValue(State.CLOSED);
                            assignedToChoice.error(localize("item_view_form.assignedTo.error", closedDisplayName));
                        }
                    }
                }
            });
        }
        
        @Override
        protected void validate() {
            filter.reset();
            super.validate();
        }
        
        @Override
        protected void onSubmit() {
            final FileUpload fileUpload = fileUploadField.getFileUpload();
            History history = (History) getModelObject();
            User user = JtracSession.get().getUser();
            history.setLoggedBy(user);
            getJtrac().storeHistoryForItem(itemId, history, fileUpload);
            setResponsePage(ItemViewPage.class, new PageParameters("0=" + history.getRefId()));
        }
        
    }

    
}
