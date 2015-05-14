package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.hosttracker.rev150416;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.DataObject;


/**
 * cost set in hosttracker
 *
 * &lt;p&gt;This class represents the following YANG schema fragment defined in module &lt;b&gt;alto-hosttracker-cost-service&lt;/b&gt;
 * &lt;br&gt;(Source path: &lt;i&gt;META-INF/yang/alto-hosttracker-cost-service.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * grouping cost {
 *     leaf cost-value {
 *         type int32;
 *     }
 * }
 * &lt;/pre&gt;
 * The schema path to identify an instance is
 * &lt;i&gt;alto-hosttracker-cost-service/cost&lt;/i&gt;
 *
 */
public interface Cost
    extends
    DataObject
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.cachedReference(org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:alto:hosttracker","2015-04-16","cost"));

    java.lang.Integer getCostValue();

}

