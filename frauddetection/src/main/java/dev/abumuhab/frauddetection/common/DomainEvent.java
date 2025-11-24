package dev.abumuhab.frauddetection.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@ToString
public abstract class DomainEvent {
    private final String id;

    @JsonProperty("created_at")
    private final Long createdAt;

     public DomainEvent(){
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
    }

    public static ObjectMapper getObjectMapper(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return  mapper;
    }

    public static String extractEventNameFromJsonString(String value){
        try{
            JsonNode node = getObjectMapper().readTree(value);
            return node.get("event").asText();
        }catch (Exception e){
            System.out.println("Error in extractEventNameFromJsonString: "+e.getMessage());
            return null;
        }
    }

    public static <T> T fromJsonString(String value, Class<T> valueType){
        try{
            return getObjectMapper().readValue(value,valueType);
        }catch (Exception e){
            System.out.println("Error in extractEventNameFromJsonString: "+e.getMessage());
            return null;
        }
    }
}
