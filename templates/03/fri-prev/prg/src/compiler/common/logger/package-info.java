/**
 * Logging the results of compiler phases.
 * 
 * <p>
 * Each compiler phase can be logged separately using an object of class
 * {@link compiler.common.logger.Logger}. Apart from the constructor and method
 * {@link compiler.common.logger.Logger#close() close} it supports adding nested
 * XML elements and their attributes to the log document. Elements are added as
 * if the document tree is traversed using a depth-first strategy. In other
 * words,
 * <ul>
 * <li>each time a new element is constructed, it is added as a child to the
 * current element and it becomes the current element;</li>
 * <li>each time a construction of the current element is finished, its parent
 * becomes the current element again.</li>
 * </ul>
 * 
 * @author sliva
 */
package compiler.common.logger;