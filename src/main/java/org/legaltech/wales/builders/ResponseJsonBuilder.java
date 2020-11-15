package org.legaltech.wales.builders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.legaltech.wales.constants.QueryTerms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import java.io.IOException;

@ApplicationScoped
public class ResponseJsonBuilder extends JsonBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseJsonBuilder.class);
	private static final String BUILD_RESPONSE_ERROR = "Unable to build response for query [{}]!";

	public Response build(Response response, JsonNode dataNode) {
		try {
			JsonNode responseEntity = OBJECT_MAPPER.readTree(response.readEntity(String.class));

			ObjectNode newResponseEntity = OBJECT_MAPPER.createObjectNode();
			newResponseEntity.set(QueryTerms.RESULTS.lowerCaseName(),
					responseEntity.get(QueryTerms.HITS.lowerCaseName()).get(QueryTerms.HITS.lowerCaseName()));
			newResponseEntity.set(QueryTerms.QUERY_TERMS.lowerCaseName(), getQueryTerms(dataNode));

			return Response.ok().entity(newResponseEntity.toString()).build();
		} catch (IOException e) {
			LOGGER.error(BUILD_RESPONSE_ERROR, dataNode.toString(), e);
			return Response.serverError().build();
		}
	}

	private ArrayNode getQueryTerms(JsonNode dataNode) {
		ArrayNode queryTerms = OBJECT_MAPPER.createArrayNode();

		if (dataNode.hasNonNull(QueryTerms.QUERY.lowerCaseName())) {
			JsonNode boolNode = dataNode.get(QueryTerms.QUERY.lowerCaseName()).get(QueryTerms.BOOL.lowerCaseName());

			if (boolNode.hasNonNull(QueryTerms.SHOULD.lowerCaseName())) {
				ArrayNode shouldNode = (ArrayNode) boolNode.get(QueryTerms.SHOULD.lowerCaseName());
				shouldNode.forEach(queryStringNode -> {
					String queryTerm = queryStringNode.get(QueryTerms.QUERY_STRING.lowerCaseName())
							.get(QueryTerms.QUERY.lowerCaseName()).asText();
					queryTerm = replaceChar(queryTerm, WILDCARD, StringUtils.EMPTY);

					queryTerms.add(queryTerm);
				});
			}
		}

		return queryTerms;
	}
}
