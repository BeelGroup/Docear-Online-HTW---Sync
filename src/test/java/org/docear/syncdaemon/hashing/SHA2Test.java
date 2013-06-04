package org.docear.syncdaemon.hashing;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class SHA2Test {

	@Test
	public void testSHA2ForFile() throws IOException{
		File file = new File("./src/test/resources/Testprojects/Project_0/rootFile.pptx");
		HashAlgorithm hashAlgo = new SHA2();
		String hash = hashAlgo.generate(file);
		assertThat(hash).isEqualTo("6d32f9ecafba3ecfdf686471a4d1fec68bb99aa4c9a55a3e200df0f62c03c425e34446c5f8afb4625784e5a82ea333b01b13524f9313c30385b6d49d665028b3");
	}
	
}
