package org.codegist.crest;

import org.codegist.common.net.Urls;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Params {
    private Params() {
    }

    /**
     * Returns true if any of the given collection of object is considered for upload (File or InputStream)
     *
     * @param os Collection of objects
     * @return flag indicating if the given list contains values to upload
     */
    public static boolean isForUpload(Collection<Object> os) {
        for (Object o : os) {
            if (isForUpload(o)) return true;
        }
        return false;
    }

    /**
     * Returns true if any of the given params of object is considered for upload (File or InputStream)
     *
     * @param params Map of objects
     * @return flag indicating if the given map contains values to upload
     */
    public static boolean isForUpload(Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (isForUpload(entry.getValue())) return true;
        }
        return false;
    }

    /**
     * Returns true if the given object is considered for upload (File or InputStream)
     *
     * @param o Object to test
     * @return flag indicating if the given object is for upload
     */
    public static boolean isForUpload(Object o) {
        return o instanceof InputStream || o instanceof File;
    }

    /**
     * Encode the given parameter map into a string for HTTP body content
     *
     * @param params   Parameter map
     * @param encoding Encoding
     * @return the encoding representation string of the given parameter map
     * @throws UnsupportedEncodingException Encoding is not supported
     */
    public static String encodeParams(Map<String, Object> params, String encoding) throws UnsupportedEncodingException {
        Map<String, String> sparams = new LinkedHashMap<String, String>();
        for (Map.Entry<String, Object> p : params.entrySet()) {
            sparams.put(p.getKey(), p.getValue() != null ? p.getValue().toString() : null);
        }
        return Urls.buildQueryString(sparams, encoding);
    }
}
