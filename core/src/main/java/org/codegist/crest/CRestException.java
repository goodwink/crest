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

package org.codegist.crest;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class CRestException extends RuntimeException {
    public CRestException() {
        super();
    }

    public CRestException(String message) {
        super(message);
    }

    public CRestException(String message, Throwable cause) {
        super(message, cause);
    }

    public CRestException(Throwable cause) {
        super(cause);
    }

    static CRestException transform(Exception e) {
        if (e instanceof CRestException) {
            return (CRestException) e;
        } else {
            return new CRestException(e);
        }
    }
}
