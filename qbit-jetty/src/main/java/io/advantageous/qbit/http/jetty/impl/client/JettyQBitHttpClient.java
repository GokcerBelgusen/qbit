/*******************************************************************************
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  ________ __________.______________
 *  \_____  \\______   \   \__    ___/
 *   /  / \  \|    |  _/   | |    |  ______
 *  /   \_/.  \    |   \   | |    | /_____/
 *  \_____\ \_/______  /___| |____|
 *         \__>      \/
 *  ___________.__                  ____.                        _____  .__                                             .__
 *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
 *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
 *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
 *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
 *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
 *  .____    ._____.
 *  |    |   |__\_ |__
 *  |    |   |  || __ \
 *  |    |___|  || \_\ \
 *  |_______ \__||___  /
 *          \/       \/
 *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
 *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
 *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
 *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
 *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
 *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
 *  __________           __  .__              __      __      ___.
 *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__
 *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
 *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
 *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
 *          \/     \/             \/     \/         \/       \/    \/
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
 *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
 *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
 *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
 *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html
 ******************************************************************************/

package io.advantageous.qbit.http.jetty.impl.client;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.util.MultiMap;
import org.boon.Str;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static io.advantageous.qbit.http.websocket.WebSocketBuilder.webSocketBuilder;
import static org.boon.Boon.puts;

/**
 * @author rhightower on 2/14/15.
 */
public class JettyQBitHttpClient implements HttpClient {

    private final Logger logger = LoggerFactory.getLogger(JettyQBitHttpClient.class);
    private final boolean debug = false || GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final org.eclipse.jetty.client.HttpClient httpClient = new
            org.eclipse.jetty.client.HttpClient();
    private final WebSocketClient webSocketClient = new WebSocketClient();
    private final String host;
    private final int port;
    private Map<String, WebSocket> webSocketMap = new ConcurrentHashMap<>();


    public JettyQBitHttpClient(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void sendHttpRequest(HttpRequest request) {
        final Request jettyRequest = createJettyRequest(request);
        jettyRequest.send(createJettyListener(request));
    }

    private Request createJettyRequest(HttpRequest request) {
        final String uri = createURIString(request);
        final HttpMethod jettyMethod = getHttpMethod(request);
        final Request jettyRequest = httpClient.newRequest(uri)
                .method(jettyMethod);

        if (jettyMethod == HttpMethod.POST || jettyMethod == HttpMethod.PUT) {
            jettyRequest.content(new BytesContentProvider(request.getContentType(), request.getBody()));
        }
        copyParams(request, jettyRequest);
        copyHeaders(request, jettyRequest);
        return jettyRequest;
    }

    private String createURIString(HttpRequest request) {
        return Str.add("http://", host, ":", Integer.toString(port), request.getUri());
    }

    private void copyParams(HttpRequest request, Request jettyRequest) {
        final MultiMap<String, String> params = request.getParams();
        final Iterator<Map.Entry<String, Collection<String>>> iterator = params.iterator();
        final Fields paramFields = jettyRequest.getParams();

        while (iterator.hasNext()) {
            final Map.Entry<String, Collection<String>> entry = iterator.next();
            final String paramName = entry.getKey();
            final Collection<String> values = entry.getValue();

            for (String value : values) {
                paramFields.add(paramName, value);

                if (debug) puts("Adding Params", paramName, value);
            }
        }
    }

    public WebSocket createWebSocket(final String uri) {
        JettyClientWebSocketSender webSocketSender =
                new JettyClientWebSocketSender(
                        host, port, uri, webSocketClient
                );

        WebSocket webSocket = webSocketBuilder()
                .setUri(uri)
                .setRemoteAddress(webSocketSender.getConnectUri().toString())
                .setWebSocketSender(webSocketSender)
                .build();

        return webSocket;
    }

    private void copyHeaders(HttpRequest request, Request jettyRequest) {
        final MultiMap<String, String> headers = request.getHeaders();
        final Iterator<Map.Entry<String, Collection<String>>> iterator = headers.iterator();
        final HttpFields headerFields = jettyRequest.getHeaders();
        while (iterator.hasNext()) {
            final Map.Entry<String, Collection<String>> entry = iterator.next();
            final String paramName = entry.getKey();
            final Collection<String> values = entry.getValue();
            for (String value : values) {
                headerFields.add(paramName, value);
                if (debug) puts("Adding Header", paramName, value);
            }
        }
    }

    private BufferingResponseListener createJettyListener(final HttpRequest request) {
        return new BufferingResponseListener(1_000_000) {

            @Override
            public void onComplete(Result result) {

                if (!result.isFailed()) {
                    byte[] responseContent = getContent();

                    if (request.getResponse().isText()) {
                        String responseString = new String(responseContent, StandardCharsets.UTF_8);

                        request.getResponse().response(result.getResponse().getStatus(),
                                result.getResponse().getHeaders().get(HttpHeader.CONTENT_TYPE),
                                responseString);
                    } else {
                        request.getResponse().response(result.getResponse().getStatus(),
                                result.getResponse().getHeaders().get(HttpHeader.CONTENT_TYPE),
                                responseContent);

                    }
                }

            }
        };
    }

    private HttpMethod getHttpMethod(HttpRequest request) {
        final String method = request.getMethod();
        return HttpMethod.fromString(method.toUpperCase());
    }

    @Override
    public void periodicFlushCallback(Consumer<Void> periodicFlushCallback) {

    }

    @Override
    public HttpClient start() {
        try {
            httpClient.start();
        } catch (Exception e) {

            throw new IllegalStateException("Unable to start httpClient Jetty support", e);
        }


        try {
            webSocketClient.start();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to start websocket Jetty support", e);
        }

        return this;
    }

    @Override
    public void flush() {

    }

    @Override
    public void stop() {

        try {
            httpClient.stop();
            webSocketClient.stop();
        } catch (Exception e) {

            logger.warn("problem stopping", e);
        }

    }
}