package com.payneteasy.socketio;

import com.payneteasy.websocket.UnauthorizedException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 *
 */
public class SocketIoClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(SocketIoClientTest.class);

    @Test
    public void test() throws IOException, UnauthorizedException {
        SocketIoClient client = new SocketIoClient();
        ISocketIoListener listener = new ISocketIoListener() {

            @Override
            public void onEvent(String aEventName, SocketIoContext aContext, Object... args) {
                LOG.debug("Event is {}", aEventName);
                aContext.ack();
            }

            @Override
            public void onFailure(Throwable aError) {
                LOG.error("Error is", aError);
            }
        };

        SocketIoSession session = client.connect(new URL("http://localhost:8080/socket.io/1/" +
                        "?payber_id=1" +
                        "&date=1413570429230" +
                        "&sign=4d48514e6d3147654371735a4a76756465485431526933682f6f53743630365a7a39684b473677516a4e2f47355047343230494b6569796231744d7464385a71537a6661786362657677675559733334616964706a42757454656f70654c2b4371536267716d4a7876594c3267686f51584b7a3542455459734b506b446d33305a4c2b326a714c714153416b6a764c73796b6a6b385564674a66426a585650734a7662675930576b7678576a42652b2f6b4b6e537768764f5247323355414f2b353741584c342f4a4541385a5a314d56534778594a79364b4a4c4d526e7a64415a6153794b4c5831686a767a71362b43762b55792b39774c4b373845616230564964704c53333856383745426d6252424c7266777934304a2b3138735143353037512f4454436d64612f34637148466b71703230614c5a6973695868746d652b53644155533266356548775561726150387a326251513d3d" +
                        "&v=1.2")
        );
        session.startAndWait(listener);
    }
}
