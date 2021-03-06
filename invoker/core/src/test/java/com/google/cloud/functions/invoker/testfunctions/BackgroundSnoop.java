package com.google.cloud.functions.invoker.testfunctions;

import com.google.cloud.functions.Context;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Extract the targetFile property from the data of the JSON payload, and write to it a JSON
 * encoding of this payload and the context. The JSON format is chosen to be identical to the
 * EventFlow format that we currently use in GCF, and the file that we write should in fact be
 * identical to the JSON payload that the Functions Framework received from the client in the test.
 * This will need to be rewritten when we switch to CloudEvents.
 */
public class BackgroundSnoop {
  public void snoop(JsonElement jsonElement, Context context) throws IOException {
    String targetFile = jsonElement.getAsJsonObject().get("targetFile").getAsString();
    if (targetFile == null) {
      throw new IllegalArgumentException("Expected targetFile in JSON payload");
    }
    JsonObject resourceJson = JsonParser.parseString(context.resource()).getAsJsonObject();
    JsonObject contextJson = new JsonObject();
    contextJson.addProperty("eventId", context.eventId());
    contextJson.addProperty("timestamp", context.timestamp());
    contextJson.addProperty("eventType", context.eventType());
    contextJson.add("resource", resourceJson);
    JsonObject contextAndPayloadJson = new JsonObject();
    contextAndPayloadJson.add("data", jsonElement);
    contextAndPayloadJson.add("context", contextJson);
    try (FileWriter fileWriter = new FileWriter(targetFile);
        PrintWriter writer = new PrintWriter(fileWriter)) {
      writer.println(contextAndPayloadJson);
    }
  }
}
