package com.payneteasy.socketio;

import com.payneteasy.websocket.UnauthorizedException;
import com.payneteasy.websocket.WebSocketClient;
import com.payneteasy.websocket.WebSocketHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

/**
 *
 */
public class SocketIoClient {

    private static final Logger LOG = LoggerFactory.getLogger(SocketIoClient.class);

    private final WebSocketClient webSocketClient = new WebSocketClient();

    public void connect(URL aUrl, ISocketIoListener aListener) throws IOException, UnauthorizedException {
        String connectionParameters =  getSocketIoConnectionParameters(aUrl);

        SocketIoHandshakeResponse info = parseConnectionInfo(connectionParameters);
        LOG.debug("handshake response: {}", info);
        SocketIoProcessor listener = new SocketIoProcessor(aListener);
        createWebSocketSession(info, aUrl, listener);
    }

    private String getSocketIoConnectionParameters(URL aUrl) throws IOException, UnauthorizedException {
        HttpURLConnection connection = (HttpURLConnection) aUrl.openConnection();
        connection.setConnectTimeout(120000);
        connection.setReadTimeout(120000);

        LOG.debug("Connecting to {}", aUrl);
        connection.connect();
        try {
            checkResponseCode(connection);

            LineNumberReader in = new LineNumberReader(new InputStreamReader(connection.getInputStream()));
            try {
                String line = in.readLine();
                if(line==null) {
                    throw new ProtocolException("Empty line");
                }
                return line;

            } finally {
                in.close();
            }
        } finally {
            connection.disconnect();
        }
    }

    private void checkResponseCode(HttpURLConnection connection) throws IOException, UnauthorizedException {
        if(connection.getResponseCode() != 200) {
            if(connection.getResponseCode()==401) {
                throw new UnauthorizedException(connection.getResponseMessage());
            } else {
                throw new IllegalStateException(connection.getResponseCode()+" "+connection.getResponseMessage());
            }
        }
    }

    private void createWebSocketSession(SocketIoHandshakeResponse aInfo, URL aUrl, SocketIoProcessor listener) throws IOException {
        String baseUrl = createBaseUrl(aUrl);

        WebSocketHandshake handshake = new WebSocketHandshake.Builder()
                .url(baseUrl+"websocket/"+aInfo.sessionId)
                .build();
        webSocketClient.connect(handshake, listener);
    }

    private String createBaseUrl(URL aUrl) {
        return aUrl.getProtocol()
                + "://"
                + aUrl.getHost()
                + ":"
                + aUrl.getPort()
                + aUrl.getPath();
    }

    private SocketIoHandshakeResponse parseConnectionInfo(String aLine) {
        String[] tokens = aLine.split(":");
        return new SocketIoHandshakeResponse.Builder()
                .sessionId(tokens[0])
                .heartbeatTimeout(Long.parseLong(tokens[1])+1000)
                .closingTimeout(Long.parseLong(tokens[2])+1000)
                .protocols(tokens[3].split(","))
                .build();
    }
}
