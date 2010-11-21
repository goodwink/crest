package org.codegist.crest.serializer;

import org.codegist.common.lang.Strings;
import org.codegist.crest.ParamContext;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Simple array serializer that serialize arrays and collections into a coma-separated values.
 * <p>In the case of array-of-arrays or collection-of-collections or any combination and depth, a simple coma separated list of all values will still be returned.
 * <p>Each single value serialization is done by calling toString() or ignored if null
 */
public class ArrayParamSerializer implements Serializer {

    private static final String DEFAULT_SEPARATOR = ",";
    private final String separator;
    private final Serializer paramSerializer;

    public ArrayParamSerializer() {
        this(new ToStringParamSerializer(), DEFAULT_SEPARATOR);
    }

    public ArrayParamSerializer(String separator) {
        this(new ToStringParamSerializer(), separator);
    }

    public ArrayParamSerializer(Serializer paramSerializer) {
        this(paramSerializer, DEFAULT_SEPARATOR);
    }

    public ArrayParamSerializer(Serializer paramSerializer, String separator) {
        this.paramSerializer = paramSerializer;
        this.separator = separator;
    }

    @Override
    public String serialize(ParamContext context) {
        if (context.getArgValue() == null) {
            return "";
        } else if (context.getArgValue().getClass().isArray())
            return serializeArray(context, context.getArgValue());
        else if (context.getArgValue() instanceof Collection)
            return serialize(context, (Collection) context.getArgValue());
        else
            throw new IllegalArgumentException("Param should be an array or a collection");
    }

    private String serializeArray(ParamContext context, Object params) {
        List<Object> o = new ArrayList<Object>();
        int len = Array.getLength(params); // Can't cast, params can be an array of primitives.
        for (int i = 0; i < len; i++) o.add(Array.get(params, i));
        return serialize(context, o);
    }

    private String serialize(ParamContext context, Collection params) {
        StringBuilder sb = new StringBuilder("");
        int i = 0;
        for (Object p : params) {
            if (p.getClass().isArray() || p instanceof Collection) {
                sb.append(serialize(context));
            } else {
                String s;
                if (p != null && Strings.isNotBlank(s = p.toString())) {
                    sb.append(s);
                }
            }
            if (++i < params.size()) sb.append(separator);
        }
        return sb.toString();
    }
}
