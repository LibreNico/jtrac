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

package info.jtrac.wicket.yui;

import java.util.Map;

/**
 * utilities for doing some javascript stuff e.g. basic JSON serialization
 */
public class YuiUtils {
    
    /**
     * custom Map to JSON converter
     * TODO support values that should not be treated like strings (quoted)
     */
    public static String getJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean firstTime = true;
        for(Map.Entry entry : map.entrySet()) {
            if(firstTime) {
                firstTime = false;
            } else {
              sb.append(", ");
            }
            sb.append(entry.getKey());
            sb.append(" : ");
            Object value = entry.getValue();
            if(value instanceof Map) {
                sb.append(getJson((Map) value));
            } else if(entry.getValue() instanceof String) {
                sb.append("'" + value + "'");
            } else {
                sb.append(value);
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
}
