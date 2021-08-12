package org.jboss.resteasy.plugins.providers.jsonp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.json.JsonNumber;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
@Consumes({"application/json", "application/*+json", "text/json"})
@Produces({"application/json", "application/*+json", "text/json"})
public class JsonValueProvider extends AbstractJsonpProvider implements MessageBodyReader<JsonValue>, MessageBodyWriter<JsonValue>
{   @Override
   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return JsonNumber.class.isAssignableFrom(type) || JsonString.class.isAssignableFrom(type);
   }

   @Override
   public JsonValue readFrom(Class<JsonValue> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
   {
      JsonReader reader = findReader(mediaType, entityStream);
      try
      {
         return reader.readValue();
      }
      finally
      {
         reader.close();
      }
   }

   @Override
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return JsonNumber.class.isAssignableFrom(type) || JsonString.class.isAssignableFrom(type);
   }

   @Override
   public long getSize(JsonValue jsonStructure, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return -1;
   }

   @Override
   public void writeTo(JsonValue jsonValue, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
   {
      JsonWriter writer = findWriter(mediaType, entityStream);
      try
      {
         writer.write(jsonValue);
      }
      finally
      {
         writer.close();
      }
   }

}
