package org.legaltech.wales.constants;

public enum QueryTerms {
	BOOL,
	FIELDS,
	FROM,
	MATCH_PHRASE,
	MINIMUM_SHOULD_MATCH,
	MUST,
	QUERY,
	QUERY_STRING,
	SIZE,
	SHOULD;

	public String lowerCaseName() {
		return name().toLowerCase();
	}
}
