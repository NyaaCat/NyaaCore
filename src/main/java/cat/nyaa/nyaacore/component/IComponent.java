package cat.nyaa.nyaacore.component;

public interface IComponent {

    /**
     * @param successor the new implementation about to replace this one
     * @return if the successor can be the new implementation for the Component type.
     */
    default boolean canReplaceMe(IComponent successor) {
        return (this.getClass() == successor.getClass());
    }
}
