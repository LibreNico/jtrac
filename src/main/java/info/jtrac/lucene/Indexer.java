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

package info.jtrac.lucene;

import info.jtrac.domain.AbstractItem;
import org.springmodules.lucene.index.support.LuceneIndexSupport;

/**
 * Uses Spring Modules Lucene support, provides Lucene Indexing support
 * in classic Spring Template style
 */
public class Indexer extends LuceneIndexSupport {    
    
    public void index(AbstractItem item) {        
        getLuceneIndexTemplate().addDocument(item);
    }    
    
}
