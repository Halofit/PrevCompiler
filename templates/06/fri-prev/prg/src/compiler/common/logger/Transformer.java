package compiler.common.logger;

import org.w3c.dom.*;

/**
 * Document transformer.
 * 
 * @author sliva
 */
public interface Transformer {

	/**
	 * Transforms the document.
	 * 
	 * @param doc
	 *            The document to be transformed.
	 * @return The transformed document.
	 */
	public Document transform(Document doc);

}
