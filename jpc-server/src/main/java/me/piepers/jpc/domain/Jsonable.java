package me.piepers.jpc.domain;

import io.vertx.core.json.JsonObject;

/**
 * A simple interface with a default method to map a pojo to an instance of {@link JsonObject}
 */
//@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface Jsonable {
    default JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
