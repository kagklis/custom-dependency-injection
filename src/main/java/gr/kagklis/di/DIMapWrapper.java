package gr.kagklis.di;

import gr.kagklis.di.annotations.CustomComponent;
import gr.kagklis.di.annotations.CustomQualifier;
import gr.kagklis.di.exceptions.*;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DIMapWrapper {

    private Map<Class<?>, Class<?>> diMap;

    public DIMapWrapper() {
        diMap = new HashMap<>();
        for (Class<?> componentClass : findCustomComponents(this.getClass().getPackage().getName())) {
            if (isInterface(componentClass)) {
                throw new InterfaceMarkedAsComponentException(componentClass.getTypeName());
            }
            registerDependenciesToMap(componentClass);
        }
    }

    private <T> void checkClassHasImplementation(Class<T> givenClass) {
        if (diMap.containsKey(givenClass) || diMap.containsValue(givenClass)) {
            return;
        }

        if (isInterface(givenClass)) {
            throw new NoImplementationFoundException(givenClass.getTypeName());
        } else {
            throw new ComponentNotFoundException(givenClass.getTypeName());
        }
    }

    private boolean isInterface(Class<?> givenClass) {
        return Modifier.isInterface(givenClass.getModifiers());
    }

    public Set<Map.Entry<Class<?>, Class<?>>> findAllInterfaceImplementationClasses(Class<?> givenClass) {
        return diMap.entrySet().stream()
                .filter(entry -> entry.getValue() == givenClass)
                .collect(Collectors.toSet());
    }

    public <T> Class<T> findImplementationClass(Class<T> givenClass) {
        checkClassHasImplementation(givenClass);
        return isInterface(givenClass) ?
                findInterfaceImplementationClass(givenClass) :
                givenClass;
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> findInterfaceImplementationClass(Class<T> givenClass) {
        Class<?> result;
        Set<Map.Entry<Class<?>, Class<?>>> implementations = findAllInterfaceImplementationClasses(givenClass);
        if (implementations.size() == 1) {
            result = implementations.stream().findFirst().get().getKey();
        } else {
            result = resolveInterfaceImplementation(givenClass, implementations);
        }
        return (Class<T>) result;
    }

    private <T> Class<?> resolveInterfaceImplementation(Class<T> givenClass, Set<Map.Entry<Class<?>, Class<?>>> implementations) {
        if (!givenClass.isAnnotationPresent(CustomQualifier.class)) {
            throw new MoreThanOneImplementationFoundException(givenClass.getTypeName(), implementations.size());
        }

        String qualifier = givenClass.getAnnotation(CustomQualifier.class).value();
        try {
            return Class.forName(qualifier);
        } catch (ClassNotFoundException e) {
            throw new InvalidQualifierValueFoundException(qualifier);
        }
    }

    private Set<Class<?>> findCustomComponents(String rootPackage) {
        Reflections reflections = new Reflections(rootPackage);
        return reflections.getTypesAnnotatedWith(CustomComponent.class);
    }

    private void registerDependenciesToMap(Class<?> implementationClass) {
        Class<?>[] interfaces = implementationClass.getInterfaces();
        if (interfaces.length == 0) {
            diMap.put(implementationClass, implementationClass);
        } else {
            Arrays.stream(interfaces).forEach(ifc -> diMap.put(implementationClass, ifc));
        }
    }

}
