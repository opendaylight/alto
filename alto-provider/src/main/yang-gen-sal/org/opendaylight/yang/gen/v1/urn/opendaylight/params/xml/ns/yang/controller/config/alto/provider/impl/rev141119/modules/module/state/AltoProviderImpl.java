package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.state;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.rev130405.modules.module.State;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * &lt;p&gt;This class represents the following YANG schema fragment defined in module &lt;b&gt;alto-provider-impl&lt;/b&gt;
 * &lt;br&gt;(Source path: &lt;i&gt;META-INF/yang/alto-provider-impl.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * case alto-provider-impl {
 *     leaf toasts-made {
 *         type uint32;
 *     }
 * }
 * &lt;/pre&gt;
 * The schema path to identify an instance is
 * &lt;i&gt;alto-provider-impl/modules/module/state/(urn:opendaylight:params:xml:ns:yang:controller:config:alto-provider:impl?revision=2014-11-19)alto-provider-impl&lt;/i&gt;
 *
 */
public interface AltoProviderImpl
    extends
    DataObject,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.alto.provider.impl.rev141119.modules.module.state.AltoProviderImpl>,
    State
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.cachedReference(org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:params:xml:ns:yang:controller:config:alto-provider:impl","2014-11-19","alto-provider-impl"));

    java.lang.Long getToastsMade();

}

