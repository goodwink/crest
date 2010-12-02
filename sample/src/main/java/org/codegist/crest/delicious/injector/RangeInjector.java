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

package org.codegist.crest.delicious.injector;

import org.codegist.crest.HttpRequest;
import org.codegist.crest.ParamContext;
import org.codegist.crest.delicious.model.Range;
import org.codegist.crest.injector.Injector;

public class RangeInjector implements Injector<Range> {
    @Override
    public void inject(HttpRequest.Builder builder, ParamContext<Range> context) {
        if(context.getRawValue() == null) return;
        Range r = context.getRawValue();
        builder.addQueryParam("start", String.valueOf(r.getStart()));
        builder.addQueryParam("results", String.valueOf(r.getResults()));
    }
}
