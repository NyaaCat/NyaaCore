package cat.nyaa.nyaacore.component;

public interface IComponent {

    default boolean unload(IComponent successor) {
        return (this.getClass() == successor.getClass());
    }
}
