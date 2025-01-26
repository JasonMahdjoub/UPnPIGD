/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.distrimind.upnp_igd.support.shared;

import com.distrimind.upnp_igd.Log;
import com.distrimind.upnp_igd.model.ModelUtil;
import com.distrimind.upnp_igd.model.XMLUtil;
import com.distrimind.upnp_igd.swing.Application;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.flexilogxml.xml.IXmlReader;
import com.distrimind.flexilogxml.xml.IXmlWriter;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Christian Bauer
 */
public class TextExpandDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    // TODO: Make this a plugin SPI and let the plugin impl decide how text should be detected and rendered

    final private static DMLogger log = Log.getLogger(TextExpandDialog.class);

    public TextExpandDialog(Frame frame, String text) {
        super(frame);
        setResizable(true);

        JTextArea textArea = new JTextArea();
        JScrollPane textPane = new JScrollPane(textArea);
        textPane.setPreferredSize(new Dimension(500, 400));
        add(textPane);

        String pretty;
        if (text.startsWith("<") && text.endsWith(">")) {


            try {
                IXmlReader reader=XMLUtil.getXMLReader(text);
                try(ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                    IXmlWriter writer = XMLUtil.getXMLWriter(true, out);
                    reader.transferTo(writer);
                    writer.close();
                    reader.close();
                    out.flush();
                    pretty=new String(out.toByteArray(), StandardCharsets.UTF_8);
                }
            } catch (Exception ex) {
                log.error(() -> "Error pretty printing XML: " + ex);
                pretty = text;
            }
        } else if (text.startsWith("http-get")) {
            pretty = ModelUtil.commaToNewline(text);
        } else {
            pretty = text;
        }

        textArea.setEditable(false);
        textArea.setText(pretty);

        pack();
        Application.center(this, getOwner());
        setVisible(true);
    }
}
