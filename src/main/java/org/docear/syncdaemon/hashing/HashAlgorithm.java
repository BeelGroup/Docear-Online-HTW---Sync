package org.docear.syncdaemon.hashing;

import java.io.File;
import java.io.FileNotFoundException;

public interface HashAlgorithm {

	public String generate(File file) throws FileNotFoundException;
}
