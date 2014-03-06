/*
 * 
 */
package it.polimi.modaclouds.space4cloud; //questa parte descrive l'appartenza della classe

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

// TODO: Auto-generated Javadoc
/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	/** The Constant PLUGIN_ID. */
	public static final String PLUGIN_ID = "it.polimi.franceschelli.space4cloud"; //$NON-NLS-1$

	// The shared instance
	/** The plugin. */
	private static Activator plugin;
	
	/**
	 * The constructor.
	 */
	public Activator() {
		// il costruttore è vuoto. 
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		//è un overload del padre. 
		super.start(context);
		plugin = this; //salva se stesso in plugin
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		//chiede l'immagine del plugin
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
