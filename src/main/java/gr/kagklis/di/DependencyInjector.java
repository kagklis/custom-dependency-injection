package gr.kagklis.di;

import gr.kagklis.di.annotations.CustomAutowired;
import gr.kagklis.di.annotations.CustomComponent;
import gr.kagklis.di.annotations.CustomQualifier;
import gr.kagklis.di.exceptions.*;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class DependencyInjector {
    Map<Class<?>, Object> singletonsMap;
    Map<Class<?>, Class<?>> diMap;

    public DependencyInjector() {
        singletonsMap = new HashMap<>();
        diMap = createDependencyInjectionMap();
    }

    public Map<Class<?>, Class<?>> createDependencyInjectionMap() {
        Map<Class<?>, Class<?>> result = new HashMap<>();
        Set<Class<?>> componentClasses = findCustomComponents();
        for (Class<?> componentClass : componentClasses) {
            if (isInterface(componentClass)) {
                throw new InterfaceMarkedAsComponentException(componentClass.getTypeName());
            }
            fillDependencyInjectionMap(result, componentClass);
        }
        return result;
    }

    private Set<Class<?>> findCustomComponents() {
        Reflections reflections = new Reflections(this.getClass().getPackage().getName());
        return reflections.getTypesAnnotatedWith(CustomComponent.class);
    }

    private boolean isInterface(Class<?> givenClass) {
        return Modifier.isInterface(givenClass.getModifiers());
    }

    private void fillDependencyInjectionMap(Map<Class<?>, Class<?>> result, Class<?> implementationClass) {
        Class<?>[] interfaces = implementationClass.getInterfaces();
        if (interfaces.length == 0) {
            result.put(implementationClass, implementationClass);
        } else {
            Arrays.stream(interfaces).forEach(ifc -> result.put(implementationClass, ifc));
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T singletonOf(Class<T> requestedClass) {
        if (singletonsMap.containsKey(requestedClass)) {
            return (T) singletonsMap.get(requestedClass);
        }

        T result = createObject(requestedClass);
        singletonsMap.put(requestedClass, result);
        return result;
    }

    public <T> T oneOf(Class<T> requestedClass) {
        return createObject(requestedClass);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> listOf(Class<T> interfaceClass) {
        List<T> result = new ArrayList<>();
        Set<Entry<Class<?>, Class<?>>> implementationClasses = diMap.entrySet().stream()
                .filter(entry -> entry.getValue() == interfaceClass).collect(Collectors.toSet());

        if (implementationClasses.isEmpty()) {
            throw new NoImplementationFoundException(interfaceClass.getTypeName());
        }

        for (Entry<Class<?>, Class<?>> entry : implementationClasses) {
            result.add(createObject((Class<T>) entry.getKey()));
        }
        return result;
    }

    private <T> T createObject(Class<T> clazz) {
        return createObject(clazz, null);
    }

    private <T> T createObject(Class<T> passedClass, HashSet<String> dependencies) {
        checkClassIsValidDependency(passedClass);
        Class<T> resolvedClass = findImplementationClass(passedClass);
        checkClassIsNotAbstract(resolvedClass);
        dependencies = processDependencies(passedClass, dependencies);
        return instantiateAndAutowire(resolvedClass, dependencies);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> findImplementationClass(Class<T> givenClass) {
        Class<?> result;
        if (isInterface(givenClass)) {
            result = findInterfaceImplementationClass(givenClass);
        } else {
            result = givenClass;
        }
        return (Class<T>) result;
    }

    private <T> Class<?> findInterfaceImplementationClass(Class<T> givenClass) {
        Class<?> result;
        Set<Entry<Class<?>, Class<?>>> implementations = diMap.entrySet().stream()
                .filter(entry -> entry.getValue() == givenClass).collect(Collectors.toSet());

        if (implementations.isEmpty()) {
            throw new NoImplementationFoundException(givenClass.getTypeName());
        } else if (implementations.size() == 1) {
            result = implementations.stream().findFirst().get().getKey();
        } else {
            result = resolveInterfaceImplementation(givenClass, implementations);
        }
        return result;
    }

    private <T> Class<?> resolveInterfaceImplementation(Class<T> givenClass, Set<Entry<Class<?>, Class<?>>> implementations) {
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

    private <T> void checkClassIsValidDependency(Class<T> givenClass) {
        if (diMap.containsKey(givenClass) || diMap.containsValue(givenClass)) {
            return;
        }

        if (isInterface(givenClass)) {
            throw new NoImplementationFoundException(givenClass.getTypeName());
        } else {
            throw new ComponentNotFoundException(givenClass.getTypeName());
        }
    }

    private void checkClassIsNotAbstract(Class<?> resolvedClass) {
        if (Modifier.isAbstract(resolvedClass.getModifiers())) {
            throw new AbstractCreationNotAllowedException(resolvedClass.getTypeName());
        }
    }

    private <T> HashSet<String> processDependencies(Class<T> passedClass, HashSet<String> dependencies) {
        // Initialize dependency list.
        if (dependencies == null) {
            dependencies = new HashSet<>();
        }

        // Check for circular dependencies.
        String passedClassTypeName = passedClass.getTypeName();
        if (dependencies.contains(passedClassTypeName)) {
            throw new CircularDependencyException(passedClassTypeName);
        }

        // Add current class to dependencies.
        dependencies.add(passedClassTypeName);
        return dependencies;
    }

    private <T> T instantiateAndAutowire(Class<T> resolvedClass, HashSet<String> dependencies) {
        T resultObject = null;
        try {
            resultObject = resolvedClass.newInstance();
            autowire(resolvedClass, resultObject, dependencies);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    private void autowire(Class<?> actualClass, Object classInstance, HashSet<String> dependencies)
            throws InstantiationException, IllegalAccessException {
        Set<Field> fields = findAutowiredFields(actualClass);
        boolean hasMultipleFields = fields.size() > 1;
        for (Field field : fields) {
            HashSet<String> branchDependencies = hasMultipleFields ? new HashSet<>(dependencies) : dependencies;
            Object fieldInstance = createObject(field.getType(), branchDependencies);
            field.set(classInstance, fieldInstance);
        }
    }

    private Set<Field> findAutowiredFields(Class<?> givenClass) {
        Set<Field> results = new HashSet<>();
        while (givenClass != null) {
            for (Field field : givenClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(CustomAutowired.class)) {
                    field.setAccessible(true);
                    results.add(field);
                }
            }
            givenClass = givenClass.getSuperclass();
        }
        return results;
    }

}
