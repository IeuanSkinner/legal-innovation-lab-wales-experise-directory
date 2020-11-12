package org.legaltech.wales;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class SearchProvider {
	private final AtomicReference<String> url = new AtomicReference<>();
	private final AtomicReference<String> user = new AtomicReference<>();
	private final AtomicReference<String> password = new AtomicReference<>();

	@Inject
	public SearchProvider(@ConfigProperty(name = "search.url") String url,
						  @ConfigProperty(name = "search.user") String user,
						  @ConfigProperty(name = "search.password") String password) {
		this.url.set(url);
		this.user.set(user);
		this.password.set(password);
	}

	String getUrl() {
		return url.get();
	}

	String getUser() {
		return user.get();
	}

	String getPassword() {
		return password.get();
	}
}
