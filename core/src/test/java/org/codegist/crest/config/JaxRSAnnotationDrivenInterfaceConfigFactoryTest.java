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

import org.codegist.crest.CRestContext;
import org.codegist.crest.TestUtils;
import org.junit.Test;

import javax.ws.rs.*;

import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;

/**
 * @author laurent.gilles@codegist.org
 */
public class JaxRSAnnotationDrivenInterfaceConfigFactoryTest {

    @Test
    public void testDefaultFactory1() throws ConfigFactoryException {
        testNonDefaultFactory1();
    }

    @Test
    public void testNonDefaultFactory1() throws ConfigFactoryException {
        InterfaceConfigFactory factory = new JaxRSAnnotationDrivenInterfaceConfigFactory(true);
        InterfaceConfig cfg = factory.newConfig(Interface.class, mock(CRestContext.class));

        InterfaceConfigTestHelper.assertExpected(BUILDER.build(true), cfg, Interface.class);
    }
    @Test
    public void testNonDefaultFactory2() throws ConfigFactoryException {
        InterfaceConfigFactory factory = new JaxRSAnnotationDrivenInterfaceConfigFactory(false);
        InterfaceConfig cfg = factory.newConfig(Interface.class, mock(CRestContext.class));

        InterfaceConfigTestHelper.assertExpected(BUILDER.build(false), cfg, Interface.class);
    }


    @Path("/my/path")
    static interface Interface {

        Method GET = TestUtils.getMethod(Interface.class, "get", String.class, int.class, long.class, boolean.class);
        Method POST = TestUtils.getMethod(Interface.class, "post", String.class, int.class, long.class, boolean.class);
        Method DELETE = TestUtils.getMethod(Interface.class, "delete", String.class, int.class, long.class, boolean.class);
        Method PUT = TestUtils.getMethod(Interface.class, "put", String.class, int.class, long.class, boolean.class);
        Method HEAD = TestUtils.getMethod(Interface.class, "head", String.class, int.class, long.class, boolean.class);
        Method OPTIONS = TestUtils.getMethod(Interface.class, "options", String.class, int.class, long.class, boolean.class);

        @GET
        @Path("/sub/path")
        String get(
                @QueryParam("s") String s,
                @FormParam("i") int i,
                @HeaderParam("l") long l,
                @PathParam("b") boolean b);

        @POST
        @Path("/sub/path")
        String post(
                @QueryParam("s") String s,
                @FormParam("i") int i,
                @HeaderParam("l") long l,
                @PathParam("b") boolean b);

        @DELETE
        @Path("/sub/path")
        String delete(
                @QueryParam("s") String s,
                @FormParam("i") int i,
                @HeaderParam("l") long l,
                @PathParam("b") boolean b);

        @PUT
        @Path("/sub/path")
        String put(
                @QueryParam("s") String s,
                @FormParam("i") int i,
                @HeaderParam("l") long l,
                @PathParam("b") boolean b);

        @HEAD
        @Path("/sub/path")
        String head(
                @QueryParam("s") String s,
                @FormParam("i") int i,
                @HeaderParam("l") long l,
                @PathParam("b") boolean b);

        @OPTIONS
        @Path("/sub/path")
        String options(
                @QueryParam("s") String s,
                @FormParam("i") int i,
                @HeaderParam("l") long l,
                @PathParam("b") boolean b);
    }

    private static final ConfigBuilders.InterfaceConfigBuilder BUILDER = new ConfigBuilders.InterfaceConfigBuilder(Interface.class)
            .setContextPath("/my/path")
            .startMethodConfig(Interface.GET)
            .setHttpMethod("GET")
            .setPath("/sub/path")
            .startParamConfig(0)
            .forQuery()
            .setName("s")
            .endParamConfig()
            .startParamConfig(1)
            .forForm()
            .setName("i")
            .endParamConfig()
            .startParamConfig(2)
            .forHeader()
            .setName("l")
            .endParamConfig()
            .startParamConfig(3)
            .forPath()
            .setName("b")
            .endParamConfig()
            .endMethodConfig()
            .startMethodConfig(Interface.POST)
            .setHttpMethod("POST")
            .setPath("/sub/path")
            .startParamConfig(0)
            .forQuery()
            .setName("s")
            .endParamConfig()
            .startParamConfig(1)
            .forForm()
            .setName("i")
            .endParamConfig()
            .startParamConfig(2)
            .forHeader()
            .setName("l")
            .endParamConfig()
            .startParamConfig(3)
            .forPath()
            .setName("b")
            .endParamConfig()
            .endMethodConfig()
            .startMethodConfig(Interface.PUT)
            .setPath("/sub/path")
            .setHttpMethod("PUT")
            .startParamConfig(0)
            .forQuery()
            .setName("s")
            .endParamConfig()
            .startParamConfig(1)
            .forForm()
            .setName("i")
            .endParamConfig()
            .startParamConfig(2)
            .forHeader()
            .setName("l")
            .endParamConfig()
            .startParamConfig(3)
            .forPath()
            .setName("b")
            .endParamConfig()
            .endMethodConfig()
            .startMethodConfig(Interface.DELETE)
            .setHttpMethod("DELETE")
            .setPath("/sub/path")
            .startParamConfig(0)
            .forQuery()
            .setName("s")
            .endParamConfig()
            .startParamConfig(1)
            .forForm()
            .setName("i")
            .endParamConfig()
            .startParamConfig(2)
            .forHeader()
            .setName("l")
            .endParamConfig()
            .startParamConfig(3)
            .forPath()
            .setName("b")
            .endParamConfig()
            .endMethodConfig()
            .startMethodConfig(Interface.HEAD)
            .setHttpMethod("HEAD")
            .setPath("/sub/path")
            .startParamConfig(0)
            .forQuery()
            .setName("s")
            .endParamConfig()
            .startParamConfig(1)
            .forForm()
            .setName("i")
            .endParamConfig()
            .startParamConfig(2)
            .forHeader()
            .setName("l")
            .endParamConfig()
            .startParamConfig(3)
            .forPath()
            .setName("b")
            .endParamConfig()
            .endMethodConfig()
            .startMethodConfig(Interface.OPTIONS)
            .setHttpMethod("OPTIONS")
            .setPath("/sub/path")
            .startParamConfig(0)
            .forQuery()
            .setName("s")
            .endParamConfig()
            .startParamConfig(1)
            .forForm()
            .setName("i")
            .endParamConfig()
            .startParamConfig(2)
            .forHeader()
            .setName("l")
            .endParamConfig()
            .startParamConfig(3)
            .forPath()
            .setName("b")
            .endParamConfig()
            .endMethodConfig();
}
