/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.ext.cli.fileconverter;

import org.apache.karaf.shell.console.OsgiCommandSupport;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "alto", name = "file-yang2rfc", description = "The converter between RFC 7285 and Yang Model files")
public class Yang2RFC extends OsgiCommandSupport {

    private static final Logger logger = LoggerFactory.getLogger(Yang2RFC.class);

    @Argument(index = 0, name = "type", description = "The type of the source file", required = true, multiValued = false)
    String type = null;

    @Argument(index = 1, name = "source", description = "The source file", required = true, multiValued = false)
    String source = null;

    @Argument(index = 2, name = "target", description = "The target file", required = false, multiValued = false)
    String target = null;

    @Override
    protected Object doExecute() throws Exception {
        logger.info("command: alto:rfc2yang {} {} {}",
                        type, source, target);

        throw new UnsupportedOperationException("Not implemented yet");
    }
}

