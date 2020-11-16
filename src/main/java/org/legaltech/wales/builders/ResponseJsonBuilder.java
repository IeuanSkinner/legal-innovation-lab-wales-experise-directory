package org.legaltech.wales.builders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.legaltech.wales.constants.QueryTerms;
import org.legaltech.wales.schemas.FilterRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import java.io.IOException;

@ApplicationScoped
public class ResponseJsonBuilder extends JsonBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseJsonBuilder.class);
	private static final String BUILD_RESPONSE_ERROR = "Unable to build response for query [{}]!";

	public JsonNode build(FilterRequestBody filterRequestBody, Response response) {
		try {
			JsonNode responseEntity = OBJECT_MAPPER.readTree(response.readEntity(String.class));

			ObjectNode newResponseEntity = OBJECT_MAPPER.createObjectNode();
			newResponseEntity.set(
					QueryTerms.RESULTS.lowerCaseName(),
					responseEntity.get(QueryTerms.HITS.lowerCaseName()).get(QueryTerms.HITS.lowerCaseName())
			);
			newResponseEntity.set(QueryTerms.QUERY_TERMS.lowerCaseName(),
					OBJECT_MAPPER.convertValue(filterRequestBody.getFilterTerms(), ArrayNode.class)
			);

			return newResponseEntity;
		} catch (IOException e) {
			LOGGER.error(BUILD_RESPONSE_ERROR, filterRequestBody.toString(), e);
		}

		return null;
	}
}
