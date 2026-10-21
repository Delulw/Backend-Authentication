package com.delulw.backendauthentication.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "TLS_KEYSTORE_PATH=file:./local-dev.p12",
        "TLS_KEYSTORE_PASSWORD=test-only",
        "security.jwt.secret-base64=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "security.auth.username=test-user",
        "security.auth.password=test-password"
})
class TlsConfigurationTest {

    @Autowired
    private ServerProperties serverProperties;

    @Test
    void enablesHttpsWithOnlyTls12AndTls13() {
        assertEquals(8443, serverProperties.getPort());
        assertTrue(serverProperties.getSsl().isEnabled());
        assertEquals("PKCS12", serverProperties.getSsl().getKeyStoreType());
        assertArrayEquals(new String[]{"TLSv1.2", "TLSv1.3"},
                serverProperties.getSsl().getEnabledProtocols());
        assertNotNull(serverProperties.getSsl().getKeyStore());
        assertTrue(serverProperties.getSsl().getKeyStore().contains("local-dev.p12"));
    }
}
