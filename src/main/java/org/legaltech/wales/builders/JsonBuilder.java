package org.legaltech.wales.builders;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class JsonBuilder {

	protected static final String WILDCARD = "*";

	protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	protected String replaceChar(String str, String character, String replacement) {
		return str.replaceAll("\\Q" + character + "\\E", replacement);
	}
}
