package com.regnosys.rosetta.common.serialisation;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.regnosys.rosetta.common.util.StringExtensions;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.meta.GlobalKeyFields;
import com.rosetta.model.lib.meta.Key;
import com.rosetta.model.lib.meta.Reference;
import com.rosetta.model.lib.meta.ReferenceWithMeta;
import com.rosetta.model.lib.records.Date;
import com.rosetta.model.lib.records.DateImpl;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A lazy-loading holder that returns a pre-configured {@link ObjectMapper} that serves as the default when
 * serialising/deserializing Rosetta Model Objects.
 */
public class RosettaObjectMapper {

	private RosettaObjectMapper() {
	}

	/**
	 * Creating new RosettaObjectMapper instances is expensive, use the singleton instance if possible.
	 */
	public static ObjectMapper getNewRosettaObjectMapper() {
		return new ObjectMapper().findAndRegisterModules()
								.registerModule(new GuavaModule())
								.registerModule(new JodaModule())
								.registerModule(new AfterburnerModule())
								.registerModule(new ParameterNamesModule())
								.registerModule(new Jdk8Module())
								.registerModule(new JavaTimeModule())
								.registerModule(new RosettaModule())
								.registerModule(new RosettaDateModule())
								.setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
								.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
								.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
								.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
								.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)
								.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
								.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
								//The next three lines add in a filter that excludes the value from a serialised ReferenceWith object if the reference is set
								//the tests for these are in the rosetta-translate project where we have actual rosettaObjects to play with
								.setFilterProvider(new SimpleFilterProvider().addFilter("ReferenceFilter", new ReferenceFilter()))
								.addMixIn(ReferenceWithMeta.class, ReferenceWithMetaMixIn.class)
								.addMixIn(GlobalKeyFields.class, GlobalKeyFieldsMixIn.class)
								.addMixIn(Key.class, KeyMixIn.class)
								.addMixIn(Reference.class, ReferenceMixIn.class)
								.setVisibility(PropertyAccessor.ALL, Visibility.PUBLIC_ONLY);

	}

	@Deprecated
	public static ObjectMapper getDefaultRosettaObjectMapper() {
		return getNewRosettaObjectMapper();
	}

	/**
	 * Using a module class to append our annotation introspector with a minimal fuss
	 */
	private static class RosettaModule extends SimpleModule {

		private static final long serialVersionUID = 1L;

		public RosettaModule() {
			super(PackageVersion.VERSION);
		}

		@Override
		public void setupModule(SetupContext context) {
			super.setupModule(context);
			context.insertAnnotationIntrospector(new RosettaBuilderIntrospector());
		}

		@Override
		public int hashCode() {
			return getClass().hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return this == o;
		}
	}

	private static class RosettaBuilderIntrospector extends JacksonAnnotationIntrospector {

		private static final long serialVersionUID = 1L;

		@Override
		public Class<?> findPOJOBuilder(AnnotatedClass ac) {
			JavaType type = ac.getType();
			// [Ljava.lang.String  type is null!
			if (null != type) {
				Class<?> rawClass = type.getRawClass();

				if (RosettaModelObject.class.isAssignableFrom(rawClass)) {
					try {
						String builderName = null;
						if (rawClass.getName().endsWith("Builder")) builderName = rawClass.getName()+"Impl";
						else if (rawClass.getName().endsWith("Impl")) builderName = rawClass.getName().replaceAll("Impl$", "BuilderImpl");
						else builderName = rawClass.getTypeName() + "$" + rawClass.getSimpleName() + "BuilderImpl";
						
						return Class.forName(builderName,
								true, rawClass.getClassLoader());

					} catch (ClassNotFoundException e) {
						throw new RosettaSerialiserException("Could not find the builder class for " + rawClass, e);
					}
				}
			}
			return super.findPOJOBuilder(ac);
		}
		public PropertyName findNameForDeserialization(Annotated a)
		{
			if (a instanceof AnnotatedMethod) {
				AnnotatedMethod am = (AnnotatedMethod)a;
				if (am.getParameterCount()==1) {
					if (am.getName().startsWith("set") && RosettaModelObject.class.isAssignableFrom(am.getDeclaringClass())) {
						String firstLower = StringExtensions.toFirstLower(am.getName().substring(3));
						if (firstLower .equals("key") && GlobalKeyFields.class.isAssignableFrom(am.getDeclaringClass())) {
							firstLower = "location";
						}
						if (firstLower.equals("reference") && ReferenceWithMeta.class.isAssignableFrom(am.getDeclaringClass())) {
							firstLower = "address";
						}
						return new PropertyName(firstLower);
					}
				}
			}
			return super.findNameForDeserialization(a);
		}
		
		public JsonIgnoreProperties.Value findPropertyIgnorals(Annotated ac)
		{
			if (ac instanceof AnnotatedClass) {
				AnnotatedClass acc = (AnnotatedClass) ac;
				Set<String> names = StreamSupport.stream(acc.memberMethods().spliterator(), false)
					.map(m->BeanUtil.getPropertyName(m.getAnnotated()))
					.filter(n->n!=null)
					.filter(n->n.startsWith("orCreate") || n.startsWith("type")|| n.startsWith("valueType"))
					.collect(Collectors.toSet());
				return JsonIgnoreProperties.Value.forIgnoredProperties(names).withAllowSetters();
			}
			if (ac instanceof AnnotatedMethod) {
				AnnotatedMethod am = (AnnotatedMethod) ac;
				String propertyName = BeanUtil.getPropertyName(am.getAnnotated());
				if (propertyName!=null && propertyName.startsWith("orCreate")) {
					return JsonIgnoreProperties.Value.forIgnoredProperties(propertyName).withAllowSetters();
				}
			}
			return JsonIgnoreProperties.Value.empty();
		}

		@Override
		public Version version() {
			return Version.unknownVersion();
		}
	}

	private static class RosettaSerialiserException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		RosettaSerialiserException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	//TODO remove after Date becomes a class with a default constructor
	private static class RosettaDateModule extends SimpleModule {
		private static final long serialVersionUID = 1L;

		{
			addDeserializer(Date.class, new StdDeserializer<Date>(Date.class) {
				private static final long serialVersionUID = 1L;

				@Override
				public Date deserialize(JsonParser p, DeserializationContext ctxt)
						throws IOException, JsonProcessingException {
					return new DateImpl(p.readValueAs(DateExtended.class).toLocalDate());
				}
			});
		}
	}

	//TODO remove after Date becomes a class with a default constructor
	public static class DateExtended extends DateImpl {
		public DateExtended(@JsonProperty("day") int day, @JsonProperty("month") int month, @JsonProperty("year") int year) {
			super(day, month, year);
		}

		public DateExtended(String date) {
			super(LocalDate.parse(date));
		}
	}
	
	//This class serves to ensure that the value of a reference doesn't get serialized if the
	//reference or global key field is populated
	public static class ReferenceFilter extends SimpleBeanPropertyFilter {

		@Override
		public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider,
				PropertyWriter writer) throws Exception {
			if (!filterOut(pojo, writer.getName())) {
				writer.serializeAsField(pojo, jgen, provider);
			}
		}

		@Override
		public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider provider,
				BeanPropertyWriter writer) throws Exception {
			if (!filterOut(bean, writer.getName())) {
				writer.serializeAsField(bean, jgen, provider);
			}
		}

		private boolean filterOut(Object pojo, String name) {
			if (!name.equals("value")) return false;
			if (pojo instanceof ReferenceWithMeta) {
				return hasReference((ReferenceWithMeta<?>)pojo);
			}
			return false;
		}

		private boolean hasReference(ReferenceWithMeta<?> pojo) {
			return pojo.getGlobalReference()!=null || (pojo.getReference()!=null && pojo.getReference().getReference()!=null);
		}
	}

	private interface GlobalKeyFieldsMixIn {
		@JsonProperty("location")
		List<Key> getKey();
	}


	private abstract interface KeyMixIn {
		@JsonProperty("value")
		String getKeyValue();
		
		@JsonIgnore
		Class<? extends RosettaModelObject> getType();
	}

	@JsonFilter("ReferenceFilter")
	private interface ReferenceWithMetaMixIn {
		@JsonProperty("address")
		Reference getReference();
	}

	/*@JsonFilter("ReferenceFilter")
	private interface ReferenceWithMetaBuilderMixIn {
		@JsonProperty("address")
		ReferenceBuilder getReference();
	}*/

	private abstract interface ReferenceMixIn {
		@JsonProperty("value")
		String getReference();
	}
}
