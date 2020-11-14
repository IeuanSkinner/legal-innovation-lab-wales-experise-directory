package org.legaltech.wales.builders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.legaltech.wales.constants.QueryTerms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.legaltech.wales.builders.QueryBuilder.WILDCARD;

public class ResponseBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseBuilder.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final String BUILD_RESPONSE_ERROR = "Unable to build response for query [{}]!";
	private static final String EMPTY = "";
	private static final String HITS = "hits";
	private static final String QUERY_TERMS = "queryTerms";
	private static final String RESULTS = "results";

	public static Response build(Response response, JsonNode dataNode) {
		try {
			JsonNode responseEntity = OBJECT_MAPPER.readTree(response.readEntity(String.class));

			ObjectNode newResponseEntity = OBJECT_MAPPER.createObjectNode();
			newResponseEntity.set(RESULTS, responseEntity.get(HITS).get(HITS));
			newResponseEntity.set(QUERY_TERMS, getQueryTerms(dataNode));

			return Response.ok().entity(newResponseEntity.toString()).build();
		} catch (IOException e) {
			LOGGER.error(BUILD_RESPONSE_ERROR, dataNode.toString(), e);
			return Response.serverError().build();
		}
	}

	private static ArrayNode getQueryTerms(JsonNode dataNode) {
		ArrayNode queryTerms = OBJECT_MAPPER.createArrayNode();

		if (dataNode.hasNonNull(QueryTerms.QUERY.lowerCaseName())) {
			JsonNode boolNode = dataNode.get(QueryTerms.QUERY.lowerCaseName()).get(QueryTerms.BOOL.lowerCaseName());

			if (boolNode.hasNonNull(QueryTerms.SHOULD.lowerCaseName())) {
				ArrayNode shouldNode = (ArrayNode) boolNode.get(QueryTerms.SHOULD.lowerCaseName());
				shouldNode.forEach(queryStringNode -> {
					String queryTerm = queryStringNode.get(QueryTerms.QUERY_STRING.lowerCaseName())
							.get(QueryTerms.QUERY.lowerCaseName()).asText();
					queryTerm = queryTerm.replaceAll("\\Q" + WILDCARD + "\\E", EMPTY);

					queryTerms.add(queryTerm);
				});
			}
		}

		return queryTerms;
	}
}
