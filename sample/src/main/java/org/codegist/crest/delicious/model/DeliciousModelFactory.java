/*
 * Copyright 2010 CodeGist.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * ===================================================================
 *
 * More information at http://www.codegist.org.
 */

package org.codegist.crest.delicious.model;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class DeliciousModelFactory {
    public Posts createPosts(){
        return new Posts();
    }
    public Post createPost(){
        return new Post();
    }
    public Update createUpdate(){
        return new Update();
    }
    public Dates createDates(){
        return new Dates();
    }
    public Date createDate(){
        return new Date();
    }
    public Suggest createSuggest(){
        return new Suggest();
    }
    public Suggest.Network createSuggestNetwork(){
        return new Suggest.Network();
    }
    public Suggest.Popular createSuggestPopular(){
        return new Suggest.Popular();
    }
    public Suggest.Recommended createSuggestRecommanded(){
        return new Suggest.Recommended();
    }
}
