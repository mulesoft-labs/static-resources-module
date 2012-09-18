/**
 * Mule Static Resources Module
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.staticresources;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.transport.PropertyScope;
import org.mule.construct.Flow;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.transport.http.HttpConnector;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class StaticResourcesModuleTest extends FunctionalTestCase {

    private static final Class[] parameters = new Class[] { URL.class };

    private static final String index = "<!DOCTYPE HTML>\n" +
            "<html>\n" +
            "  <head>\n" +
            "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n" +
            "  \n" +
            "    <title>Hello</title>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <p>Hello world!</p>\n" +
            "  </body>\n" +
            "</html>\n" +
            "";

    @Override
    protected String getConfigResources() {
        return "mule-config.xml";
    }

    @Before
    public void before() throws Exception {
        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;
        Method method = sysclass.getDeclaredMethod("addURL", parameters);
        method.setAccessible(true);
        method.invoke(sysloader, getClass().getResource("/hello.jar").toURI().toURL());
    }

    @Test
    public void serveIndex() throws Exception {
        HashMap<String, Object> inboundProperties = new HashMap<String, Object>();

        inboundProperties.put(HttpConnector.HTTP_REQUEST_PATH_PROPERTY, "http://dummy/");
        inboundProperties.put(HttpConnector.HTTP_CONTEXT_PATH_PROPERTY, "http://dummy");

        Object testServe = runFlow("testServe", inboundProperties);
        assertEquals(index,new String((byte[]) testServe));
    }

    @Test
    public void serve() throws Exception {
        HashMap<String, Object> inboundProperties = new HashMap<String, Object>();

        inboundProperties.put(HttpConnector.HTTP_REQUEST_PATH_PROPERTY, "http://dummy/hello.html");
        inboundProperties.put(HttpConnector.HTTP_CONTEXT_PATH_PROPERTY, "http://dummy");

        Object testServe = runFlow("testServe", inboundProperties);
        assertEquals(index,new String((byte[]) testServe));
    }

    /**
     * Run a flow and cast its result to type T
     *
     * @param flowName Name of the flow to run
     */
    private <T> T runFlow(String flowName, Map<String,Object> inboundProperties) throws Exception {
        return runFlow(flowName, null, inboundProperties);
    }

    /**
     * Run a flow with a payload of type U and cast its result to type T
     *
     * @param flowName Name of the flow to run
     * @param payload  payload to use in the execution
     */
    @SuppressWarnings("unchecked")
    private <T, U> T runFlow(String flowName, U payload, Map<String, Object> inboundHeaders) throws Exception {
        Flow flow = lookupFlowConstruct(flowName);

        if (flow == null) {
            throw new IllegalStateException("Flow with the name [" + flowName + "] was not found.");
        }

        MuleEvent testEvent = AbstractMuleContextTestCase.getTestEvent(payload);
        for (Map.Entry<String, Object> property: inboundHeaders.entrySet()) {
            testEvent.getMessage().setProperty(property.getKey(), property.getValue(), PropertyScope.INBOUND);
        }

        MuleMessage message = flow.process(testEvent).getMessage();
        return (T) message.getPayload();
    }

    /**
     * Retrieve a flow by name from the registry
     *
     * @param name Name of the flow to retrieve
     */
    protected Flow lookupFlowConstruct(String name) {
        return muleContext.getRegistry().lookupObject(name);
    }
}
