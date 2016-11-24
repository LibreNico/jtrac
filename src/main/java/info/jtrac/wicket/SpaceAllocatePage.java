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

import info.jtrac.domain.Role;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.BoundCompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;

/**
 * space allocate page
 */
public class SpaceAllocatePage extends BasePage {
    
    private WebPage previous;
    private long spaceId;
    private long selectedUserId;

    public void setSelectedUserId(long selectedUserId) {
        this.selectedUserId = selectedUserId;
    }
    
    public long getSpaceId() {
        return spaceId;
    }

    public WebPage getPrevious() {
        return previous;
    }    
    
    public SpaceAllocatePage(long spaceId, WebPage previous) {        
        this.spaceId = spaceId;
        this.previous = previous;        
        add(new SpaceAllocateForm("form"));
    }
    
    public SpaceAllocatePage(long spaceId, WebPage previous, long selectedUserId) {
        this.spaceId = spaceId;
        this.previous = previous;
        this.selectedUserId = selectedUserId;
        add(new SpaceAllocateForm("form"));        
    }    
    
    /**
     * wicket form
     */     
    private class SpaceAllocateForm extends Form {
                
        private Space space;
        private User user;        
                             
        private RoleAllocatePanel roleAllocatePanel;
        private Button allocateButton;  
        
        /**
         * function that attempts to pre-select roleKey for convenience
         * used on form init and also on Ajax onChange event for User choice
         */
        private void initRoleChoice(User u) {            
            List<String> roleKeys = space.getMetadata().getAllRoleKeys();            
            for(String s : u.getRoleKeys(space)) {                
                roleKeys.remove(s);
            } 
            // super user doesn't need space level option
            if(u.isSuperUser()) {
                roleKeys.remove(Role.ROLE_ADMIN);
            }
            roleAllocatePanel.setChoices(roleKeys);         
            allocateButton.setEnabled(true);            
        }        
                
        public SpaceAllocateForm(String id) {
            
            super(id);
                                    
            if(selectedUserId > 0) {
                // pre-select newly created user for convenience
                user = getJtrac().loadUser(selectedUserId);
            }
            
            final BoundCompoundPropertyModel model = new BoundCompoundPropertyModel(this);
            setModel(model);
            
            space = getJtrac().loadSpace(spaceId);
            
            add(new Label("label", space.getName() + " (" + space.getPrefixCode() + ")"));
            
            final Map<Long, List<UserSpaceRole>> userRolesMap = getJtrac().loadUserRolesMapForSpace(spaceId);
            
            final SimpleAttributeModifier sam = new SimpleAttributeModifier("class", "alt");
            
            add(new ListView("usrs", new ArrayList(userRolesMap.keySet())) {
                protected void populateItem(ListItem listItem) {
                    long userId = (Long) listItem.getModelObject();
                    final User user = userRolesMap.get(userId).get(0).getUser();
                    if(selectedUserId == userId) {
                        listItem.add(new SimpleAttributeModifier("class", "selected"));
                    } else if(listItem.getIndex() % 2 == 1) {
                        listItem.add(sam);
                    }                     
                    listItem.add(new Link("loginName") {
                        public void onClick() {                            
                            if(previous instanceof UserAllocatePage) { // prevent recursive stack buildup
                                previous = null;
                            }                               
                            setResponsePage(new UserAllocatePage(user.getId(), SpaceAllocatePage.this, spaceId));
                        }
                    }.add(new Label("loginName", new PropertyModel(user, "loginName"))));
                    listItem.add(new Label("name", new PropertyModel(user, "name")));
                    List<UserSpaceRole> usrs = userRolesMap.get(userId);
                    listItem.add(new RoleDeAllocatePanel("roleDeAllocatePanel", usrs));
                }
            });
            
            add(new Link("createNewUser") {
                @Override
                public void onClick() {
                    UserFormPage page = new UserFormPage();
                    page.setPrevious(SpaceAllocatePage.this);
                    setResponsePage(page);
                }
            });
            
            List<User> users = getJtrac().findUsersNotFullyAllocatedToSpace(spaceId);                      
            
            DropDownChoice userChoice = new DropDownChoice("user", users, new IChoiceRenderer() {
                public Object getDisplayValue(Object o) {
                    User u = (User) o;
                    return u.getName() + " (" + u.getLoginName() + ")";
                }
                public String getIdValue(Object o, int i) {
                    return ((User) o).getId() + "";
                }
            });
            userChoice.setNullValid(true);

            add(userChoice);
                        
            userChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {
                protected void onUpdate(AjaxRequestTarget target) {
                    User u = (User) getFormComponent().getConvertedInput();
                    if (u == null) {              
                        roleAllocatePanel.setChoices(new ArrayList<String>());
                        allocateButton.setEnabled(false);
                    } else {
                        User temp = getJtrac().loadUser(u.getId());
                        // populate choice, enable button etc
                        initRoleChoice(temp);
                    }
                    target.addComponent(roleAllocatePanel);
                    target.addComponent(allocateButton);
                }
            });             
            
            roleAllocatePanel = new RoleAllocatePanel("roleAllocatePanel");                    
            roleAllocatePanel.setOutputMarkupId(true);                                  
            add(roleAllocatePanel);
            
            allocateButton = new Button("allocate") {
                @Override
                public void onSubmit() {    
                    List<String> roleKeys = roleAllocatePanel.getSelected();
                    if(user == null || roleKeys.size() == 0) {
                        return;
                    }
                    // avoid lazy init problem
                    User temp = getJtrac().loadUser(user.getId());
                    for(String roleKey : roleKeys) {
                        getJtrac().storeUserSpaceRole(temp, space, roleKey);
                    }                                         
                    JtracSession.get().refreshPrincipalIfSameAs(temp);
                    setResponsePage(new SpaceAllocatePage(spaceId, previous, user.getId()));
                }
            };
            
            allocateButton.setOutputMarkupId(true);
            allocateButton.setEnabled(false);
            add(allocateButton);    
            
            if(users.size() == 1) {
                // pre select the user for convenience
                user = users.get(0);                
            }              
            
            if(user != null) {
                initRoleChoice(user);
            }
            
            // cancel ==========================================================
            add(new Link("cancel") {
                public void onClick() {
                    if(previous == null) {
                        setResponsePage(SpaceListPage.class);
                        return;
                    }
                    if(previous instanceof SpaceListPage) {
                        ((SpaceListPage) previous).setSelectedSpaceId(spaceId);
                    }
                    if(previous instanceof UserAllocatePage) {
                        ((UserAllocatePage) previous).setSelectedSpaceId(spaceId);
                    }                    
                    setResponsePage(previous);
                }
            });
        }
        
    }
    
    
    
}
