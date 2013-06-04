package org.docear.syncdaemon.fileindex;

import org.docear.syncdaemon.Daemon;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

public class FileIndexTest {

	@Test
	public void testFileIndexProjectFolder(){
		final Daemon daemon = new Daemon();
		FileIndexPlugin fileIndexPlugin = daemon.addPluginByClass(FileIndexPlugin.class);
		daemon.onStart();
	}
	
}
