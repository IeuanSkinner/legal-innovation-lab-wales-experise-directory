package org.legaltech.wales;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/search")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class ElasticProxy {

	private final SearchProvider searchConfig;

	@Inject
	public ElasticProxy(SearchProvider searchConfig) {
		this.searchConfig = searchConfig;
	}

	@GET
	public Response search() {
		HttpAuthenticationFeature authenticationFeature =
				HttpAuthenticationFeature.basic(searchConfig.getUser(), searchConfig.getPassword());

		Client client = ClientBuilder.newClient().register(authenticationFeature);
		WebTarget webTarget = client.target(searchConfig.getUrl());

		return webTarget.request().get();
	}
}
