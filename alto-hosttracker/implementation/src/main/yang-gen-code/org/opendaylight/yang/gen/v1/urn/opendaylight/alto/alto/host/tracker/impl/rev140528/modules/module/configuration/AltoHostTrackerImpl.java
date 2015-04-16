package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.alto.host.tracker.impl.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.rev130405.modules.module.Configuration;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * &lt;p&gt;This class represents the following YANG schema fragment defined in module &lt;b&gt;alto-host-tracker-impl&lt;/b&gt;
 * &lt;br&gt;(Source path: &lt;i&gt;META-INF/yang/alto-host-tracker-impl.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * case alto-host-tracker-impl {
 *     leaf topology-id {
 *         type string;
 *     }
 *     container data-broker {
 *         leaf type {
 *             type leafref;
 *         }
 *         leaf name {
 *             type leafref;
 *         }
 *         uses service-ref {
 *             refine (urn:opendaylight:alto:alto-host-tracker-impl?revision=2014-05-28)type {
 *                 leaf type {
 *                     type leafref;
 *                 }
 *             }
 *         }
 *     }
 * }
 * &lt;/pre&gt;
 * The schema path to identify an instance is
 * &lt;i&gt;alto-host-tracker-impl/modules/module/configuration/(urn:opendaylight:alto:alto-host-tracker-impl?revision=2014-05-28)alto-host-tracker-impl&lt;/i&gt;
 *
 */
public interface AltoHostTrackerImpl
    extends
    DataObject,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.alto.alto.host.tracker.impl.rev140528.modules.module.configuration.AltoHostTrackerImpl>,
    Configuration
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.cachedReference(org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:alto:alto-host-tracker-impl","2014-05-28","alto-host-tracker-impl"));

    java.lang.String getTopologyId();
    
    DataBroker getDataBroker();

}

