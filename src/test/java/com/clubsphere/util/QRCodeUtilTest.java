package com.clubsphere.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QRCodeUtilTest {

    @Test
    void generateQRCodePNGReturnsPngBytes() throws Exception {
        byte[] png = QRCodeUtil.generateQRCodePNG("https://example.edu/event/register?token=demo", 160, 160);
        assertNotNull(png);
        assertTrue(png.length > 100);
        assertEquals((byte) 0x89, png[0]);
        assertEquals((byte) 0x50, png[1]);
        assertEquals((byte) 0x4E, png[2]);
        assertEquals((byte) 0x47, png[3]);
    }

    @Test
    void generateQRCodeBase64ReturnsDataUri() {
        String dataUri = QRCodeUtil.generateQRCodeBase64("ClubSphere demo token", 120, 120);
        assertNotNull(dataUri);
        assertTrue(dataUri.startsWith("data:image/png;base64,"));
        assertTrue(dataUri.length() > 50);
    }
}
