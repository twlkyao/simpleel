/*
 * Copyright 1999-2101 Alibaba Group.
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
package com.alibaba.simpleEL.preprocess;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.simpleEL.JavaSource;
import com.alibaba.simpleEL.Preprocessor;

/**
 * @author wenshao<szujobs@hotmail.com>
 *
 */
public class TemplatePreProcessor implements Preprocessor {
	public final static String DEFAULT_TEMPLATE = "com/alibaba/simpleEL/Expr.java.template";
	public final static String DEFAULT_PACKAGE_NAME = "com.alibaba.simpleEL.gen";
	
	private String templdateResource = DEFAULT_TEMPLATE;

	protected transient String template;
	protected String packageName = DEFAULT_PACKAGE_NAME;

	protected final AtomicLong classIdSeed = new AtomicLong(10000L);

	protected VariantResolver variantResolver = new DefaultVariantResolver();
	
	protected boolean allowMultiStatement = false;
	

	public boolean isAllowMultiStatement() {
		return allowMultiStatement;
	}

	public void setAllowMultiStatement(boolean allowMuliStatement) {
		this.allowMultiStatement = allowMuliStatement;
	}

	public VariantResolver getVariantResolver() {
		return variantResolver;
	}

	public void setVariantResolver(VariantResolver variantResolver) {
		this.variantResolver = variantResolver;
	}

	public String digits() {
		return Long.toString(classIdSeed.incrementAndGet());
	}

	public TemplatePreProcessor() {
		try {
			this.template = this.loadTemplate();
		} catch (IOException e) {
			throw new IllegalStateException("load template error", e);
		}
	}

	public String getTemplate() {
		return template;
	}

	@Override
	public JavaSource handle(Map<String, Object> context, String expr) {
		if (variantResolver == null) {
			throw new IllegalStateException("variantResolver is null");
		}

		String resolvedExpr = ExpressUtils.resolve(expr, variantResolver);
		
		if (!allowMultiStatement) {
			resolvedExpr = "return " + resolvedExpr + ";";
		}

		final String className = "GenClass_" + digits();

		String source = template.replace("$packageName", packageName)//
				.replace("$className", className)//
				.replace("$expression", resolvedExpr);

		JavaSource javaSource = new JavaSource(packageName, className, source);

		return javaSource;
	}

	public String fillTemplate(String packageName, String className, String expression) {
		String source = template.replace("$packageName", packageName)//
				.replace("$className", className)//
				.replace("$expression", expression);
		return source;
	}

	private String loadTemplate() throws IOException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		InputStream is = null;

		is = loader.getResourceAsStream(templdateResource);
		int size = is.available();
		byte bytes[] = new byte[size];

		if (size != is.read(bytes, 0, size)) {
			throw new IOException();
		}

		String template = new String(bytes, "UTF-8");

		return template;
	}
}
