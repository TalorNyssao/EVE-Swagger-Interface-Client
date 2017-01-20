package de.nyssao.esi.beautifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;

public class ESIBeautifier {

	protected Model internalServerErrorModel;
	protected Model notFoundModel;
	protected Model forbiddenModel;
	protected Model unprocessableEntityModel;

	public static void main(String[] args) throws IOException {
		File          file    = new File("esi.json");
		//System.setProperty("debugParser", "true");
		System.out.println(file.getAbsolutePath());
		
		SwaggerParser swaggerParser = new SwaggerParser();
		Swagger       swagger       = swaggerParser.read("./esi.json");
		ESIBeautifier master        = new ESIBeautifier();
		
		
		String swaggerString = Json.pretty(master.makePretty(swagger));
		Files.write(Paths.get("./pretty.esi.json"), swaggerString.getBytes());
	}
	
	public ESIBeautifier() {
		internalServerErrorModel = new ModelImpl();
		internalServerErrorModel.setTitle("internal_server_error");
		internalServerErrorModel.setDescription("Internal server error");
//		StringProperty internalServerErrorProperty = new StringProperty();
//		internalServerErrorProperty.set		
		
		notFoundModel = new ModelImpl();
		notFoundModel.setTitle("not_found");
		notFoundModel.setDescription("Not found");
		
		forbiddenModel = new ModelImpl();
		forbiddenModel.setTitle("forbidden");
		forbiddenModel.setDescription("Forbidden");		
		
		unprocessableEntityModel = new ModelImpl();
		unprocessableEntityModel.setTitle("unprocessable_entity");
		unprocessableEntityModel.setDescription("Unprocessable entity");
	}
	
	public Swagger makePretty(Swagger swagger) {	
		String version = swagger.getInfo().getVersion();
		
		for (Entry<String, Path> pathEntry : swagger.getPaths().entrySet()) {
			String pathName = pathEntry.getKey();
			Path   path     = pathEntry.getValue();
			
			List<String> pathParameterNames = new ArrayList<String>();
			
			 Pattern p = Pattern.compile(".*\\{([\\w_]+?)\\}.*");
			 Matcher m = p.matcher(pathName);
			 if (m.matches()) {
				 for (int i = 1; i <= m.groupCount(); i++) {
					 String        parameterName = m.group(i);
					 pathParameterNames.add(parameterName);
//					 PathParameter parameter = new PathParameter();
//					 parameter.allowEmptyValue(false);
//					 parameter.setName(parameterName);
//					 path.addParameter(parameter);
				 }
			 } 
			
			for (Operation operation : path.getOperations()) {
				String name = operation.getOperationId();
				operation.setOperationId(prettyOperationName(name));
				
				for (Parameter parameter : operation.getParameters()) {
					
				}

				for (Response response : operation.getResponses().values()) {
					Property schema = response.getSchema();
					//schema = new RefProperty("#/definitions/Message");
					
					if (schema != null) {
						Property newSchema = convertSchema(schema);
						
						if (newSchema != schema) {
							response.setSchema(newSchema);
						}
					}
				}
			}						
		}		
		
		swagger.addDefinition("internal_server_error", internalServerErrorModel);
		swagger.addDefinition("not_found", notFoundModel);
		swagger.addDefinition("forbidden", forbiddenModel);
		swagger.addDefinition("unprocessable_entity", unprocessableEntityModel);
				
		return swagger;
	}

	private void mapProperties(ObjectProperty input, Model output) {
		for (Entry<String, Property> entry : input.getProperties().entrySet()) {
			String   name     = entry.getKey();
			Property property = entry.getValue();
			
			if (output.getProperties() != null) {
				if (!output.getProperties().containsKey(name)) {
					output.getProperties().put(name, property);
				}
			} else {
				Map<String,Property> properties = new HashMap<String, Property>();
				properties.put(name, property);
				output.setProperties(properties);
			}
		}
	}
	
	private Property convertSchema(Property schema) {
		Property result     = schema;
		String   schemaName = schema.getTitle();
		
		if (schemaName.contains("internal_server_error")) {
			result = new RefProperty("#/definitions/internal_server_error");
			mapProperties((ObjectProperty) schema, internalServerErrorModel);
			
		} else if (schemaName.contains("not_found")) {
			result = new RefProperty("#/definitions/not_found");
			mapProperties((ObjectProperty) schema, notFoundModel);
			
		} else if (schemaName.contains("forbidden")) {
			result = new RefProperty("#/definitions/forbidden");
			mapProperties((ObjectProperty) schema, forbiddenModel);
		
		} else if (schemaName.contains("unprocessable_entity")) {
			result = new RefProperty("#/definitions/unprocessable_entity");
			mapProperties((ObjectProperty) schema, unprocessableEntityModel);					
			
		} else {
			schema.setTitle(prettySchemaName(schemaName));
			
			if (schema instanceof ArrayProperty) {
				ArrayProperty arrayProperty = (ArrayProperty) schema;
				arrayProperty.getItems().getTitle();
				
				String itemName = arrayProperty.getItems().getTitle();
				arrayProperty.getItems().setTitle(prettySchemaName(itemName));
			}
		}
		
		return result;
	}
	
	public String prettyOperationName(String input) {
		String result = input.replaceAll("s_[^_]+_id", "");
		
		result = result.replaceAll("_[^_]+_id", "");
		
		if (!result.equals(input)) {
			System.out.println("OP: " + input + " -> " + result);
		}
		
		return result;
	}
	
	public String prettySchemaName(String input) {
		String result = input;
		
		result = result.replaceAll("s_[^_]+_id", "");
		result = result.replaceAll("_[^_]+_id", "");			
		
		if (result.startsWith("get_")) {
			result = result.substring(4, result.length());
		}
		
		if (result.endsWith("_200_ok")) {
			result = result.substring(0, result.length() - 7);
			
		} else if (result.endsWith("_ok")) {
			result = result.substring(0, result.length() - 3);
		}						
	
		
		if (result.endsWith("s")) {
			result = result.substring(0, result.length() - 1);
		}
		
		if (!result.equals(input)) {
			System.out.println("SH: " + input + " -> " + result);
		}
		
		return result;
	}

}
