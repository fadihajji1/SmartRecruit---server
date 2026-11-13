package com.smartrecruit.identity.service;

import com.smartrecruit.identity.model.AccountStatus;

/** Correct credentials, but the account may not sign in yet (pending review, rejected or blocked). */
public class AccountNotActiveException extends RuntimeException {
	private final AccountStatus status;

	public AccountNotActiveException(AccountStatus status) {
		super(switch (status) {
			case PENDING -> "Your recruiter account is waiting for an administrator to review your employment proof.";
			case REJECTED -> "Your recruiter request was not approved. Contact the platform team if you think this is a mistake.";
			default -> "This account has been deactivated. Contact the platform team to restore access.";
		});
		this.status = status;
	}

	public AccountStatus getStatus() {
		return status;
	}
}
