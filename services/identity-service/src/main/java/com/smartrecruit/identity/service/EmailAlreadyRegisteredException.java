package com.smartrecruit.identity.service;

public class EmailAlreadyRegisteredException extends RuntimeException {
	public EmailAlreadyRegisteredException() {
		super("Email address is already registered");
	}
}
