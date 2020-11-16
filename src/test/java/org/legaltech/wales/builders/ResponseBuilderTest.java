package org.legaltech.wales.builders;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.legaltech.wales.schemas.FilterRequestBody;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.legaltech.wales.builders.JsonBuilder.OBJECT_MAPPER;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResponseBuilderTest {

	@Mock private Response mockResponse;

	private static final ResponseJsonBuilder RESPONSE_JSON_BUILDER = new ResponseJsonBuilder();
	private static final Path WORKING_DIR = Path.of("", "src/test/resources/responseJsonBuilder");

	@DisplayName("Verify JSON response entity contains the result set from ES cluster response along with the query" +
			"terms that were sent")
	@Test
	void buildResponseTest() throws IOException {
		String input = Files.readString(WORKING_DIR.resolve("input.json"));
		String response = Files.readString(WORKING_DIR.resolve("response.json"));
		String output = Files.readString(WORKING_DIR.resolve("output.json"));
		FilterRequestBody filterRequestBody = OBJECT_MAPPER.readValue(input, FilterRequestBody.class);
		JsonNode expectedOutput = OBJECT_MAPPER.readTree(output);

		when(mockResponse.readEntity(String.class)).thenReturn(response);

		JsonNode entity = RESPONSE_JSON_BUILDER.build(filterRequestBody, mockResponse);

		assertEquals(expectedOutput, entity);
	}
}