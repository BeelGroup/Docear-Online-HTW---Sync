package org.docear.syncdaemon.client.exceptions;

@SuppressWarnings("serial")
public class NoFolderException extends RuntimeException {
	public NoFolderException(String message) {
		super(message);
	}
}
