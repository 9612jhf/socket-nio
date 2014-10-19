package com.payneteasy.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

import static com.payneteasy.websocket.WebSocketUtil.toHex;

/**
 *
 */
public class WebSocketFrameEncoder {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketFrameEncoder.class);

    public void encode(WebSocketFrame aFrame, OutputStream aOutput) throws IOException {
        int length = aFrame.payloadLength;

        // writes op-code
        byte finBit = (byte) (aFrame.fin ? 0x80 : 0x00);
        aOutput.write(finBit | aFrame.opCode.code);

        byte maskBit = (byte) (aFrame.maskedPayload ? 0x80 : 0x00);

        // writes length
        if(length <=125) {
            byte len1 = (byte) (length | maskBit);
            aOutput.write(len1);
        } else if (length <= 0xFFFF) {
            aOutput.write( (byte) (maskBit | 126) );
            aOutput.write( length >>> 8 & 0xFF );
            aOutput.write( length & 0xFF );
        } else {
            throw new IllegalStateException("Unsupported payload length "+ length);
        }

        // writes mask
        if(aFrame.maskedPayload) {
            aOutput.write( aFrame.maskingKey_4 );
        }

        if( aFrame.applicationData != null) {
            aOutput.write( aFrame.applicationData );
        }

    }
}
