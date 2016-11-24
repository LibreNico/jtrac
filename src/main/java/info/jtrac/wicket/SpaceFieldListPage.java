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

import info.jtrac.domain.Field;
import info.jtrac.domain.Space;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
 * space fields add / re-order page
 */
public class SpaceFieldListPage extends BasePage {              
    
    private Space space;
    private WebPage previous;        
    
    public SpaceFieldListPage(Space space, String selectedFieldName, WebPage previous) {
        this.previous = previous;
        this.space = space;
        add(new SpaceFieldsForm("form", space, selectedFieldName));        
    }
    
    /**
     * wicket form
     */     
    private class SpaceFieldsForm extends Form {        
        
        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }          
        
        public SpaceFieldsForm(String id, final Space space, final String selectedFieldName) {
            super(id);
            
            final BoundCompoundPropertyModel model = new BoundCompoundPropertyModel(this);
            setModel(model);
            
            add(new Label("name", new PropertyModel(space, "name")));
            add(new Label("prefixCode", new PropertyModel(space, "prefixCode")));
            
            final SimpleAttributeModifier sam = new SimpleAttributeModifier("class", "alt");
            
            ListView listView = new ListView("fields", space.getMetadata().getFieldList()) {
                protected void populateItem(ListItem listItem) {
                    final Field field = (Field) listItem.getModelObject();
                    
                    if (field.getName().getText().equals(selectedFieldName)) {
                        listItem.add(new SimpleAttributeModifier("class", "selected"));
                    } else if(listItem.getIndex() % 2 == 1) {
                        listItem.add(sam);
                    }                     
                    
                    listItem.add(new Button("up") {
                        @Override
                        public void onSubmit() {    
                            List<Field.Name> fieldOrder = space.getMetadata().getFieldOrder();
                            int index = fieldOrder.indexOf(field.getName());
                            int swapIndex = index - 1;
                            if (swapIndex < 0) {
                                if (fieldOrder.size() > 1) {        
                                    swapIndex = fieldOrder.size() - 1;
                                } else {
                                    swapIndex = 0;
                                }
                            }
                            if (index != swapIndex) {
                                Collections.swap(fieldOrder, index, swapIndex);
                                setResponsePage(new SpaceFieldListPage(space, field.getName().getText(), previous));
                            }                            
                        }                    
                    });
                    
                    listItem.add(new Button("down") {
                        @Override
                        public void onSubmit() {  
                            List<Field.Name> fieldOrder = space.getMetadata().getFieldOrder();
                            int index = fieldOrder.indexOf(field.getName());
                            int swapIndex = index + 1;
                            if (swapIndex == fieldOrder.size()) {
                                swapIndex = 0;
                            }
                            if (index != swapIndex) {
                                Collections.swap(fieldOrder, index, swapIndex);
                                setResponsePage(new SpaceFieldListPage(space, field.getName().getText(), previous));
                            }                            
                        }                        
                    });
                    
                    listItem.add(new Label("name", new PropertyModel(field, "name.text")));
                    listItem.add(new Label("type", new PropertyModel(field, "name.description")));                    
                    listItem.add(new Label("label", new PropertyModel(field, "label")));
                    List<String> optionsList;
                    if(field.getOptions() != null) {
                        optionsList = new ArrayList(field.getOptions().values());
                    } else {
                        optionsList = new ArrayList(0);
                    }
                    ListView options = new ListView("options", optionsList) {
                        protected void populateItem(ListItem item) {
                            item.add(new Label("option", item.getModelObject() + ""));
                        }                        
                    };
                    listItem.add(options);
                    listItem.add(new Button("edit") {
                        @Override
                        public void onSubmit() {
                            Field f = field.getClone();
                            setResponsePage(new SpaceFieldFormPage(space, f, previous));
                        }                        
                    });
                }                
            };            
            add(listView);
            
            final Map<String, String> types = space.getMetadata().getAvailableFieldTypes();
            List<String> typesList = new ArrayList(types.keySet());    
            // pre-select the drop down for convenience
            if(typesList.size() > 0) {
                type = typesList.get(0);
            }             
            
            DropDownChoice choice = new DropDownChoice("type", typesList, new IChoiceRenderer() {
                public Object getDisplayValue(Object o) {
                    return localize("space_fields.type_" + o) + " - " + localize("space_fields.typeRemaining", types.get(o));
                }
                public String getIdValue(Object o, int i) {
                    return o.toString();
                }
            });         
            
            add(choice);                      
            
            add(new Button("add") {
                @Override
                public void onSubmit() {                    
                    if(type == null) {
                        return;
                    }                    
                    Field field = space.getMetadata().getNextAvailableField(Integer.parseInt(type));
                    field.initOptions();
                    setResponsePage(new SpaceFieldFormPage(space, field, previous));          
                }                 
            });                      
            
            add(new Button("back") {
                @Override
                public void onSubmit() {
                    SpaceFormPage page = new SpaceFormPage(space);
                    page.setPrevious(previous);
                    setResponsePage(page);
                }           
            });
            
            add(new Button("next") {
                @Override
                public void onSubmit() {
                    setResponsePage(new SpacePermissionsPage(space, previous));
                }           
            });            
            
            add(new Link("cancel") {
                public void onClick() {
                    if(previous == null) {
                        setResponsePage(new OptionsPage());
                    } else {
                        if (previous instanceof SpaceListPage) {
                            ((SpaceListPage) previous).setSelectedSpaceId(space.getId());
                        }                      
                        setResponsePage(previous);
                    }                   
                }                
            });            
            
        }                              
        
    }        
        

    
}
