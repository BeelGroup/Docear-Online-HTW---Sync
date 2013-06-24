package org.docear.syncdaemon;

import org.docear.syncdaemon.client.ClientService;
import org.docear.syncdaemon.client.ClientServiceImpl;
import org.docear.syncdaemon.logging.LoggingPlugin;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.docear.syncdaemon.TestUtils.*;
import static org.fest.assertions.Assertions.assertThat;

public class DaemonConfigurationTest {
    private static final Logger logger = LoggerFactory.getLogger(Daemon.class);


    @Test
    public void testGetConfigurationFromClasspath() throws Exception {
        final Daemon daemon = testDaemon();
        assertBaseUrl(daemon, "http://localhost:9000/api");
    }

    @Test
    public void testGetConfigurationWithInjected() throws Exception {
        final String baseUrl = "https://my.docear.org";
        final Daemon daemon = testDaemonWithAdditionalConfiguration("daemon.client.baseurl=\"" + baseUrl + "\"");
        assertDaemonNameIsCorrect(daemon);
        assertBaseUrl(daemon, baseUrl);
    }

    @Test
    public void testFindPluginSettingsInConfigFile() throws Exception {
        final LoggingPlugin plugin = testDaemon().plugin(LoggingPlugin.class);
        assertThat(plugin).overridingErrorMessage("logging plugin not found").isNotNull();
    }

    @Test
    public void testDependencyInjection() throws Exception {
        final ClientService service = testDaemon().service(ClientService.class);
        assertThat(service).isNotNull();
        assertThat(service).isInstanceOf(ClientServiceImpl.class);
    }

    @Test
    public void testDependencyInjectionWithConfiguration() throws Exception {
        final Daemon daemon = daemonWithService(AServiceInterface.class, ServiceNeedingConfig.class);
        final AServiceInterface service = daemon.service(AServiceInterface.class);
        assertThat(service).isNotNull();
        assertThat(service).isInstanceOf(ServiceNeedingConfig.class);
        final ServiceNeedingConfig serviceNeedingConfig = (ServiceNeedingConfig) service;
        assertThat(serviceNeedingConfig.getConfig()).isNotNull();
        assertThat(serviceNeedingConfig.getConfig().getString("daemon.name")).isNotEmpty();
    }

    @Test(expected = com.typesafe.config.ConfigException.class)
    public void testDependencyInjectionWithNoConfigKey() throws Exception {
        testDaemon().service(DaemonConfigurationTest.class);
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
