package org.legaltech.wales;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.legaltech.wales.builders.QueryJsonBuilder;
import org.legaltech.wales.builders.ResponseJsonBuilder;
import org.legaltech.wales.constants.QueryTerms;
import org.legaltech.wales.schemas.FilterRequestBody;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
	@Operation(summary = "Full list",
			description = "Full list of Swansea Uni College of Science staff members areas of expertise")
	public Response get(@DefaultValue("10") @QueryParam("size") int size) {
		return webTarget.queryParam(QueryTerms.SIZE.lowerCaseName(), size).request().get();
	}

	@POST
	@Operation(summary = "Filtered list",
			description = "Filtered list of Swansea Uni College of Science staff members areas of expertise")
	public Response post(@RequestBody(description = "Filter Data",
			content = @Content(schema = @Schema(implementation = FilterRequestBody.class))) FilterRequestBody filterRequestBody) {
		JsonNode queryNode = queryBuilder.build(filterRequestBody);
		Response response = webTarget.request().post(Entity.json(queryNode.toString()));

		if (response.getStatus() == OK.getStatusCode()) {
			JsonNode entity = responseBuilder.build(filterRequestBody, response);
			return entity != null ? Response.ok().entity(entity.toString()).build() : Response.serverError().build();
		} else {
			return response;
		}
	}
}
