package org.legaltech.wales.builders;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.legaltech.wales.builders.JsonBuilder.OBJECT_MAPPER;

class QueryJsonBuilderTest {

	private static final QueryJsonBuilder QUERY_JSON_BUILDER = new QueryJsonBuilder();
	private static final Path WORKING_DIR = Path.of("", "src/test/resources/queryJsonBuilder");

	@DisplayName("Verify JSON query is of expected format for the given input")
	@ParameterizedTest
	@ValueSource(strings = {"allDept.json", "compSciDept.json", "biosciencesDeptNoFilterTerms.json"})
	void buildQueryTest(String fileName) throws IOException {
		String input = Files.readString(WORKING_DIR.resolve("input/" + fileName));
		String output = Files.readString(WORKING_DIR.resolve("output/" + fileName));
		JsonNode outputNode = OBJECT_MAPPER.readTree(output);

		assertEquals(outputNode, QUERY_JSON_BUILDER.build(input));
	}
}