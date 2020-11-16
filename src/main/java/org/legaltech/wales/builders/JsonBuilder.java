package org.legaltech.wales.builders;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class JsonBuilder {

	protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
}
