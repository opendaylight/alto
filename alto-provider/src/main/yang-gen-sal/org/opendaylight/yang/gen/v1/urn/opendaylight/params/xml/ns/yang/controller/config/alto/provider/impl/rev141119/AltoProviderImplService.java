package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119;
import java.util.concurrent.Future;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;


/**
 * Interface for implementing the following YANG RPCs defined in module &lt;b&gt;alto-provider-impl&lt;/b&gt;
 * &lt;br&gt;(Source path: &lt;i&gt;META-INF/yang/alto-provider-impl.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * rpc clear-toasts-made {
 *     "JMX call to clear the toasts-made counter.";
 *     input {
 *         leaf context-instance {
 *             type instance-identifier;
 *         }
 *     }
 *     
 *     status CURRENT;
 * }
 * &lt;/pre&gt;
 *
 */
public interface AltoProviderImplService
    extends
    RpcService
{




    /**
     * JMX call to clear the toasts-made counter.
     *
     */
    Future<RpcResult<java.lang.Void>> clearToastsMade(ClearToastsMadeInput input);

}

