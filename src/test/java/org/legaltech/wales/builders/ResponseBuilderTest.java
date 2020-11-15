package org.legaltech.wales.builders;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static javax.ws.rs.core.Response.Status.OK;
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
		String request = Files.readString(WORKING_DIR.resolve("request.json"));
		String response = Files.readString(WORKING_DIR.resolve("response.json"));
		String output = Files.readString(WORKING_DIR.resolve("output.json"));

		when(mockResponse.readEntity(String.class)).thenReturn(response);

		Response serviceResponse = RESPONSE_JSON_BUILDER.build(mockResponse, OBJECT_MAPPER.readTree(request));

		assertEquals(OK.getStatusCode(), serviceResponse.getStatus());
		assertEquals(OBJECT_MAPPER.readTree(output).toString(), serviceResponse.getEntity());
	}
}