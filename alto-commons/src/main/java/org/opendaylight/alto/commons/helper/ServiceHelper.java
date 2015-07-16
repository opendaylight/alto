package org.opendaylight.alto.commons.helper;

import java.util.Dictionary;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class ServiceHelper {
  private static final Logger logger = LoggerFactory
      .getLogger(ServiceHelper.class);

  /**
   * Register a Global Service in the OSGi service registry
   *
   * @param clazz
   *          The target class
   * @param instance
   *          of the object exporting the service, be careful the object must
   *          implement/extend clazz else the registration will fail unless a
   *          ServiceFactory is passed as parameter
   * @param properties
   *          The properties to be attached to the service registration
   * @return the ServiceRegistration if registration happened, null otherwise
   */
  public static ServiceRegistration registerGlobalServiceWReg(Class<?> clazz,
      Object instance, Dictionary<String, Object> properties) {
    try {
      BundleContext bCtx = FrameworkUtil.getBundle(instance.getClass())
          .getBundleContext();
      if (bCtx == null) {
        logger.error("Could not retrieve the BundleContext");
        return null;
      }

      ServiceRegistration registration = bCtx.registerService(clazz.getName(),
          instance, properties);
      return registration;
    } catch (Exception e) {
      logger.error("Exception {} while registering the service {}",
          e.getMessage(), instance.toString());
    }
    return null;
  }

  /**
   * Retrieve global instance of a class via OSGI registry, if there are many
   * only the first is returned.
   *
   * @param clazz
   *          The target class
   * @param bundle
   *          The caller
   */
  public static Object getGlobalInstance(Class<?> clazz, Object bundle) {
    return getGlobalInstance(clazz, bundle, null);
  }


  /**
   * Retrieve global instance of a class via OSGI registry, if there are many
   * only the first is returned. On this version an LDAP type of filter is
   * applied
   *
   * @param clazz
   *          The target class
   * @param bundle
   *          The caller
   * @param serviceFilter
   *          LDAP filter to be applied in the search
   */
  public static Object getGlobalInstance(Class<?> clazz, Object bundle,
      String serviceFilter) {
    Object[] instances = getGlobalInstances(clazz, bundle, serviceFilter);
    if (instances != null) {
      return instances[0];
    }
    return null;
  }

  /**
   * Retrieve all the Instances of a Service, optionally filtered via
   * serviceFilter if non-null else all the results are returned if null
   *
   * @param clazz
   *          The target class
   * @param bundle
   *          The caller
   * @param serviceFilter
   *          LDAP filter to be applied in the search
   */
  @SuppressWarnings("unchecked")
  public static Object[] getGlobalInstances(Class<?> clazz, Object bundle,
      String serviceFilter) {
    Object instances[] = null;
    try {
      BundleContext bCtx = FrameworkUtil.getBundle(bundle.getClass())
          .getBundleContext();

      ServiceReference[] services = bCtx.getServiceReferences(clazz.getName(),
          serviceFilter);

      if (services != null) {
        instances = new Object[services.length];
        for (int i = 0; i < services.length; i++) {
          instances[i] = bCtx.getService(services[i]);
        }
      }
    } catch (Exception e) {
      logger.error("Instance reference is NULL");
    }
    return instances;
  }
}
