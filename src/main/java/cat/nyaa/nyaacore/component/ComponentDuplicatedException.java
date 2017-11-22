package cat.nyaa.nyaacore.component;

public class ComponentDuplicatedException extends RuntimeException {
    private final Class<? extends IComponent> componentInterface;
    private final IComponent registeredImplementation;
    private final IComponent incomingImplementation;

    public ComponentDuplicatedException(Class<? extends IComponent> componentInterface, IComponent registeredImplementation, IComponent incomingImplementation) {
        this.componentInterface = componentInterface;
        this.registeredImplementation = registeredImplementation;
        this.incomingImplementation = incomingImplementation;
    }

    public Class<? extends IComponent> getComponentInterface() {
        return componentInterface;
    }

    public IComponent getRegisteredImplementation() {
        return registeredImplementation;
    }

    public IComponent getIncomingImplementation() {
        return incomingImplementation;
    }

    @Override
    public String toString() {
        return String.format("Interface %s is already registered by %s but gets registered again by %s",
                componentInterface,
                registeredImplementation,
                incomingImplementation);
    }
}
