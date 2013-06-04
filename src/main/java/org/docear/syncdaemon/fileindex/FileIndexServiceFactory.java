package org.docear.syncdaemon.fileindex;

import org.docear.syncdaemon.Daemon;

public interface FileIndexServiceFactory {
    FileIndexService create(Daemon daemon);
}
