package org.legaltech.wales;

import com.fasterxml.jackson.databind.JsonNode;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.legaltech.wales.builders.QueryBuilder;
import org.legaltech.wales.builders.ResponseBuilder;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;

@Path("/search")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ElasticProxy {

	private final SearchProvider searchConfig;

	@Inject
	public ElasticProxy(SearchProvider searchConfig) {
		this.searchConfig = searchConfig;
	}

	@GET
	public Response get() {
		HttpAuthenticationFeature authenticationFeature =
				HttpAuthenticationFeature.basic(searchConfig.getUser(), searchConfig.getPassword());

		Client client = ClientBuilder.newClient().register(authenticationFeature);
		WebTarget webTarget = client.target(searchConfig.getUrl());

		return webTarget.request().get();
	}

	@POST
	public Response post(String data) {
		HttpAuthenticationFeature authenticationFeature =
				HttpAuthenticationFeature.basic(searchConfig.getUser(), searchConfig.getPassword());

		Client client = ClientBuilder.newClient().register(authenticationFeature);
		WebTarget webTarget = client.target(searchConfig.getUrl());

		JsonNode dataNode = QueryBuilder.build(data);
		Response response = webTarget.request().post(Entity.json(dataNode.toString()));

		if (response.getStatus() == OK.getStatusCode()) {
			return ResponseBuilder.build(response, dataNode);
		} else {
			return response;
		}
	}
}
