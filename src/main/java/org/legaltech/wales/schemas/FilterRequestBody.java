package org.legaltech.wales.schemas;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@Schema(name = "FilterRequestBody",
		description = "POJO that represents the filtering options this API accepts as JSON in the request body")
public class FilterRequestBody {

	@Schema(required = true, example = "10")
	private int size;

	@Schema(example = "compsci")
	private String department;

	@Schema(example = "Machine Learning, AI")
	private String filterTerms;
}
