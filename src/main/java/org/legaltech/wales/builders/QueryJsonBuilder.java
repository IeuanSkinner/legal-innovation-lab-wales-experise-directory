package org.legaltech.wales.builders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.legaltech.wales.constants.QueryTerms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.Arrays;

@ApplicationScoped
public class QueryJsonBuilder extends JsonBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryJsonBuilder.class);

	private static final String DATA_READ_EXCEPTION = "Data [{}] could not be read into JSON structure.";

	private static final int MINIMUM_MATCH = 1;
	private static final String SEPARATOR = "[\\s,]+";
	private static final String SPACE = " ";
	private static final int START = 0;

	private static final ArrayNode QUERY_FIELDS = OBJECT_MAPPER.createArrayNode()
			.add(QueryTerms.NAME.lowerCaseName()).add(QueryTerms.EXPERTISE.lowerCaseName());
	private static final String[] ESCAPE_CHARS = new String[] {
			"+","-","=","&&","||",">","<","!","(",")","{","}","[","]","^","\"","~","*","?",":","\\","/"
	};

	public JsonNode build(String data) {
		ObjectNode queryNode = OBJECT_MAPPER.createObjectNode();

		try {
			JsonNode dataNode = OBJECT_MAPPER.readTree(data);

			if (dataNode.size() > 0) {
				// Size of results to return.
				queryNode.put(QueryTerms.FROM.lowerCaseName(), START);
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

		if (dataNode.hasNonNull(QueryTerms.DEPARTMENT.lowerCaseName())) {
			String department = dataNode.get(QueryTerms.DEPARTMENT.lowerCaseName()).asText();

			if (!QueryTerms.ALL.lowerCaseName().equalsIgnoreCase(department)) {
				boolNode.set(QueryTerms.MUST.lowerCaseName(), buildMustNode(department));
			}
		}

		if (dataNode.hasNonNull(QueryTerms.FILTER_TERMS.lowerCaseName())) {
			boolNode.set(QueryTerms.SHOULD.lowerCaseName(), buildShouldArrayNode(dataNode));
			boolNode.put(QueryTerms.MINIMUM_SHOULD_MATCH.lowerCaseName(), MINIMUM_MATCH);
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
	private ArrayNode buildShouldArrayNode(JsonNode dataNode) {
		String filterTerms = clean(dataNode.get(QueryTerms.FILTER_TERMS.lowerCaseName()).asText());
		ArrayNode shouldNode = OBJECT_MAPPER.createArrayNode();

		Arrays.stream(filterTerms.split(SEPARATOR)).forEach(filterTerm -> {
			ObjectNode queryStringNode = OBJECT_MAPPER.createObjectNode();
			ObjectNode queryFieldNode = OBJECT_MAPPER.createObjectNode();
			queryFieldNode.put(QueryTerms.QUERY.lowerCaseName(), WILDCARD + filterTerm + WILDCARD);
			queryFieldNode.set(QueryTerms.FIELDS.lowerCaseName(), QUERY_FIELDS);
			queryStringNode.set(QueryTerms.QUERY_STRING.lowerCaseName(), queryFieldNode);
			shouldNode.add(queryStringNode);
		});

		return shouldNode;
	}

	private String clean(String str) {
		for (String escapeChar : ESCAPE_CHARS) {
			str = replaceChar(str, escapeChar, SPACE);
		}
		return str;
	}
}
