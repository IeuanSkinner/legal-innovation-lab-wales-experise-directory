package org.legaltech.wales.builders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.legaltech.wales.constants.QueryTerms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class QueryJsonBuilder extends JsonBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryJsonBuilder.class);
	private static final String DATA_READ_EXCEPTION = "Data [{}] could not be read into JSON structure.";
	private static final Pattern PATTERN = Pattern.compile("\\w+", Pattern.CASE_INSENSITIVE);
	private static final ArrayNode QUERY_FIELDS = OBJECT_MAPPER.createArrayNode()
			.add(QueryTerms.NAME.lowerCaseName())
			.add(QueryTerms.EXPERTISE.lowerCaseName());

	public JsonNode build(String data) {
		ObjectNode queryNode = OBJECT_MAPPER.createObjectNode();

		try {
			JsonNode dataNode = OBJECT_MAPPER.readTree(data);

			if (dataNode.size() > 0) {
				// Size of results to return.
				queryNode.put(QueryTerms.FROM.lowerCaseName(), 0);
				queryNode.put(QueryTerms.SIZE.lowerCaseName(), dataNode.get(QueryTerms.SIZE.lowerCaseName()).asInt());

				// Query logic for the search request.
				ObjectNode nestedQueryNode = OBJECT_MAPPER.createObjectNode();
				nestedQueryNode.set(QueryTerms.BOOL.lowerCaseName(), buildBoolNode(dataNode));
				queryNode.set(QueryTerms.QUERY.lowerCaseName(), nestedQueryNode);
				return queryNode;
			}
		} catch (IOException e) {
			LOGGER.error(DATA_READ_EXCEPTION, data, e);
		}

		return queryNode;
	}

	private JsonNode buildBoolNode(JsonNode dataNode) {
		ObjectNode boolNode = OBJECT_MAPPER.createObjectNode();
		String department = dataNode.get(QueryTerms.DEPARTMENT.lowerCaseName()).asText();
		String filterTerms = dataNode.get(QueryTerms.FILTER_TERMS.lowerCaseName()).asText();

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
