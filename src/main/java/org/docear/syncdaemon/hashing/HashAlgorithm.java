package org.docear.syncdaemon.hashing;

import java.io.File;
import java.io.IOException;

public interface HashAlgorithm {

	public String generate(File file) throws IOException;
}
