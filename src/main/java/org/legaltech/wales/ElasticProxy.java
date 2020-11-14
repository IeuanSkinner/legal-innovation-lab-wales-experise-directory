package org.legaltech.wales;

import com.fasterxml.jackson.databind.JsonNode;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.legaltech.wales.builders.QueryJsonBuilder;
import org.legaltech.wales.builders.ResponseJsonBuilder;

import javax.enterprise.context.ApplicationScoped;
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
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ElasticProxy {

	private final WebTarget webTarget;
	private final QueryJsonBuilder queryBuilder;
	private final ResponseJsonBuilder responseBuilder;

	@Inject
	public ElasticProxy(SearchProvider searchConfig, QueryJsonBuilder queryBuilder, ResponseJsonBuilder responseBuilder) {
		HttpAuthenticationFeature authenticationFeature =
				HttpAuthenticationFeature.basic(searchConfig.getUser(), searchConfig.getPassword());

		Client client = ClientBuilder.newClient().register(authenticationFeature);
		this.webTarget = client.target(searchConfig.getUrl());
		this.queryBuilder = queryBuilder;
		this.responseBuilder = responseBuilder;
	}

	@GET
	public Response get() {
		return webTarget.request().get();
	}

	@POST
	public Response post(String data) {
		JsonNode dataNode = queryBuilder.build(data);
		Response response = webTarget.request().post(Entity.json(dataNode.toString()));

		if (response.getStatus() == OK.getStatusCode()) {
			return responseBuilder.build(response, dataNode);
		} else {
			return response;
		}
	}
}
