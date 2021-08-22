package com.ne.gs.services.custom;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author KID
 *
 */
public class ChangeColorService {
	private static final Logger log = LoggerFactory.getLogger(ChangeColorService.class);
	private static final ChangeColorService controller = new ChangeColorService();
	public static ChangeColorService getInstance() {
		return controller;
	}
	
	public ChangeColorService() {
		try {
			this.loadXML();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Map<String, String> colors = new HashMap<>();
	
	public void reload() throws Exception {
		log.info("reward service reload");
		this.colors.clear();
		this.loadXML();
	}
	
	public void loadXML() throws Exception {
		File xml = new File("./config/custom_data/custom_color.xml");
		Document doc = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		if (xml.exists()) {
			try {
				doc = factory.newDocumentBuilder().parse(xml);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			Node table = doc.getFirstChild();
			for (Node nodea = table.getFirstChild(); nodea != null; nodea = nodea.getNextSibling()) {
				if (nodea.getNodeName().equals("color")) {
					NamedNodeMap attrs = nodea.getAttributes();
					
					this.colors.put(attrs.getNamedItem("name").getNodeValue(), attrs.getNamedItem("hex").getNodeValue());
				}
			}
		}
		log.info("ChangeColorService: "+this.colors.size()+" colors.");
	}
}
