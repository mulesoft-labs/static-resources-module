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

import org.apache.log4j.Logger;
import org.mule.api.MuleEvent;
import org.mule.api.annotations.Module;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.param.InboundHeaders;
import org.mule.api.annotations.param.OutboundHeaders;
import org.mule.transformer.types.MimeTypes;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;
import org.mule.transport.http.components.ResourceNotFoundException;
import org.mule.transport.http.i18n.HttpMessages;
import org.mule.util.FilenameUtils;
import org.mule.util.IOUtils;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Provides a way of hosting static resources inside a Mule App
 *
 * @author MuleSoft, Inc.
 */
@Module(name = "static-resources", schemaVersion = "1.0", friendlyName = "Static Resources Module")
public class StaticResourcesModule {

    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    public static final String MIME_TYPE_JAVASCRIPT = "application/x-javascript";
    public static final String MIME_TYPE_PNG = "image/png";
    public static final String MIME_TYPE_GIF = "image/gif";
    public static final String MIME_TYPE_CSS = "text/css";

    private static final Logger LOGGER = Logger.getLogger(StaticResourcesModule.class);

    /**
     * Serves static content stored in the classpath
     *
     * {@sample.xml ../../../doc/static-resources-module.xml.sample static-resources:serve}
     *
     * @param path Request Path
     * @param contextPath Request Path Root
     * @param outboundHeaders Outbound Headers
     * @param muleEvent Received Mule Event
     * @param basePackage The package in the classpath where to find the static resources
     * @param defaultResource Resource to load when called "/" (i.e. index.html)
     * @return the bytes of the required resource
     * @throws ResourceNotFoundException When the resource has not been found
     * @throws IOException When fetching/reading the resource
     */
    @Inject
    @Processor
    public byte[] serve(@InboundHeaders(HttpConnector.HTTP_REQUEST_PATH_PROPERTY) String path,
                                        @InboundHeaders(HttpConnector.HTTP_CONTEXT_PATH_PROPERTY) String contextPath,
                                        @OutboundHeaders Map<String, Object> outboundHeaders,
                                        MuleEvent muleEvent,
                                        String basePackage,
                                        String defaultResource) throws ResourceNotFoundException, IOException {
        path = path.substring(contextPath.length() + 1);

        InputStream in = null;
        try {
            basePackage += basePackage.endsWith("/") ? "" : "/";

            String finalPath = basePackage + path;

            LOGGER.info("Fetching " + finalPath);

            if ( "".equals(path) || "/".equals(path)) {
                path += defaultResource;
                finalPath += defaultResource;
            }

            in = getClass().getResourceAsStream(finalPath);
            if (in == null) {
                outboundHeaders.put("http.status", 404);
                LOGGER.info("Resource not found");
                throw new ResourceNotFoundException(HttpMessages.fileNotFound(path), muleEvent);
            }

            String mimeType = DEFAULT_MIME_TYPE;
            if (FilenameUtils.getExtension(path).equals("html")) {
                mimeType = MimeTypes.HTML;
            } else if (FilenameUtils.getExtension(path).equals("js")) {
                mimeType = MIME_TYPE_JAVASCRIPT;
            } else if (FilenameUtils.getExtension(path).equals("png")) {
                mimeType = MIME_TYPE_PNG;
            } else if (FilenameUtils.getExtension(path).equals("gif")) {
                mimeType = MIME_TYPE_GIF;
            } else if (FilenameUtils.getExtension(path).equals("css")) {
                mimeType = MIME_TYPE_CSS;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copyLarge(in, baos);

            byte[] buffer = baos.toByteArray();

            outboundHeaders.put(HttpConnector.HTTP_STATUS_PROPERTY, String.valueOf(HttpConstants.SC_OK));
            outboundHeaders.put(HttpConstants.HEADER_CONTENT_TYPE, mimeType);
            outboundHeaders.put(HttpConstants.HEADER_CONTENT_LENGTH, buffer.length);
            return buffer;
        } catch (IOException e) {
            throw new ResourceNotFoundException(HttpMessages.fileNotFound(path), muleEvent);
        } finally {
            if (in != null) {
                in.close();
            }
        }

    }

}
