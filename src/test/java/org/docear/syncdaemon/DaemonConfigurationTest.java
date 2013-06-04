package org.docear.syncdaemon;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.ClientServiceImpl;
import org.docear.syncdaemon.logging.LoggingPlugin;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.fest.assertions.Assertions.assertThat;

public class DaemonConfigurationTest {
    private static final Logger logger = LoggerFactory.getLogger(Daemon.class);


    @Test
    public void testGetConfigurationFromClasspath() throws Exception {
        final Daemon daemon = new Daemon();
        assertDaemonNameIsCorrect(daemon);
        assertBaseUrl(daemon, "http://localhost:9000");
    }

    @Test
    public void testGetConfigurationWithInjected() throws Exception {
        final String baseUrl = "https://my.docear.org";
        final Config overWritingConfig = ConfigFactory.parseString("daemon.client.baseurl=\"" + baseUrl + "\"");
        final Daemon daemon = Daemon.createWithAdditionalConfig(overWritingConfig);
        assertDaemonNameIsCorrect(daemon);
        assertBaseUrl(daemon, baseUrl);
    }

    @Test
    public void testFindPluginSettingsInConfigFile() throws Exception {
        final LoggingPlugin plugin = new Daemon().plugin(LoggingPlugin.class);
        assertThat(plugin).overridingErrorMessage("logging plugin not found").isNotNull();
    }

    @Test
    public void testDependencyInjection() throws Exception {
        final ClientService service = new Daemon().service(ClientService.class);
        assertThat(service).isNotNull();
        assertThat(service).isInstanceOf(ClientServiceImpl.class);
    }

    @Test(expected = com.typesafe.config.ConfigException.class)
    public void testDependencyInjectionWithNoConfigKey() throws Exception {
        new Daemon().service(DaemonConfigurationTest.class);
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
