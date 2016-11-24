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
import java.io.Serializable;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.BoundCompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;

/**
 * space field option edit form
 */
public class SpaceFieldOptionPage extends BasePage {
      
    private WebPage previous;
    private Space space;
    
    private void addComponents(Field field, String key) {                
        add(new SpaceFieldOptionForm("form", field, key));
    }     
    
    public SpaceFieldOptionPage(Space space, Field field, String key, WebPage previous) {
        this.space = space;
        this.previous = previous;
        addComponents(field, key);
    }
    
    /**
     * wicket form
     */     
    private class SpaceFieldOptionForm extends Form {               
        
        private Field field;
        private String key;
        
        public SpaceFieldOptionForm(String id, final Field field, final String key) {
            
            super(id);          
            add(new FeedbackPanel("feedback"));
            
            this.field = field;
            this.key = key;
            
            SpaceFieldOptionModel modelObject = new SpaceFieldOptionModel();
            modelObject.setOption(field.getCustomValue(key));
            final BoundCompoundPropertyModel model = new BoundCompoundPropertyModel(modelObject);
            setModel(model);
            
            // delete ==========================================================
            Button delete = new Button("delete") {
                @Override
                public void onSubmit() {
                    int affectedCount = getJtrac().loadCountOfRecordsHavingFieldWithValue(space, field, Integer.parseInt(key));
                    if (affectedCount > 0) {
                        String heading = localize("space_field_option_delete.confirm") + " : " + field.getCustomValue(key) 
                            + " [" + field.getLabel() + "]";
                        String warning = localize("space_field_option_delete.line3");
                        String line1 = localize("space_field_option_delete.line1");
                        String line2 = localize("space_field_option_delete.line2", affectedCount + "");                        
                        ConfirmPage confirm = new ConfirmPage(SpaceFieldOptionPage.this, heading, warning, new String[] {line1, line2}) {
                            public void onConfirm() {
                                field.getOptions().remove(key);        
                                getJtrac().bulkUpdateFieldToNullForValue(space, field, Integer.parseInt(key));
                                // database has been updated, if we don't do this
                                // user may leave without committing metadata change       
                                getJtrac().storeSpace(space);
                                // synchronize metadata else when we save again we get Stale Object Exception
                                space.setMetadata(getJtrac().loadMetadata(space.getMetadata().getId()));
                                setResponsePage(new SpaceFieldFormPage(space, field, previous));
                            }                        
                        };
                        setResponsePage(confirm);
                    } else {
                        // this is an unsaved space / field or there are no impacted items
                        field.getOptions().remove(key);
                        setResponsePage(new SpaceFieldFormPage(space, field, previous));
                    }
                }                
            };
            delete.setDefaultFormProcessing(false);
            add(delete);
            // heading label ===================================================
            add(new Label("label", new PropertyModel(field, "label")));
            // option ===========================================================
            final TextField option = new TextField("option");
            option.setRequired(true);
            option.add(new ErrorHighlighter());
            option.add(new AbstractValidator() {
                protected void onValidate(IValidatable v) {
                    String s = (String) v.getValue();
                    if(field.hasOption(s)) {
                        error(v);
                    }
                }
                @Override
                protected String resourceKey() {                    
                    return "space_field_option_edit.error.exists";
                }                
            });
            add(option);
            // cancel ==========================================================
            add(new Link("cancel") {
                public void onClick() {
                    setResponsePage(new SpaceFieldFormPage(space, field, previous));
                }                
            });            
        }
                
        @Override
        protected void onSubmit() {                    
            SpaceFieldOptionModel model = (SpaceFieldOptionModel) getModelObject();
            field.addOption(key, model.getOption());
            setResponsePage(new SpaceFieldFormPage(space, field, previous));
        }     
                        
    }        
        
    /**
     * custom form backing object that wraps Field
     * required for the create / edit use case
     */
    private class SpaceFieldOptionModel implements Serializable {
                
        private String option;

        public String getOption() {
            return option;
        }

        public void setOption(String option) {
            this.option = option;
        }
               
    }
    
}
