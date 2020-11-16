package org.legaltech.wales.builders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.legaltech.wales.schemas.FilterRequestBody;
import org.legaltech.wales.constants.QueryTerms;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;

import static org.legaltech.wales.ElasticProxy.OBJECT_MAPPER;

@ApplicationScoped
public class QueryJsonBuilder {

	private static final String WILDCARD = "*";

	private static final ArrayNode QUERY_FIELDS = OBJECT_MAPPER.createArrayNode()
			.add(QueryTerms.NAME.lowerCaseName())
			.add(QueryTerms.EXPERTISE.lowerCaseName());

	public JsonNode build(FilterRequestBody filterRequestBody) {
		ObjectNode queryNode = OBJECT_MAPPER.createObjectNode();

		// Amount of results to return.
		queryNode.put(QueryTerms.FROM.lowerCaseName(), 0);
		queryNode.put(QueryTerms.SIZE.lowerCaseName(), filterRequestBody.getSize());

		// Create query logic for the search request.
		ObjectNode nestedQueryNode = OBJECT_MAPPER.createObjectNode();
		nestedQueryNode.set(QueryTerms.BOOL.lowerCaseName(), buildBoolNode(filterRequestBody));
		queryNode.set(QueryTerms.QUERY.lowerCaseName(), nestedQueryNode);

		return queryNode;
	}

	private JsonNode buildBoolNode(FilterRequestBody filterRequestBody) {
		ObjectNode boolNode = OBJECT_MAPPER.createObjectNode();
		String department = filterRequestBody.getDepartment();
		ArrayList<String> filterTerms = filterRequestBody.getFilterTerms();

		if (StringUtils.isNotBlank(department) && !QueryTerms.ALL.lowerCaseName().equalsIgnoreCase(department)) {
			boolNode.set(QueryTerms.MUST.lowerCaseName(), buildMustNode(department));
		}

		if (filterTerms.size() > 0) {
			boolNode.set(QueryTerms.SHOULD.lowerCaseName(), buildShouldArrayNode(filterTerms));
			boolNode.put(QueryTerms.MINIMUM_SHOULD_MATCH.lowerCaseName(), 1);
		}

		return boolNode;
	}

	// Logic: If department exists and is not all search request MUST match.
	private ArrayNode buildMustNode(String department) {
		ArrayNode mustNode = OBJECT_MAPPER.createArrayNode();
		ObjectNode matchPhraseNode = OBJECT_MAPPER.createObjectNode();
		ObjectNode departmentNode = OBJECT_MAPPER.createObjectNode();

		departmentNode.put(QueryTerms.DEPARTMENT.lowerCaseName(), department);
		matchPhraseNode.set(QueryTerms.MATCH_PHRASE.lowerCaseName(), departmentNode);
		mustNode.add(matchPhraseNode);

		return mustNode;
	}

	// Logic: If filter terms exist then request SHOULD match SOME part of these terms.
	private ArrayNode buildShouldArrayNode(ArrayList<String>  filterTerms) {
		ArrayNode shouldNode = OBJECT_MAPPER.createArrayNode();

		filterTerms.forEach(filterTerm -> {
			ObjectNode queryStringNode = OBJECT_MAPPER.createObjectNode();
			ObjectNode queryFieldNode = OBJECT_MAPPER.createObjectNode();
			queryFieldNode.put(QueryTerms.QUERY.lowerCaseName(), WILDCARD + filterTerm + WILDCARD);
			queryFieldNode.set(QueryTerms.FIELDS.lowerCaseName(), QUERY_FIELDS);
			queryStringNode.set(QueryTerms.QUERY_STRING.lowerCaseName(), queryFieldNode);
			shouldNode.add(queryStringNode);
		});

		return shouldNode;
	}
}
