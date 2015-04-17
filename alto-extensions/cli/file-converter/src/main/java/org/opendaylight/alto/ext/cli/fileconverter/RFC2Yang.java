package org.opendaylight.alto.ext.cli.fileconverter;

import org.apache.karaf.shell.console.OsgiCommandSupport;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;

import org.opendaylight.alto.commons.types.converter.RFC2ModelNetworkMapConverter;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285JSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;
import org.opendaylight.alto.commons.types.model150404.ModelJSONMapper;
import org.opendaylight.alto.commons.types.model150404.ModelNetworkMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "alto", name = "file-rfc2yang", description = "Convert file from RFC 7285 format to Yang Data format")
public class RFC2Yang extends OsgiCommandSupport {

    private static final Logger logger = LoggerFactory.getLogger(RFC2Yang.class);

    @Argument(index = 0, name = "type", description = "The type of the source file", required = true, multiValued = false)
    String type = null;

    @Argument(index = 1, name = "source", description = "The source file", required = true, multiValued = false)
    String source = null;

    @Argument(index = 2, name = "target", description = "The target file", required = false, multiValued = false)
    String target = null;

    protected RFC7285JSONMapper rfcMapper = new RFC7285JSONMapper();
    protected ModelJSONMapper modelMapper = new ModelJSONMapper();
    protected FileConverterHelper helper = new FileConverterHelper();

    @Override
    protected Object doExecute() throws Exception {
        logger.info("command: alto:rfc2yang {} {} {}",
                        type, source, target);
        if (ConvertType.NETWORK_MAP.equals(type)) {
            String input = helper.load(source);
            RFC7285NetworkMap rfcMap = rfcMapper.asNetworkMap(input);

            RFC2ModelNetworkMapConverter conv = new RFC2ModelNetworkMapConverter();

            ModelNetworkMap modelMap = conv.convert(rfcMap);
            String output = modelMapper.asJSON(modelMap);
            helper.save(target, output);
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

