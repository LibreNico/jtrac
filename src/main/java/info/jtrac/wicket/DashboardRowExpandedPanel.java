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

import info.jtrac.domain.Counts;
import info.jtrac.domain.ItemSearch;
import info.jtrac.domain.Space;
import info.jtrac.domain.State;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;

/**
 * panel for expanded view of statistics for a single space 
 */
public class DashboardRowExpandedPanel extends BasePanel {    
    
    public DashboardRowExpandedPanel(String id, final UserSpaceRole usr, final Counts counts) {        
        
        super(id);
        setOutputMarkupId(true);
        
        final Space space = usr.getSpace();
        final User user = usr.getUser();
        
        final Map<Integer, String> states = new TreeMap(space.getMetadata().getStatesMap());    
        states.remove(State.NEW);
        int rowspan = states.size() + 1; // add one totals row also
        final SimpleAttributeModifier sam = new SimpleAttributeModifier("rowspan", rowspan + "");
        List<Integer> stateKeys = new ArrayList<Integer>(states.keySet());                                                
        
        add(new ListView("rows", stateKeys) {
            
            protected void populateItem(ListItem listItem) {                                
                
                if (listItem.getIndex() == 0) { // rowspan output only for first row            
                    
                    WebMarkupContainer spaceCell = new WebMarkupContainer("space");     
                    spaceCell.add(sam);
                    listItem.add(spaceCell);
                                      
                    spaceCell.add(new Label("name", space.getName()));
                    spaceCell.add(new Label("prefixCode", space.getPrefixCode()));
                    
                    WebMarkupContainer newColumn = new WebMarkupContainer("new");
                    newColumn.add(sam);   
                    listItem.add(newColumn);
                    
                    if(usr.isAbleToCreateNewItem()) {
                        newColumn.add(new Link("new") {
                            public void onClick() {
                                setCurrentSpace(space);
                                setResponsePage(ItemFormPage.class);
                            }
                        });

                    } else {
                        newColumn.add(new WebMarkupContainer("new").setVisible(false));
                    }

                    listItem.add(new Link("search") {
                        public void onClick() {
                            setCurrentSpace(space);
                            ItemSearch itemSearch = new ItemSearch(space);
                            setResponsePage(ItemSearchFormPage.class, itemSearch.getAsQueryString());
                        }
                    }.add(sam));

                    listItem.add(new IndicatingAjaxLink("link") {
                        public void onClick(AjaxRequestTarget target) {
                            DashboardRowPanel dashboardRow = new DashboardRowPanel("dashboardRow", usr, counts);
                            DashboardRowExpandedPanel.this.replaceWith(dashboardRow);
                            target.addComponent(dashboardRow);
                        }
                    }.add(sam)); 
                    
                } else {
                    listItem.add(new WebMarkupContainer("space").setVisible(false));
                    listItem.add(new WebMarkupContainer("new").setVisible(false));
                    listItem.add(new WebMarkupContainer("search").setVisible(false));
                    listItem.add(new WebMarkupContainer("link").setVisible(false));
                }
                
                final Integer i = (Integer) listItem.getModelObject();
                listItem.add(new Label("status", states.get(i)));
                
                if(user.getId() > 0) {                
                    listItem.add(new Link("loggedByMe") {
                        public void onClick() {
                            setCurrentSpace(space);
                            ItemSearch itemSearch = new ItemSearch(space);
                            itemSearch.setLoggedBy(user);
                            itemSearch.setStatus(i);                            
                            setResponsePage(ItemListPage.class, itemSearch.getAsQueryString());
                        }
                    }.add(new Label("loggedByMe", counts.getLoggedByMeForState(i))));

                    listItem.add(new Link("assignedToMe") {
                        public void onClick() {
                            setCurrentSpace(space);
                            ItemSearch itemSearch = new ItemSearch(space);
                            itemSearch.setAssignedTo(user);
                            itemSearch.setStatus(i);                            
                            setResponsePage(ItemListPage.class, itemSearch.getAsQueryString());
                        }
                    }.add(new Label("assignedToMe", counts.getAssignedToMeForState(i))));
                } else {
                    listItem.add(new WebMarkupContainer("loggedByMe").setVisible(false));
                    listItem.add(new WebMarkupContainer("assignedToMe").setVisible(false));                    
                }
                
                listItem.add(new Link("total") {
                    public void onClick() {
                        setCurrentSpace(space);
                        ItemSearch itemSearch = new ItemSearch(space);                        
                        itemSearch.setStatus(i);                        
                        setResponsePage(ItemListPage.class, itemSearch.getAsQueryString());
                    }
                }.add(new Label("total", counts.getTotalForState(i))));                
            }
            
        });
        
        // sub totals ==========================================================
        
        if(user.getId() > 0) {        
            add(new Link("loggedByMeTotal") {
                public void onClick() {
                    setCurrentSpace(space);
                    ItemSearch itemSearch = new ItemSearch(space);
                    itemSearch.setLoggedBy(user);                    
                    setResponsePage(ItemListPage.class, itemSearch.getAsQueryString());                                        
                }
            }.add(new Label("loggedByMe", new PropertyModel(counts, "loggedByMe"))));

            add(new Link("assignedToMeTotal") {
                public void onClick() {
                    setCurrentSpace(space);
                    ItemSearch itemSearch = new ItemSearch(space);
                    itemSearch.setAssignedTo(user);                    
                    setResponsePage(ItemListPage.class, itemSearch.getAsQueryString());
                }
            }.add(new Label("assignedToMe", new PropertyModel(counts, "assignedToMe"))));
        } else {
            add(new WebMarkupContainer("loggedByMeTotal").setVisible(false));
            add(new WebMarkupContainer("assignedToMeTotal").setVisible(false));               
        }
        
        add(new Link("totalTotal") {
            public void onClick() {
                setCurrentSpace(space);
                ItemSearch itemSearch = new ItemSearch(space);                
                setResponsePage(ItemListPage.class, itemSearch.getAsQueryString());                
            }
        }.add(new Label("total", new PropertyModel(counts, "total"))));
        
    }
    
}
