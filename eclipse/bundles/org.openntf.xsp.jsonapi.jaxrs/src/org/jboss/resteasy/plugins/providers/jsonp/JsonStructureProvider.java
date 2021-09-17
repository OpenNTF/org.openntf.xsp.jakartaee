package org.jboss.resteasy.plugins.providers.jsonp;

import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.JsonWriter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Consumes({"application/json", "application/*+json", "text/json"})
@Produces({"application/json", "application/*+json", "text/json"})
public class JsonStructureProvider extends AbstractJsonpProvider implements MessageBodyReader<JsonStructure>, MessageBodyWriter<JsonStructure>
{
   @Override
   public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return JsonStructure.class.isAssignableFrom(type);
   }

   @Override
   public JsonStructure readFrom(Class<JsonStructure> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
   {
      JsonReader reader = findReader(mediaType, entityStream);
      try
      {
         return reader.read();
      }
      finally
      {
         reader.close();
      }
   }

   @Override
   public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return JsonStructure.class.isAssignableFrom(type);
   }

   @Override
   public long getSize(JsonStructure jsonStructure, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
   {
      return -1;
   }

   @Override
   public void writeTo(JsonStructure jsonStructure, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
   {
      JsonWriter writer = findWriter(mediaType, entityStream);
      try
      {
         writer.write(jsonStructure);
      }
      finally
      {
         writer.close();
      }
   }
}
