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

import info.jtrac.Jtrac;
import info.jtrac.domain.ColumnHeading.Name;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import java.util.EnumMap;
import java.util.Map;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * base class for all wicket pages, this provides
 * a way to access the spring managed service layer
 * as well as other convenience common methods
 * also takes care of the standard template for all
 * pages which is using wicket markup inheritance
 */
public abstract class BasePage extends WebPage {
    
    protected static final Logger logger = LoggerFactory.getLogger(BasePage.class);        
    
    // helper to avoid polluting non-wicket packages (e.g. excel export, import) with Wicket i18n
    public static Map<Name, String> getLocalizedLabels(Component c) {
        Map<Name, String> map = new EnumMap<Name, String>(Name.class);
        for(Name name : Name.values()) {
            map.put(name, c.getLocalizer().getString("item_list." + name.getText(), null));
        }
        return map;
    }     
    
    protected Jtrac getJtrac() {
        return JtracApplication.get().getJtrac();
    }          
    
    protected User getPrincipal() {
        return JtracSession.get().getUser();
    }
    
    protected void setCurrentSpace(Space space) {
        JtracSession.get().setCurrentSpace(space);
    }      
    
    protected Space getCurrentSpace() {
        return JtracSession.get().getCurrentSpace();
    }                
    
    protected String localize(String key) {
        return getLocalizer().getString(key, null);
    }
    
    protected String localize(String key, Object... params) {
        StringResourceModel m = new StringResourceModel(key, null, null, params);
        m.setLocalizer(getLocalizer());
        return m.getString();
    } 
            
    public BasePage() {        
        add(new IndividualHeadPanel().setRenderBodyOnly(true));
        add(new HeaderPanel().setRenderBodyOnly(true));
        String jtracVersion = getJtrac().getReleaseVersion();
        add(new Label("version", jtracVersion));
    }

}
