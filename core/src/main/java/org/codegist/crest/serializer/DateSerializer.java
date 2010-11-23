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

package org.codegist.crest.serializer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class DateSerializer implements Serializer<Date> {

    /**
     * Use this parameter in the {@link org.codegist.crest.CRestContext#getCustomProperties()} to format the date with the given date format.
     * <p>Expects a date format string.
     */
    public static final String DATEFORMAT_PROP = DateSerializer.class.getName() + "#date-format";

    /**
     * Use this parameter in the {@link org.codegist.crest.CRestContext#getCustomProperties()} to format the date as a time unit from January 1, 1970, 00:00:00 GMT to this date.
     * <p>Expects a enum from {@link org.codegist.crest.serializer.DateSerializer.FormatType}.
     */
    public static final String DATEFORMAT_TYPE_PROP = DateSerializer.class.getName() + "#format";

    private final DateFormat formatter;
    private final FormatType formatType;

    public DateSerializer() {
        this(FormatType.Millis);
    }
    public DateSerializer(FormatType type) {
        this.formatter = null;
        this.formatType = type;
    }
    public DateSerializer(String dateFormat) {
        this(new SimpleDateFormat(dateFormat));
    }
    public DateSerializer(DateFormat formatter) {
        this.formatter = formatter;
        this.formatType = null;
    }

    public String serialize(Date value) {
        if(value == null) return "";
        if(formatter != null) {
             synchronized (formatter) {
                 return formatter.format(value);
             }
        }else{
            return String.valueOf(formatType.format(value));
        }
    }


    public enum FormatType {
        Millis(1),
        Second(1000),
        Minutes(1000*60),
        Hours(1000*60*60),
        Days(1000*60*60*24);
        private final long div;

        FormatType(long div) {
            this.div = div;
        }

        long format(Date date) {
            return date.getTime() / div;
        }
    }
}
