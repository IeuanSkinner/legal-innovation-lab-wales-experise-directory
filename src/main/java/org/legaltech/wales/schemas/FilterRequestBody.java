package org.legaltech.wales.schemas;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Schema(name = "FilterRequestBody",
		description = "POJO that represents the filtering options this API accepts as JSON in the request body")
public class FilterRequestBody {

	@Schema(required = true, example = "10")
	private int size;

	@Schema(example = "compsci")
	private String department;

	@Schema(example = "Machine Learning, AI")
	private ArrayList<String> filterTerms;

	public void setFilterTerms(String filterTerms) {
		this.filterTerms = new ArrayList<>();
		Matcher matcher = Pattern.compile("\\w+", Pattern.CASE_INSENSITIVE).matcher(filterTerms);

		while (matcher.find()) {
			this.filterTerms.add(matcher.group());
		}
	}
}
