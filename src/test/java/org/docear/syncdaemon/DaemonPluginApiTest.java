package org.docear.syncdaemon;

import org.junit.Test;

import static org.docear.syncdaemon.TestUtils.testDaemon;
import static org.fest.assertions.Assertions.assertThat;

public class DaemonPluginApiTest {

    @Test
    public void testPluginLifeCycle() throws Exception {
        final Daemon daemon = testDaemon();
        final DemoPlugin plugin = new DemoPlugin(daemon);
        daemon.addPlugin(plugin);
        assertThat(plugin.onStartCalled).isFalse();
        assertThat(plugin.onStopCalled).isFalse();
        daemon.onStart();
        assertThat(plugin.onStartCalled).isTrue();
        assertThat(plugin.onStopCalled).isFalse();
        daemon.onStop();
        assertThat(plugin.onStartCalled).isTrue();
        assertThat(plugin.onStopCalled).isTrue();
    }

    public static class DemoPlugin extends Plugin {
        public boolean onStartCalled = false;
        public boolean onStopCalled = false;

        public DemoPlugin(Daemon daemon) {
            super(daemon);
        }

        @Override
        public void onStart() {
            onStartCalled = true;
        }

        @Override
        public void onStop() {
            onStopCalled = true;
        }
    }
}
