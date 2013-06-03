package org.docear.syncdaemon;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class DaemonTest {
    @Test
    public void testGetConfigurationFromClasspath() throws Exception {
        final Daemon daemon = new Daemon();
        assertDaemonNameIsCorrect(daemon);
        assertBaseUrl(daemon, "https://my.docear.org");
    }

    @Test
    public void testGetConfigurationWithInjected() throws Exception {
        final String baseUrl = "http://localhost:9000";
        final Config overWritingConfig = ConfigFactory.parseString("daemon.client.baseurl=\"" + baseUrl + "\"");
        final Daemon daemon = Daemon.createWithAdditionalConfig(overWritingConfig);
        assertDaemonNameIsCorrect(daemon);
        assertBaseUrl(daemon, baseUrl);
    }

    private void assertBaseUrl(Daemon daemon, String baseUrl) {
        assertThat(daemon.getConfig().getString("daemon.client.baseurl")).isEqualTo(baseUrl);
    }

    private void assertDaemonNameIsCorrect(Daemon daemon) {
        final String daemonName = daemon.getConfig().getString("daemon.name");
        assertThat(daemonName).isNotNull();
        assertThat(daemonName).contains("Docear");
    }
}
