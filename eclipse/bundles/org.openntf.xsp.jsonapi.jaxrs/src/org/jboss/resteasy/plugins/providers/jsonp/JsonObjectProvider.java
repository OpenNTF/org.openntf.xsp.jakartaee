package org.jboss.resteasy.plugins.providers.jsonp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Consumes({"application/json", "application/*+json", "text/json"})
@Produces({"application/json", "application/*+json", "text/json"})
public class JsonObjectProvider extends AbstractJsonpProvider implements MessageBodyReader<JsonObject>, MessageBodyWriter<JsonObject>
{


   @Override
   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return JsonObject.class.isAssignableFrom(type);
   }

   @Override
   public JsonObject readFrom(Class<JsonObject> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
   {
      JsonReader reader = findReader(mediaType, entityStream);
      try
      {
         return reader.readObject();
      }
      finally
      {
         reader.close();
      }
   }

   @Override
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return JsonObject.class.isAssignableFrom(type);
   }

   @Override
   public long getSize(JsonObject jsonObject, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return -1;
   }

   @Override
   public void writeTo(JsonObject jsonObject, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
   {
      JsonWriter writer = findWriter(mediaType, entityStream);
      try
      {
         writer.writeObject(jsonObject);
      }
      finally
      {
         writer.close();
      }
   }
}
