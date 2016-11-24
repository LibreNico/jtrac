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
import java.util.Set;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.ValidationErrorFeedback;

/**
 * custom feedback message filter, removes duplicates and works in conjunction
 * with the ErrorHighlighter form component behavior
 */
public class JtracFeedbackMessageFilter implements IFeedbackMessageFilter {
    
    private Set<String> previous = new HashSet<String>();
    
    public void reset() {
        previous.clear();
    }
    
    public boolean accept(FeedbackMessage fm) {
        String message = null;
        // wicket bit too flexible, wicket internally created errors are not just Strings
        // but if you added an error using the error(String) signature - will be just String
        if(fm.getMessage() instanceof String) {
            message = (String) fm.getMessage();
        } else {
            ValidationErrorFeedback error = (ValidationErrorFeedback) fm.getMessage();
            message = error.getMessage();
        }        
        if(!previous.contains(message)) {
            previous.add(message);
            return true;
        }
        return false;
    }
    
}
