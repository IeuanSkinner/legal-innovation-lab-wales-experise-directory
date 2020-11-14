package org.legaltech.wales.constants;

public enum QueryTerms {
	ALL,
	BOOL,
	DEPARTMENT,
	EXPERTISE,
	FIELDS,
	FILTER_TERMS,
	FROM,
	HITS,
	MATCH_PHRASE,
	MINIMUM_SHOULD_MATCH,
	MUST,
	NAME,
	QUERY,
	QUERY_STRING,
	QUERY_TERMS,
	RESULTS,
	SIZE,
	SHOULD;

	public String lowerCaseName() {
		return name().toLowerCase();
	}
}
