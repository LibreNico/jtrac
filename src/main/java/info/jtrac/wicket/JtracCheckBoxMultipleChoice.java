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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.util.string.Strings;

/**
 * custom multo select list / check box control that
 * is scrollable and selected entries "float" to the top
 */
public class JtracCheckBoxMultipleChoice extends ListMultipleChoice {
    
    private boolean isForSet;
    
    public JtracCheckBoxMultipleChoice(String id, List choices, IChoiceRenderer renderer) {
        super(id, choices, renderer);
    }
    
    public JtracCheckBoxMultipleChoice(String id, List choices, IChoiceRenderer renderer, boolean isForSet) {
        super(id, choices, renderer);
        this.isForSet = isForSet;
    }
    
    @Override
    protected java.lang.Object convertValue(String[] ids) {
        List list = (List) super.convertValue(ids);
        if(isForSet) {
            return new HashSet(list);
        } else {
            return list;
        }
    }
    
    /**
     * code adapted from onComponentTagBody implementation of wicket's built-in
     * CheckBoxMultipleChoice component
     */
    @Override
    protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
        
        final List choices = getChoices();
        boolean scrollable = choices.size() > 6;
        
        final StringBuilder buffer = new StringBuilder();
        final StringBuilder selectedBuffer = new StringBuilder();
        
        if(scrollable) {
            selectedBuffer.append("<div class=\"multiselect scrollable\">");
        } else {
            selectedBuffer.append("<div class=\"multiselect\">");
        }
        
        final String selected = getValue();
        
        boolean hasSelected = false;
        
        Locale locale = getLocale();
        
        for (int index = 0; index < choices.size(); index++) {
            
            final Object choice = choices.get(index);
            
            final String label = getConverter(String.class).convertToString(getChoiceRenderer().getDisplayValue(choice), locale);
            
            if (label != null) {
                
                String id = getChoiceRenderer().getIdValue(choice, index);
                final String idAttr = getInputName() + "_" + id;
                
                String display = label;
                if (localizeDisplayValues()) {
                    display = getLocalizer().getString(label, this, label);
                }
                CharSequence escaped = Strings.escapeMarkup(display, false, true);
                boolean isSelected = false;
                StringBuilder whichBuffer = buffer;                
                if(isSelected(choice, index, selected)) {
                    isSelected = true;                    
                    if(scrollable) {
                        hasSelected = true;
                        whichBuffer = selectedBuffer;
                    }
                }
                whichBuffer.append("<input name=\"").append(getInputName()).append("\"").append(" type=\"checkbox\"")
                            .append(isSelected ? " checked=\"checked\"" : "")
                            .append((isEnabled() ? "" : " disabled=\"disabled\"")).append(" value=\"")
                            .append(id).append("\" id=\"").append(idAttr).append("\"/>").append("<label for=\"")
                            .append(idAttr).append("\">").append(escaped).append("</label>").append("<br/>");
            }
        }
        
        if(hasSelected) {
            selectedBuffer.append("<hr/>");
        }
        
        selectedBuffer.append(buffer).append("</div>");
        
        replaceComponentTagBody(markupStream, openTag, selectedBuffer);
        
    }
    
    
}
