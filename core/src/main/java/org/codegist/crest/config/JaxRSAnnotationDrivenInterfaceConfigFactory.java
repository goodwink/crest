/*
 * Copyright 2010 CodeGist.org
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *  ==================================================================
 *
 *  More information at http://www.codegist.org.
 */

package org.codegist.crest.config;

import org.codegist.common.reflect.Methods;
import org.codegist.crest.CRestContext;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author laurent.gilles@codegist.org
 */
public class JaxRSAnnotationDrivenInterfaceConfigFactory implements InterfaceConfigFactory {

    private final boolean useDefaults;

    public JaxRSAnnotationDrivenInterfaceConfigFactory(boolean useDefaults) {
        this.useDefaults = useDefaults;
    }
    public JaxRSAnnotationDrivenInterfaceConfigFactory() {
        this(true);
    }

    public InterfaceConfig newConfig(Class<?> interfaze, CRestContext context) throws ConfigFactoryException {


        try {
            Path contextPath = interfaze.getAnnotation(Path.class);
//        Consumes consumes = interfaze.getAnnotation(Consumes.class); // todo
//        Produces produces = interfaze.getAnnotation(Produces.class); // todo
//        CookieParam cookieParam = interfaze.getAnnotation(CookieParam.class); // todo
//        Encoded encoded = interfaze.getAnnotation(Encoded.class); // todo
            ConfigBuilders.InterfaceConfigBuilder icb = new ConfigBuilders.InterfaceConfigBuilder(interfaze, context.getProperties());

            if(contextPath != null)
                icb.setContextPath(contextPath.value());

            for (Method meth : interfaze.getDeclaredMethods()) {

                //        HttpMethod httpMethod = interfaze.getAnnotation(HttpMethod.class); todo
                Path path = meth.getAnnotation(Path.class);
                DefaultValue defaultValue = (DefaultValue) meth.getAnnotation(DefaultValue.class);

                HttpMethod method = null;
                for(Annotation a : meth.getAnnotations()){
                    method = a.annotationType().getAnnotation(HttpMethod.class);
                    if(method != null) break;
                }

                ConfigBuilders.MethodConfigBuilder mcb = icb.startMethodConfig(meth);

                if(path != null) mcb.setPath(path.value());
                if(method != null)  mcb.setHttpMethod(method.value());
                if(defaultValue != null)  mcb.setParamsDefautValue(defaultValue.value());


                for(int i = 0, max = meth.getParameterTypes().length; i < max ; i++){
                    Map<Class<? extends Annotation>,Annotation> paramAnnotations = Methods.getParamsAnnotation(meth, i);
                    ConfigBuilders.ParamConfigBuilder pcb = mcb.startParamConfig(i);

                    // Injects user type annotated config.
                    Configs.injectAnnotatedConfig(pcb, meth.getParameterTypes()[i]);
                    defaultValue = (DefaultValue) paramAnnotations.get(DefaultValue.class);

                    //param
                    FormParam formParam = (FormParam) paramAnnotations.get(FormParam.class);
                    HeaderParam headerParam = (HeaderParam) paramAnnotations.get(HeaderParam.class);
                    PathParam pathParam = (PathParam) paramAnnotations.get(PathParam.class);
                    QueryParam queryParam = (QueryParam) paramAnnotations.get(QueryParam.class);
//                    CookieParam cookieParam = (CookieParam) paramAnnotations.get(CookieParam.class); todo
//                    MatrixParam matrixParam = (MatrixParam) paramAnnotations.get(MatrixParam.class); todo

                    if(defaultValue != null) pcb.setDefaultValue(defaultValue.value());
                    if(formParam != null) {
                        pcb.setDestination(Destination.BODY);
                        pcb.setName(formParam.value());
                    }else if(headerParam != null) {
                        pcb.setDestination(Destination.HEADER);
                        pcb.setName(headerParam.value());
                    }else if(pathParam != null) {
                        pcb.setDestination(Destination.URL);
                        pcb.setName(pathParam.value());
                    }else if(queryParam != null) {
                        pcb.setDestination(Destination.URL);
                        pcb.setName(queryParam.value());
                    }/*else if(matrixParam != null) {

                    }*/
                    pcb.endParamConfig();
                }
                mcb.endMethodConfig();
            }

            return icb.build(useDefaults);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigFactoryException(e);
        }
    }

}
