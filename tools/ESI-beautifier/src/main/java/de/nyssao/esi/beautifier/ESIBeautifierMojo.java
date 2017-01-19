package de.nyssao.esi.beautifier;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Goal which touches a timestamp file.
 *
 */
@Mojo(name = "touch", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class ESIBeautifierMojo extends AbstractMojo {
	
	@Parameter(defaultValue = "nice.swagger.json", property = "outputFile", required = true)
	private File outputFile;

	@Parameter(defaultValue = "swagger.json", property = "inputFile", required = true)
	private File inputFile;

	public ESIBeautifierMojo() {
		
	}
	
	public ESIBeautifierMojo(File inputFile, File outputFile) {
		this();
		
		this.inputFile  = inputFile;
		this.outputFile = outputFile;
	}
	
	public void execute() throws MojoExecutionException {
		File outputDirectory = outputFile.getParentFile();

		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
		
		SwaggerParser swaggerParser = new SwaggerParser();
		Swagger       swagger       = swaggerParser.read(inputFile.getAbsolutePath());
		ESIBeautifier beautifier    = new ESIBeautifier();
		
		swagger = beautifier.makePretty(swagger);
		
		String swaggerString = Json.pretty(swagger);
		
		try {
			Files.write(outputFile.toPath(), swaggerString.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(swagger.getResponses());
		
//		File f = outputDirectory;
//
//		if (!f.exists()) {
//			f.mkdirs();
//		}
//
//		File touch = new File(f, "touch.txt");
//
//		FileWriter w = null;
//		try {
//			w = new FileWriter(touch);
//
//			w.write("touch.txt");
//		} catch (IOException e) {
//			throw new MojoExecutionException("Error creating file " + touch, e);
//		} finally {
//			if (w != null) {
//				try {
//					w.close();
//				} catch (IOException e) {
//					// ignore
//				}
//			}
//		}
	}
}
