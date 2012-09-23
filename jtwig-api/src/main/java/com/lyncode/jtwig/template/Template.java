/**
 * Copyright 2012 Lyncode
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lyncode.jtwig.template;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.parboiled.common.FileUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.lyncode.jtwig.exceptions.JtwigParsingException;
import com.lyncode.jtwig.exceptions.JtwigRenderException;
import com.lyncode.jtwig.exceptions.TemplateBuildException;
import com.lyncode.jtwig.manager.ResourceManager;
import com.lyncode.jtwig.manager.ServletContextResourceManager;
import com.lyncode.jtwig.parser.JtwigParser;
import com.lyncode.jtwig.tree.JtwigBlock;
import com.lyncode.jtwig.tree.JtwigContent;
import com.lyncode.jtwig.tree.JtwigElement;
import com.lyncode.jtwig.tree.JtwigExtends;
import com.lyncode.jtwig.tree.JtwigInclude;
import com.lyncode.jtwig.tree.JtwigRoot;

/**
 * @author "João Melo <jmelo@lyncode.com>"
 *
 */
public class Template {
	private ServletContext servletContext;
	private ResourceManager resources;
	private JtwigRoot resolved;
	
	public Template (ServletContext servletContext, String filename) throws TemplateBuildException  {
		try {
			this.servletContext = servletContext;
			this.resources = new ServletContextResourceManager(servletContext, filename);
			resolved = this.resolve();
		} catch (TemplateBuildException e) {
			throw new TemplateBuildException(e);
		}
	}
	
	private Template loadTemplate (String relativePath) throws TemplateBuildException {
		try {
			return new Template(servletContext, resources.getFile(relativePath));
		} catch (IOException e) {
			throw new TemplateBuildException(e);
		}
	}

	private void replaceIncludes (JtwigContent content) throws TemplateBuildException {
		Collection<JtwigElement> includes = Collections2.filter(content.getChilds(), new Predicate<JtwigElement>() {
			public boolean apply(JtwigElement input) {
				return (input instanceof JtwigInclude);
			}
		});
		
		for (JtwigElement inc : includes) {
			JtwigInclude icl = (JtwigInclude) inc;
			Template t = this.loadTemplate(icl.getTemplateName());
			JtwigRoot parent = t.resolve();
			content.replace(icl, parent);
		}
		
		Collection<JtwigElement> contents = Collections2.filter(content.getChilds(), new Predicate<JtwigElement>() {
			public boolean apply(JtwigElement input) {
				return (input instanceof JtwigContent);
			}
		});
		
		for (JtwigElement ct : contents) {
			JtwigContent icl = (JtwigContent) ct;
			this.replaceIncludes(icl);
		}
	}
	
	private JtwigRoot resolve () throws TemplateBuildException {
		byte[] readed;
		try {
			readed = FileUtils.readAllBytes(resources.getResource());
		} catch (IOException e1) {
			throw new TemplateBuildException(e1);
		}
		String input = new String(readed);
		JtwigRoot root;
		try {
			root = JtwigParser.parse(input);
		} catch (JtwigParsingException e) {
			throw new TemplateBuildException(e);
		}
		if (root.getChilds().isEmpty()) {
			return root;
		} else {
			// Search the tree for Includes and replace them
			this.replaceIncludes(root);
			
			// Search the root tree for Blocks and replace them
			List<JtwigElement> elements = root.getChilds();
			if (elements.get(0) instanceof JtwigExtends) {
				// Extension template
				JtwigExtends ext = (JtwigExtends) elements.get(0);
				Template t = this.loadTemplate(ext.getTemplateName());
				JtwigRoot parent = t.resolve();
				
				Collection<JtwigElement> masterBlocks = Collections2.filter(parent.getChilds(), new Predicate<JtwigElement>() {
					public boolean apply(JtwigElement input) {
						return (input instanceof JtwigBlock);
					}
				});
				
				Collection<JtwigElement> thisBlocks = Collections2.filter(elements, new Predicate<JtwigElement>() {
					public boolean apply(JtwigElement input) {
						return (input instanceof JtwigBlock);
					}
				});
				
				if (thisBlocks.size() > elements.size() + 1)
					throw new TemplateBuildException(this.resources.getPath() + " template with unexpected constructions");
				
				for (JtwigElement elem : thisBlocks) {
					JtwigBlock block = (JtwigBlock) elem;
					if (!masterBlocks.contains(block))
						throw new TemplateBuildException("Undefined block "+block.getName());
					
					parent.replace(block);
				}
				
				return parent;
			}
		}
		
		return root;
	}
	
	public void process (Map<String, Object> model, OutputStream out) throws JtwigRenderException {
		try {
			out.write(this.resolved.renderer(model).render().getBytes());
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new JtwigRenderException(e);
		}
	}
}
