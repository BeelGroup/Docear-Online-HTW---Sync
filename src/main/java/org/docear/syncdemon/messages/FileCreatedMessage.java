package org.docear.syncdemon.messages;

import com.google.common.base.Objects;
import org.joda.time.DateTime;

import java.io.File;

public abstract class FileCreatedMessage {
    public abstract String getAbsolutePath();
    public abstract File getFile();
    public abstract DateTime getTimestamp();

    @Override
    public String toString() {
        return Objects.toStringHelper(this.getClass())
                .add("absolutePath", getAbsolutePath())
                .add("timestamp", getTimestamp())
                .toString();
    }
}