package dev.forgepack.authentication.internal.utils;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QRCodeTest {

    @Test
    void generateQRCodeBytes_producesValidPngImage() throws Exception {
        byte[] bytes = QRCode.generateQRCodeBytes("otpauth://totp/john?secret=ABC", 200);

        assertThat(bytes).isNotEmpty();
        assertThat(bytes[0]).isEqualTo((byte) 0x89);
        assertThat(bytes[1]).isEqualTo((byte) 'P');
        assertThat(bytes[2]).isEqualTo((byte) 'N');
        assertThat(bytes[3]).isEqualTo((byte) 'G');

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        assertThat(image.getWidth()).isEqualTo(200);
        assertThat(image.getHeight()).isEqualTo(200);
    }

    @Test
    void generateQRCodeBytes_dataTooLarge_throwsRuntimeException() {
        String oversizedContent = "A".repeat(5000);

        assertThatThrownBy(() -> QRCode.generateQRCodeBytes(oversizedContent, 200))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error generating QR Code");
    }
}
