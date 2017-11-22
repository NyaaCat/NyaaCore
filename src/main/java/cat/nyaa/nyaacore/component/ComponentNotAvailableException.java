package cat.nyaa.nyaacore.component;

public class ComponentNotAvailableException extends RuntimeException {
    private final Class<? extends IComponent> componentInterface;

    public ComponentNotAvailableException(Class<? extends IComponent> componentInterface) {
        this.componentInterface = componentInterface;
    }

    public Class<? extends IComponent> getComponentInterface() {
        return componentInterface;
    }

    @Override
    public String toString() {
        return String.format("There is no implementation registered for %s", componentInterface);
    }
}
