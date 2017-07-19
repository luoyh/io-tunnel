package org.luoyh.tunnel.utils;

import java.text.SimpleDateFormat;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author luoyh(Roy) - Jul 18, 2017
 */
public final class JsonUtils {
	
	private static final ObjectMapper mapper;
	
	static {
		mapper = new ObjectMapper();
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	}
	
	public static <T> String toJson(T t) {
		try {
			return mapper.writeValueAsString(t);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JsonException(e.getMessage());
		}
	}
	
	public static <T> T toObject(String json, Class<T> clazz) {
		try {
			return mapper.readValue(json, clazz);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JsonException(e.getMessage());
		}
	}
	
	public static <T> List<T> toList(String json, TypeReference<T> valueTypeRef) {
		try {
			return mapper.readValue(json, valueTypeRef);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JsonException(e.getMessage());
		}
		
	}
}

class JsonException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public JsonException(String msg) {
		super(msg);
	}
	
}
