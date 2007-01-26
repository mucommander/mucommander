package com.mucommander.command;

/**
 * Used to explore a list of associations in a format independant fashion.
 * <p>
 * Different classes, such as {@link CommandManager} or {@link AssociationReader}, know how to
 * generate a list of associations - from instances loaded in memory or from a file, for example.
 * Implementing <code>AssociationBuilder</code> allows classes to query these lists regardless of
 * their source.
 * </p>
 * <p>
 * Instances of <code>AssociationBuilder</code> can rely on their methods to be called in the proper order,
 * and on both their {@link #startBuilding()} and {@link #endBuilding()} methods to be called. Classes that
 * interact with such instances must make sure this contract is respected.
 * </p>
 * <p>
 * A (fairly useless) implemention might look like:
 * <pre>
 * public class AssociationPrinter implements AssociationBuilder {
 *    public void startBuilding() {System.out.println("Begining association list building...");}
 *
 *    public void endBuilding() {System.out.println("Done.");}
 *
 *    public void addAssociation(String mask, int read, int write, int execute, String command) throws CommandException {
 *        System.out.println(" - matching '" + mask + "' to '" + command + "'");
 *    }
 * }
 * </pre>
 * Passing an instance of <code>AssociationPrinter</code> to {@link com.mucommander.command.CommandManager#buildAssociations(AssociationBuilder)}
 * will result in something like:
 * <pre>
 * Begining association list building...
 * - matching '^https?:\/\/.+' to 'Safari'
 * - matching '.*' to 'open'
 * Done.
 * </pre>
 * </p>
 * @author Nicolas Rinaudo
 * @see    com.mucommander.command.AssociationReader
 * @see    com.mucommander.command.CommandManager#buildAssociations(AssociationBuilder)
 */
public interface AssociationBuilder {
    /**
     * Notifies the builder that association building is about to start.
     * @throws CommandException if an error occurs.
     */
    public void startBuilding() throws CommandException;

    /**
     * Notifies the builder that association building is finished.
     * @throws CommandException if an error occurs.
     */
    public void endBuilding() throws CommandException;

    /**
     * Notifies the builder that a new association has been found.
     * <p>
     * The permission flags can have any of the following values:
     * <ul>
     *  <li>{@link CommandAssociation#YES}, if a file must have the permission flag set to match the association.</li>
     *  <li>{@link CommandAssociation#NO}, if a file must not have the permission flag set to match the association.</li>
     *  <li>{@link CommandAssociation#UNFILTERED}, if a file's permission flag is not taken into account by the association.</li>
     * </ul>
     * </p>
     * @param  mask             regular expression that file names must match in order to be affected by this association.
     * @param  read             <i>read</i> permissions mask that a file must match in order to be affected by this association.
     * @param  write            <i>write</i> permissions mask that a file must match in order to be affected by this association.
     * @param  execute          <i>execute</i> permissions mask that a file must match in order to be affected by this association.
     * @param  command          alias of the command that will be executed on files that match this association.
     * @throws CommandException if an error occurs.
     */
    public void addAssociation(String mask, int read, int write, int execute, String command) throws CommandException;
}
