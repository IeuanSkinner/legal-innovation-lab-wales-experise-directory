package org.legaltech.wales.builders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.legaltech.wales.schemas.FilterRequestBody;
import org.legaltech.wales.constants.QueryTerms;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class QueryJsonBuilder extends JsonBuilder {

	private static final Pattern PATTERN = Pattern.compile("\\w+", Pattern.CASE_INSENSITIVE);
	private static final ArrayNode QUERY_FIELDS = OBJECT_MAPPER.createArrayNode()
			.add(QueryTerms.NAME.lowerCaseName())
			.add(QueryTerms.EXPERTISE.lowerCaseName());

	public JsonNode build(FilterRequestBody filterRequestBody) {
		ObjectNode queryNode = OBJECT_MAPPER.createObjectNode();

		// Size of results to return.
		queryNode.put(QueryTerms.FROM.lowerCaseName(), 0);
		queryNode.put(QueryTerms.SIZE.lowerCaseName(), filterRequestBody.getSize());

		// Query logic for the search request.
		ObjectNode nestedQueryNode = OBJECT_MAPPER.createObjectNode();
		nestedQueryNode.set(QueryTerms.BOOL.lowerCaseName(), buildBoolNode(filterRequestBody));
		queryNode.set(QueryTerms.QUERY.lowerCaseName(), nestedQueryNode);

		return queryNode;
	}

	private JsonNode buildBoolNode(FilterRequestBody filterRequestBody) {
		ObjectNode boolNode = OBJECT_MAPPER.createObjectNode();
		String department = filterRequestBody.getDepartment();
		String filterTerms = filterRequestBody.getFilterTerms();

		if (StringUtils.isNotBlank(department) && !QueryTerms.ALL.lowerCaseName().equalsIgnoreCase(department)) {
			boolNode.set(QueryTerms.MUST.lowerCaseName(), buildMustNode(department));
		}

		if (StringUtils.isNotBlank(filterTerms)) {
			boolNode.set(QueryTerms.SHOULD.lowerCaseName(), buildShouldArrayNode(filterTerms));
			boolNode.put(QueryTerms.MINIMUM_SHOULD_MATCH.lowerCaseName(), 1);
		}

		return boolNode;
	}

	// These are values that the search result *must* contain.
	private ArrayNode buildMustNode(String department) {
		ArrayNode mustNode = OBJECT_MAPPER.createArrayNode();
		ObjectNode matchPhraseNode = OBJECT_MAPPER.createObjectNode();
		ObjectNode departmentNode = OBJECT_MAPPER.createObjectNode();

		departmentNode.put(QueryTerms.DEPARTMENT.lowerCaseName(), department);
		matchPhraseNode.set(QueryTerms.MATCH_PHRASE.lowerCaseName(), departmentNode);
		mustNode.add(matchPhraseNode);

		return mustNode;
	}

	// These are values that the search result *should* contain at least part of.
	private ArrayNode buildShouldArrayNode(String filterTerms) {
		ArrayList<String> terms = extractTerms(filterTerms);
		ArrayNode shouldNode = OBJECT_MAPPER.createArrayNode();

		terms.forEach(filterTerm -> {
			ObjectNode queryStringNode = OBJECT_MAPPER.createObjectNode();
			ObjectNode queryFieldNode = OBJECT_MAPPER.createObjectNode();
			queryFieldNode.put(QueryTerms.QUERY.lowerCaseName(), WILDCARD + filterTerm + WILDCARD);
			queryFieldNode.set(QueryTerms.FIELDS.lowerCaseName(), QUERY_FIELDS);
			queryStringNode.set(QueryTerms.QUERY_STRING.lowerCaseName(), queryFieldNode);
			shouldNode.add(queryStringNode);
		});

		return shouldNode;
	}

	private ArrayList<String> extractTerms(String str) {
		ArrayList<String> terms = new ArrayList<>();
		Matcher matcher = PATTERN.matcher(str);

		while (matcher.find()) {
			terms.add(matcher.group());
		}

		return terms;
	}
}
